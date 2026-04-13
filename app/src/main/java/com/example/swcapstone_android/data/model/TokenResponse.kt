package com.example.swcapstone_android.data.model

data class TokenResponse(
    val status: String,
    val data: TokenData
)

data class TokenData(
    val accessToken: String,
    val refreshToken: String
)