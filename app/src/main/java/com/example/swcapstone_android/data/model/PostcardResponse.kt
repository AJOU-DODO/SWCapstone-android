package com.example.swcapstone_android.data.model

data class PostcardRequest(
    val imageUrl: String,
    val content: String
)

data class PostcardResponse(
    val status: String,
    val message: String?
)