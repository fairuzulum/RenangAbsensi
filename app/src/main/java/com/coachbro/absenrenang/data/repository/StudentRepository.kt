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
                val newPayment = Payment(amount = amount, sessionsAdded = sessionsToAdd.toLong())
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

    suspend fun processAttendance(studentId: String): Result<Unit> {
        try {
            db.runTransaction { transaction ->
                val studentRef = studentCollection.document(studentId)
                val studentSnapshot = transaction.get(studentRef)
                val currentSessions = studentSnapshot.getLong("remainingSessions")?.toInt() ?: 0

                if (currentSessions <= 0) {
                    throw Exception("Sesi pertemuan siswa sudah habis (0).")
                }
                val newTotalSessions = currentSessions - 1
                transaction.update(studentRef, "remainingSessions", newTotalSessions)
                val attendanceRef = studentRef.collection("attendances").document()
                val newAttendance = Attendance()
                transaction.set(attendanceRef, newAttendance)
            }.await()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    // ===============================================================
    // FUNGSI BARU UNTUK VALIDASI
    // ===============================================================
    suspend fun checkIfStudentAttendedToday(studentId: String): Result<Boolean> {
        return try {
            // 1. Siapkan rentang waktu untuk "hari ini"
            val calendar = Calendar.getInstance()

            // Set waktu ke awal hari (pukul 00:00:00.000)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay: Date = calendar.time

            // Set waktu ke akhir hari (pukul 23:59:59.999)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay: Date = calendar.time

            // 2. Buat query ke Firestore untuk mencari data dalam rentang waktu tersebut
            val snapshot = studentCollection.document(studentId)
                .collection("attendances")
                .whereGreaterThanOrEqualTo("date", startOfDay)
                .whereLessThanOrEqualTo("date", endOfDay)
                .limit(1) // Optimasi: kita hanya butuh 1 dokumen untuk membuktikan
                .get()
                .await()

            // 3. Kembalikan hasilnya. Jika snapshot tidak kosong, berarti sudah ada data (sudah absen)
            Result.success(!snapshot.isEmpty)
        } catch (e: Exception) {
            // Jika terjadi error saat query, kembalikan failure
            Result.failure(e)
        }
    }

    // ===============================================================
    // FUNGSI BARU UNTUK MENGAMBIL LAPORAN KEUANGAN
    // ===============================================================
    suspend fun getFinancialReport(): Result<List<FinancialReport>> {
        return try {
            // Kita gunakan coroutineScope untuk menjalankan beberapa query secara paralel
            coroutineScope {
                // 1. Ambil dulu semua data siswa
                val studentsSnapshot = studentCollection.get().await()
                val students = studentsSnapshot.toObjects(Student::class.java)

                // 2. Untuk setiap siswa, buat tugas (deferred task) untuk mengambil riwayat pembayarannya
                val reportTasks = students.map { student ->
                    async {
                        // Ambil semua dokumen dari sub-collection 'payments' untuk siswa ini
                        val paymentsSnapshot = studentCollection.document(student.id)
                            .collection("payments")
                            .get()
                            .await()

                        // Ubah dokumen menjadi objek Payment
                        val payments = paymentsSnapshot.toObjects(Payment::class.java)

                        // Hitung total pembayaran untuk siswa ini
                        val totalAmountForStudent = payments.sumOf { it.amount }

                        // Buat objek FinancialReport jika ada pembayaran
                        if (totalAmountForStudent > 0) {
                            FinancialReport(studentName = student.name, totalAmount = totalAmountForStudent)
                        } else {
                            null // Kembalikan null jika siswa belum pernah bayar
                        }
                    }
                }

                // 3. Jalankan semua tugas secara bersamaan dan tunggu hasilnya
                val reports = reportTasks.awaitAll()
                    .filterNotNull() // Buang hasil yang null (siswa yang belum bayar)
                    .sortedByDescending { it.totalAmount } // Urutkan dari pemasukan terbesar

                Result.success(reports)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===============================================================
    // FUNGSI BARU UNTUK UPDATE DATA SISWA
    // ===============================================================
    suspend fun updateStudent(student: Student): Result<Unit> {
        return try {
            // Menggunakan ID dokumen untuk menargetkan siswa yang akan di-update
            studentCollection.document(student.id).set(student).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===============================================================
    // FUNGSI BARU UNTUK MENGHAPUS DATA SISWA
    // ===============================================================
    suspend fun deleteStudent(studentId: String): Result<Unit> {
        return try {
            // Hapus dokumen siswa beserta semua sub-collection di dalamnya
            // (seperti riwayat absensi dan pembayaran) akan otomatis terhapus
            // jika menggunakan fitur "Delete collection" dari Firebase Extensions.
            // Untuk sekarang, kita hapus dokumen utamanya saja.
            studentCollection.document(studentId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}