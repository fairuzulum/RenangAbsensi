// data/repository/StudentRepository.kt
package com.coachbro.absenrenang.data.repository

import com.coachbro.absenrenang.data.model.Attendance
import com.coachbro.absenrenang.data.model.Payment
import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.data.model.FinancialReport
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

    private val db = FirebaseFirestore.getInstance()
    private val studentCollection = db.collection("students")

    // ... (Fungsi registerStudent, getAllStudents, getStudentById, getPaymentHistory, processPayment, getAttendanceHistory biarkan sama)

    suspend fun registerStudent(student: Student): Result<Unit> {
        return try {
            studentCollection.add(student).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllStudents(): Result<List<Student>> {
        return try {
            val snapshot = studentCollection.get().await()
            val students = snapshot.toObjects(Student::class.java)
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
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
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
                val currentSessions = studentSnapshot.getLong("remainingSessions")?.toInt() ?: 0
                val newTotalSessions = currentSessions + sessionsToAdd
                transaction.update(studentRef, "remainingSessions", newTotalSessions)
                val paymentRef = studentRef.collection("payments").document()
                val newPayment = Payment(amount = amount, sessionsAdded = sessionsToAdd)
                transaction.set(paymentRef, newPayment)
            }.await()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun getAttendanceHistory(studentId: String): Result<List<Attendance>> {
        return try {
            val snapshot = studentCollection.document(studentId)
                .collection("attendances")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            val attendances = snapshot.toObjects(Attendance::class.java)
            Result.success(attendances)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===============================================================
    // PERUBAHAN UTAMA ADA DI FUNGSI INI
    // ===============================================================
    suspend fun processAttendance(studentId: String): Result<Unit> {
        try {
            db.runTransaction { transaction ->
                val studentRef = studentCollection.document(studentId)
                val studentSnapshot = transaction.get(studentRef)
                val currentSessions = studentSnapshot.getLong("remainingSessions")?.toInt() ?: 0

                // Blok validasi 'if (currentSessions <= 0)' DIHAPUS.
                // Sekarang proses akan langsung lanjut ke pengurangan sesi.

                val newTotalSessions = currentSessions - 1

                // 1. Update (kurangi) sisa sesi di dokumen siswa
                transaction.update(studentRef, "remainingSessions", newTotalSessions)

                // 2. Buat catatan baru di sub-collection 'attendances'
                val attendanceRef = studentRef.collection("attendances").document()
                val newAttendance = Attendance()
                transaction.set(attendanceRef, newAttendance)

            }.await()
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

    // ... (Fungsi getFinancialReport, updateStudent, deleteStudent biarkan sama)
    suspend fun getFinancialReport(): Result<List<FinancialReport>> {
        return try {
            coroutineScope {
                val studentsSnapshot = studentCollection.get().await()
                val students = studentsSnapshot.toObjects(Student::class.java)

                val reportTasks = students.map { student ->
                    async {
                        val paymentsSnapshot = studentCollection.document(student.id)
                            .collection("payments")
                            .get()
                            .await()

                        val payments = paymentsSnapshot.toObjects(Payment::class.java)
                        val totalAmountForStudent = payments.sumOf { it.amount }

                        if (totalAmountForStudent > 0) {
                            FinancialReport(studentName = student.name, totalAmount = totalAmountForStudent)
                        } else {
                            null
                        }
                    }
                }
                val reports = reportTasks.awaitAll()
                    .filterNotNull()
                    .sortedByDescending { it.totalAmount }

                Result.success(reports)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStudent(student: Student): Result<Unit> {
        return try {
            studentCollection.document(student.id).set(student).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteStudent(studentId: String): Result<Unit> {
        return try {
            studentCollection.document(studentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // ===============================================================
    // FUNGSI BARU UNTUK UPDATE SESI SECARA MANUAL
    // ===============================================================
    suspend fun updateStudentSessions(studentId: String, newSessionCount: Int): Result<Unit> {
        return try {
            // Menggunakan fungsi 'update' untuk mengubah satu field spesifik
            studentCollection.document(studentId)
                .update("remainingSessions", newSessionCount)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}