package com.example.swcapstone_android.data.model

import com.google.gson.annotations.SerializedName

data class MyInfoResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("data")
    val data: UserData
)

data class UserData(
    @SerializedName("nickname")
    val nickname: String?,

    @SerializedName("profileImageUrl")
    val profileImageUrl: String?,

    @SerializedName("fcmToken")
    val fcmToken: String?
)