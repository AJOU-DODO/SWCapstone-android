package com.example.swcapstone_android.data.model

data class DeviceRequest(
    val fcmToken: String,
    val deviceType: String = "ANDROID"
)