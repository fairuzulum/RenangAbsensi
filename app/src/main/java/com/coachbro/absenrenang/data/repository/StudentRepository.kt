// data/repository/StudentRepository.kt
package com.coachbro.absenrenang.data.repository

import com.coachbro.absenrenang.data.model.Student
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
}