package com.example.swcapstone_android.data.model

import com.google.gson.annotations.SerializedName

data class InterestResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("data")
    val data: List<InterestData>
)
data class InterestData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("createdAt")
    val createdAt: String
)