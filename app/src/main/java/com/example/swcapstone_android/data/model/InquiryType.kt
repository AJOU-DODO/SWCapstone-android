package com.example.swcapstone_android.data.model

enum class InquiryType(theName: String) {
    ACCOUNT("계정 문의"),
    BUG("오류/버그"),
    SUGGESTION("기능 건의"),
    BUSINESS("비즈니스 문의");

    // UI에 이쁘게 한글로 보여주기 위한 속성
    val title: String = theName
}