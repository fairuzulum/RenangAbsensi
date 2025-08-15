// data/model/Attendance.kt
package com.coachbro.absenrenang.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Attendance(
    @DocumentId
    val id: String = "",

    // Firestore akan otomatis mengisi tanggal dan waktu absensi
    // sesuai waktu di server.
    @ServerTimestamp
    val date: Date? = null
)