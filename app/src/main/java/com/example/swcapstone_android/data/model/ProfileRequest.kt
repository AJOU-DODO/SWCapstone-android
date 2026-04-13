package com.example.swcapstone_android.data.model

data class ProfileRequest(
    val nickname: String,
    val fcmToken: String?,
    val profileImageUrl: String?,
    val deviceType: String = "ANDROID"
)
