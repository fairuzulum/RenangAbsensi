package com.coachbro.absenrenang.data.model

import java.util.Date

data class FinancialReport(
    val id: String = "",
    val studentName: String = "",
    val amount: Long = 0,
    val paymentDate: Date? = null
)