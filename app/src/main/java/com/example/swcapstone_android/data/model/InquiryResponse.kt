package com.example.swcapstone_android.data.model

import com.google.gson.annotations.SerializedName
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class InquiryResponse(
    @SerializedName("status") val status: String,
    @SerializedName("code") val code: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: InquiryPageData
)

data class InquiryPageData(
    @SerializedName("content") val content: List<InquiryItem>
)

data class InquiryItem(
    @SerializedName("id") val id: Long,
    @SerializedName("type") val type: String,                       // "ACCOUNT", "BUG" 등
    @SerializedName("typeDescription") val typeDescription: String,   // "계정", "버그" 등
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String,
    @SerializedName("answer") val answer: String?,                    // 답변 없으면 null 가능성 고려
    @SerializedName("status") val status: String,                     // "PENDING", "ANSWERED"
    @SerializedName("statusDescription") val statusDescription: String, // "답변 대기", "답변 완료"
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("answeredAt") val answeredAt: String?
) {
    // 🗓️ "2019-08-24T14:15:22.123Z" 포맷을 "2019.08.24" 형태로 이쁘게 변환해 주는 헬퍼
    val formattedDate: String
        get() = try {
            val parsed = ZonedDateTime.parse(createdAt)
            parsed.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        } catch (e: Exception) {
            createdAt.substringBefore("T") // 파싱 실패 시 차선책으로 날짜만 컷
        }
}