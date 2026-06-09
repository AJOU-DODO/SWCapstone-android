package com.example.swcapstone_android.data.model

data class NoticeResponse(
    val status: String,
    val code: String,
    val message: String,
    val data: NoticePagedData
)

data class NoticePagedData(
    val content: List<NoticeItem>
)

data class NoticeItem(
    val id: Long,
    val category: String, // UPDATE, EVENT 등
    val categoryDescription: String,
    val title: String,
    val content: String,
    val createdAt: String
)