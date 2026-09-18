package com.example.data.model

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
