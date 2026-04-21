package com.example.swcapstone_android.data.model

data class PinResponse(
    val status: String,
    val code: String,
    val message: String,
    val data: List<PinData>
)

data class PinData(
    val id: Long,
    val latitude: Double,
    val longitude: Double
)