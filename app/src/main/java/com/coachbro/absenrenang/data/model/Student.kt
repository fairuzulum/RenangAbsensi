// data/model/Student.kt
package com.coachbro.absenrenang.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class Student(
    // Anotasi @DocumentId memberitahu Firestore untuk otomatis mengisi field ini
    // dengan ID dokumen saat data dibaca. Sangat berguna!
    @DocumentId
    val id: String = "",

    val name: String = "",
    val age: Int? = null, // Nullable karena boleh tidak diisi
    val parentName: String? = null, // Nullable
    val parentPhone: String? = null, // Nullable

    // Kita tambahkan ini dari sekarang untuk fitur pembayaran nanti.
    // Default-nya 0 saat siswa baru mendaftar.
    val remainingSessions: Int = 0
) : Parcelable