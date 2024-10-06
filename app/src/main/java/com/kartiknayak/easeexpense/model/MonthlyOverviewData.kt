package com.kartiknayak.easeexpense.model

data class MonthlyOverviewData(
    val time: String,
    val transactionCount: Int,
    val budget: Double,
    val expense: Double,
    val savings: Double,
    val overspent: Double,
)