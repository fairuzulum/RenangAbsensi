// data/model/Payment.kt
package com.coachbro.absenrenang.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Payment(
    @DocumentId
    val id: String = "",

    // Anotasi ini akan membuat Firestore otomatis mengisi field ini
    // dengan waktu server saat dokumen dibuat. Sangat akurat!
    @ServerTimestamp
    val date: Date? = null,

    val amount: Long = 0, // Jumlah uang yang dibayarkan
    val sessionsAdded: Long = 0 // Jumlah sesi yang didapat
)