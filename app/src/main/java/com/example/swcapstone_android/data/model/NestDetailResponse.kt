package com.example.swcapstone_android.data.model

data class NestDetailResponse(
    val status: String,
    val code: String,
    val message: String?,
    val data: NestDetailData
)

data class NestDetailData(
    val id: Long,
    val title: String,
    val content: String,
    val unlockRadius: Int,
    val viewCount: Int,
    val createdAt: String,
    val creatorNickname: String,
    val creatorProfileImageUrl: String?,
    val categoryNames: List<String>,
    val imageUrls: List<String>,
    val likeCount: Int,
    val dislikeCount: Int,
    val ad: Boolean,
    val unlocked: Boolean // 해금 여부 확인용
)