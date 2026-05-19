package com.example.swcapstone_android.data.remote

import com.example.swcapstone_android.data.model.CommonResponse
import com.example.swcapstone_android.data.model.DeviceRequest
import com.example.swcapstone_android.data.model.MyInfoResponse
import com.example.swcapstone_android.data.model.OsmResponse
import com.example.swcapstone_android.data.model.NestDetailResponse
import com.example.swcapstone_android.data.model.PinResponse
import com.example.swcapstone_android.data.model.PostcardRequest
import com.example.swcapstone_android.data.model.PostcardResponse
import com.example.swcapstone_android.data.model.PresignedResponse
import com.example.swcapstone_android.data.model.ProfileRequest
import com.example.swcapstone_android.data.model.ReissueRequest
import com.example.swcapstone_android.data.model.TokenResponse
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
    ): Response<MyInfoResponse>

    @POST("/api/v1/auth/reissue")
    suspend fun reissueToken(
        @Body request: ReissueRequest
    ): Response<TokenResponse>

    @GET("/api/v1/nests/pins")
    suspend fun getNearbyPins(
        @Header("Authorization") token: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<PinResponse>

    @POST("/api/v1/devices")
    suspend fun registerDevice(
        @Header("Authorization") token: String,
        @Body request: DeviceRequest
    ): Response<Unit>

    @GET("/api/v1/nests/{id}")
    suspend fun getNestDetail(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long
    ): Response<NestDetailResponse> // NestDetailResponse는 아래 제공된 JSON 구조에 맞춘 모델

    // 둥지 해금 요청
    @POST("/api/v1/nests/{id}/unlock")
    suspend fun unlockNest(
        @Header("Authorization") authHeader: String,
        @Path("id") id: Long,
        @Body location: Map<String, Double> // latitude, longitude
    ): Response<CommonResponse>
    @POST("/api/v1/postcards")
    suspend fun createPostcard(
        @Header("Authorization") authHeader: String,
        @Body request: PostcardRequest
    ): Response<PostcardResponse>

    @GET("https://overpass-api.de/api/interpreter")
    suspend fun getOsmWalkingPaths(
        @Query("data") query: String
    ): Response<OsmResponse>
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