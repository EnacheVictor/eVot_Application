package com.victor.evotapplication.models

import java.util.Date

data class Comment(
    val id: String = "",
    val announcementId: String = "",
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Date? = null
)