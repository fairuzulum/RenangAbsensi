package com.coachbro.absenrenang.data.repository

import com.coachbro.absenrenang.data.model.Attendance
import com.coachbro.absenrenang.data.model.AttendanceReport
import com.coachbro.absenrenang.data.model.FinancialReport
import com.coachbro.absenrenang.data.model.MenuPasswords
import com.coachbro.absenrenang.data.model.Payment
import com.coachbro.absenrenang.data.model.Student
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class StudentRepository {

    companion object {
        private var cachedStudents: List<Student>? = null
        private var cachedClassifications: List<com.coachbro.absenrenang.data.model.Classification>? = null

        fun clearCache() {
            cachedStudents = null
            cachedClassifications = null
        }
    }

    private val db = FirebaseFirestore.getInstance()
    private val studentCollection = db.collection("students")
    private val settingsCollection = db.collection("app_settings")
    private val classificationCollection = db.collection("classifications")

    suspend fun registerStudent(student: Student): Result<Unit> {
        return try {
            studentCollection.add(student).await()
            cachedStudents = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllStudents(forceRefresh: Boolean = false): Result<List<Student>> {
        if (!forceRefresh && cachedStudents != null) {
            return Result.success(cachedStudents!!)
        }
        return try {
            val snapshot = studentCollection
                .orderBy("remainingSessions", Query.Direction.DESCENDING)
                .get()
                .await()
            val students = snapshot.toObjects(Student::class.java)
            cachedStudents = students
            Result.success(students)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStudentById(studentId: String): Result<Student> {
        return try {
            val student = studentCollection.document(studentId).get().await().toObject(Student::class.java)
            if (student != null) Result.success(student) else Result.failure(Exception("Siswa tidak ditemukan"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPaymentHistory(studentId: String): Result<List<Payment>> {
        return try {
            val snapshot = studentCollection.document(studentId)
                .collection("payments")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            val payments = snapshot.toObjects(Payment::class.java)
            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun processPayment(studentId: String, amount: Long): Result<Unit> {
        val sessionsToAdd = (amount / 250000) * 4
        if (sessionsToAdd <= 0) {
            return Result.failure(Exception("Jumlah pembayaran tidak valid"))
        }

        try {
            db.runTransaction { transaction ->
                val studentRef = studentCollection.document(studentId)
                val studentSnapshot = transaction.get(studentRef)
                val studentName = studentSnapshot.getString("name") ?: ""
                val currentSessions = studentSnapshot.getLong("remainingSessions")?.toInt() ?: 0
                val newTotalSessions = currentSessions + sessionsToAdd
                transaction.update(studentRef, "remainingSessions", newTotalSessions)
                val paymentRef = studentRef.collection("payments").document()
                val newPayment = Payment(amount = amount, sessionsAdded = sessionsToAdd, studentName = studentName)
                transaction.set(paymentRef, newPayment)
            }.await()
            cachedStudents = null
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun getAttendanceHistory(studentId: String): Result<List<Attendance>> {
        return try {
            val snapshot = studentCollection.document(studentId)
                .collection("attendances")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            val attendances = snapshot.toObjects(Attendance::class.java)
            Result.success(attendances)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun processAttendance(studentId: String): Result<Unit> {
        try {
            val isSessionsEmpty = db.runTransaction { transaction ->
                val studentRef = studentCollection.document(studentId)
                val studentSnapshot = transaction.get(studentRef)
                val studentName = studentSnapshot.getString("name") ?: ""
                val currentSessions = studentSnapshot.getLong("remainingSessions")?.toInt() ?: 0
                val newTotalSessions = currentSessions - 1
                
                transaction.update(studentRef, "remainingSessions", newTotalSessions)
                val attendanceRef = studentRef.collection("attendances").document()
                val newAttendance = Attendance(studentName = studentName)
                transaction.set(attendanceRef, newAttendance)

                newTotalSessions <= 0
            }.await()

            cachedStudents = null

            if (isSessionsEmpty) {
                getStudentById(studentId).onSuccess { student ->
                    val message = "⚠️ <b>PERINGATAN SESI HABIS</b>\n\nSiswa: <b>${student.name}</b>\nSisa Sesi: <b>0</b>\n\nSiswa ini telah melakukan absensi terakhir. Mohon segera infokan untuk pembayaran paket selanjutnya."
                    
                    getNotificationSettings().onSuccess { settings ->
                        settings.recipientIds.forEach { chatId ->
                            com.coachbro.absenrenang.utils.TelegramService.sendMessage(
                                botToken = settings.botToken,
                                chatId = chatId,
                                message = message
                            )
                        }
                    }
                }
            }

            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun checkIfStudentAttendedToday(studentId: String): Result<Boolean> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay: Date = calendar.time

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay: Date = calendar.time

            val snapshot = studentCollection.document(studentId)
                .collection("attendances")
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThanOrEqualTo("date", endOfDay)
                .limit(1)
                .get()
                .await()
            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFinancialReport(): Result<List<FinancialReport>> {
        return try {
            val paymentsSnapshot = db.collectionGroup("payments")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(200)
                .get()
                .await()

            val reports = paymentsSnapshot.documents.mapNotNull { doc ->
                val payment = doc.toObject(Payment::class.java)
                val name = payment?.studentName?.takeIf { it.isNotBlank() } ?: "Siswa"

                FinancialReport(
                    id = doc.id,
                    studentName = name,
                    amount = payment?.amount ?: 0L,
                    paymentDate = payment?.date
                )
            }.sortedByDescending { it.paymentDate }

            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStudent(student: Student): Result<Unit> {
        return try {
            studentCollection.document(student.id).set(student).await()
            cachedStudents = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteStudent(studentId: String): Result<Unit> {
        return try {
            studentCollection.document(studentId).delete().await()
            cachedStudents = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStudentSessions(studentId: String, newSessionCount: Int): Result<Unit> {
        return try {
            studentCollection.document(studentId)
                .update("remainingSessions", newSessionCount)
                .await()
            cachedStudents = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMenuPasswords(): Result<MenuPasswords?> {
        return try {
            val snapshot = settingsCollection.document("menuPasswords").get().await()
            val passwords = snapshot.toObject<MenuPasswords>()
            Result.success(passwords)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotificationSettings(): Result<com.coachbro.absenrenang.data.model.NotificationSettings> {
        return try {
            val snapshot = settingsCollection.document("notifications").get().await()
            val settings = snapshot.toObject<com.coachbro.absenrenang.data.model.NotificationSettings>()
            Result.success(settings ?: com.coachbro.absenrenang.data.model.NotificationSettings())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNotificationSettings(settings: com.coachbro.absenrenang.data.model.NotificationSettings): Result<Unit> {
        return try {
            settingsCollection.document("notifications").set(settings).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Klasifikasi Management ---
    suspend fun getClassifications(forceRefresh: Boolean = false): Result<List<com.coachbro.absenrenang.data.model.Classification>> {
        if (!forceRefresh && cachedClassifications != null) {
            return Result.success(cachedClassifications!!)
        }
        return try {
            val snapshot = classificationCollection.get().await()
            val list = snapshot.toObjects(com.coachbro.absenrenang.data.model.Classification::class.java)
            cachedClassifications = list
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addClassification(name: String, color: String): Result<Unit> {
        return try {
            val classification = com.coachbro.absenrenang.data.model.Classification(name = name, color = color)
            classificationCollection.add(classification).await()
            cachedClassifications = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateClassification(classification: com.coachbro.absenrenang.data.model.Classification): Result<Unit> {
        return try {
            classificationCollection.document(classification.id).set(classification).await()
            cachedClassifications = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteClassification(id: String): Result<Unit> {
        return try {
            classificationCollection.document(id).delete().await()
            cachedClassifications = null
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAttendanceReport(startDate: Date, endDate: Date): Result<List<AttendanceReport>> {
        return try {
            val attendanceSnapshot = db.collectionGroup("attendances")
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
                .get()
                .await()

            val reports = attendanceSnapshot.documents.mapNotNull { doc ->
                val attendance = doc.toObject(Attendance::class.java)
                val name = attendance?.studentName?.takeIf { it.isNotBlank() } ?: "Siswa"

                com.coachbro.absenrenang.data.model.AttendanceReport(
                    studentName = name,
                    attendanceDate = attendance?.date
                )
            }.sortedByDescending { it.attendanceDate }

            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}