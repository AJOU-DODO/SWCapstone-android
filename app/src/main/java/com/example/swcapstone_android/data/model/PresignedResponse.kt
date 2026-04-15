package com.example.swcapstone_android.data.model

data class PresignedResponse(
    val status: String,
    val data: PresignedData
)

data class PresignedData(
    val presignedUrl: String,
    val fileUrl: String
)