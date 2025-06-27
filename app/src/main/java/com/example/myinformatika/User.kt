package com.example.myinformatika

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String?,
    val username: String?,
    val student_id: String,
    val password: String,
    val photo_URL: String? = null,
)
