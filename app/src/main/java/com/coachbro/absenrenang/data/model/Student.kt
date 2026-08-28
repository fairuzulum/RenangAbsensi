// data/model/Student.kt
package com.coachbro.absenrenang.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize

@IgnoreExtraProperties
@Parcelize
data class Student(
    @DocumentId
    val id: String = "",

    // Nama lengkap (wajib)
    val name: String = "",
    // Nama panggilan (opsional, tapi dianjurkan untuk pencarian)
    val nickname: String? = null,

    // Klasifikasi / Folder (default kosong)
    val classification: String = "",

    // Nomor HP / WhatsApp
    val phoneNumber: String = "",

    // Sisa sesi, tetap ada
    val remainingSessions: Int = 0
) : Parcelable