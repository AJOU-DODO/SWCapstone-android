package com.example.swcapstone_android.data.remote

import com.example.swcapstone_android.data.model.PresignedResponse
import com.example.swcapstone_android.data.model.ProfileRequest
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // 1. Presigned URL 발급 (GET)
    @GET("/api/v1/files/presigned-url/profile")
    suspend fun getPresignedUrl(
        @Header("Authorization") token: String,
        @Query("fileName") fileName: String
    ): Response<PresignedResponse> // PresignedResponse는 아래 별도 정의

    // 2. 프로필 정보 저장 (POST)
    @POST("/api/v1/users/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body profile: ProfileRequest
    ): Response<Unit>

    @GET("/api/v1/users/me")
    suspend fun getMyInfo(
        @Header("Authorization") token: String
    ): Response<ProfileRequest>
}

// S3 업로드를 위한 별도 인터페이스
interface S3Service {
    @PUT
    suspend fun uploadImage(
        @Url url: String,
        @Body body: RequestBody,
        @Header("Content-Type") contentType: String
    ): Response<Unit>
}