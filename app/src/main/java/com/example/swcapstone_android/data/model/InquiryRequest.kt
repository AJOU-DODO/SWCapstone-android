package com.example.swcapstone_android.data.model

import com.google.gson.annotations.SerializedName

data class InquiryRequest(
    @SerializedName("type")
    val type: String,       // "SUGGESTION", "BUG_REPORT", "INQUIRY" 등

    @SerializedName("title")
    val title: String,      // 문의 제목

    @SerializedName("content")
    val content: String     // 문의 본문 내용
)