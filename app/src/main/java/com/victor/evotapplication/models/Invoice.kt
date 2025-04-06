package com.victor.evotapplication.models

data class Invoice(
    val url: String = "",
    val fileName: String = "",
    val timestamp: Long = 0L,
    val uploadedBy: String = ""
)