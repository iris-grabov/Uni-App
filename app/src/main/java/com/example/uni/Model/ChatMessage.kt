package com.example.uni.Model

data class ChatMessage(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0
)
