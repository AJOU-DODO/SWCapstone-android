package com.example.swcapstone_android.data.model

enum class InquiryType(theName: String) {
    SUGGESTION("건의사항"),
    BUG_REPORT("오류신고"),
    INQUIRY("일반문의");

    // UI에 이쁘게 한글로 보여주기 위한 속성
    val title: String = theName
}