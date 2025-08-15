// data/repository/StudentRepository.kt
package com.coachbro.absenrenang.data.repository

import com.coachbro.absenrenang.data.model.Student
import com.coachbro.absenrenang.data.model.Payment
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class StudentRepository {

    private val db = FirebaseFirestore.getInstance()
    private val studentCollection = db.collection("students")

    // Fungsi suspend menandakan ini adalah fungsi asynchronous (coroutine)
    // yang tidak akan memblokir main thread.
    suspend fun registerStudent(student: Student): Result<Unit> {
        return try {
            // Firestore akan otomatis membuat ID jika kita tidak menentukannya
            studentCollection.add(student).await()
            Result.success(Unit) // Mengembalikan hasil sukses
        } catch (e: Exception) {
            // Mengembalikan hasil gagal beserta exception-nya
            Result.failure(e)
        }
    }

    // Fungsi suspend menandakan ini adalah fungsi asynchronous (coroutine)
    // yang tidak akan memblokir main thread.
    suspend fun getAllStudents(): Result<List<Student>> {
        return try {
            val snapshot = studentCollection.get().await()
            // Mengubah hasil query dari Firestore menjadi List<Student>
            val students = snapshot.toObjects(Student::class.java)
            Result.success(students)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // FUNGSI BARU: Mengambil riwayat pembayaran seorang siswa
    suspend fun getPaymentHistory(studentId: String): Result<List<Payment>> {
        return try {
            val snapshot = studentCollection.document(studentId)
                .collection("payments") // Mengakses sub-collection 'payments'
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            val payments = snapshot.toObjects(Payment::class.java)
            Result.success(payments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // FUNGSI BARU: Memproses pembayaran menggunakan Transaction
    suspend fun processPayment(studentId: String, amount: Long): Result<Unit> {
        // Logika bisnis: 250rb = 4 sesi
        val sessionsToAdd = (amount / 250000) * 4
        if (sessionsToAdd <= 0) {
            return Result.failure(Exception("Jumlah pembayaran tidak valid"))
        }

        try {
            // Transaction memastikan semua operasi di dalamnya (baca, tulis, update)
            // berhasil semua, atau gagal semua. Ini mencegah data korup.
            db.runTransaction { transaction ->
                val studentRef = studentCollection.document(studentId)
                val studentSnapshot = transaction.get(studentRef)

                val currentSessions = studentSnapshot.getLong("remainingSessions")?.toInt() ?: 0
                val newTotalSessions = currentSessions + sessionsToAdd

                // 1. Update sisa sesi di dokumen siswa
                transaction.update(studentRef, "remainingSessions", newTotalSessions)

                // 2. Buat catatan baru di sub-collection 'payments'
                val paymentRef = studentRef.collection("payments").document()
                val newPayment = Payment(amount = amount, sessionsAdded = sessionsToAdd)
                transaction.set(paymentRef, newPayment)

                // Jika tidak ada error, transaction akan commit secara otomatis
            }.await()
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
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

}