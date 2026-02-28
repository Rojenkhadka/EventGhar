package com.example.eventghar.data

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImageUri: String = "",
    val isVerified: Boolean = true
)

