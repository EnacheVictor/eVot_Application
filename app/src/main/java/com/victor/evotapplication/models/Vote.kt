package com.victor.evotapplication.models

import java.util.Date

// Data model representing a vote

data class Vote(
    val id: String = "",
    val question: String = "",
    val createdBy: String = "",
    val associationId: String = "",
    val active: Boolean = true,
    val deadline: Date? = null
)