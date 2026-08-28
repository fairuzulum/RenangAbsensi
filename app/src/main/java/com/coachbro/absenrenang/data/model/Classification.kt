package com.coachbro.absenrenang.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Classification(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val color: String = "#0084FF" // Default sea blue
)