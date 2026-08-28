package com.coachbro.absenrenang.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class NotificationSettings(
    val botToken: String = "8852329746:AAGo62uvKtONWVebAr72LSZhOQ1bj34ClAA",
    val recipientIds: List<String> = listOf("1137962456")
)