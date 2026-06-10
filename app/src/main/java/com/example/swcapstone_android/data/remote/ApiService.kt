package com.example.swcapstone_android.data.remote

import com.example.swcapstone_android.data.model.CommonResponse
import com.example.swcapstone_android.data.model.DeviceRequest
import com.example.swcapstone_android.data.model.InquiryRequest
import com.example.swcapstone_android.data.model.InquiryResponse
import com.example.swcapstone_android.data.model.InterestResponse
import com.example.swcapstone_android.data.model.NestDetailResponse
import com.example.swcapstone_android.data.model.NoticeResponse
import com.example.swcapstone_android.data.model.OsmResponse
import com.example.swcapstone_android.data.model.PinResponse
import com.example.swcapstone_android.data.model.PostcardRequest
import com.example.swcapstone_android.data.model.PostcardResponse
import com.example.swcapstone_android.data.model.PresignedResponse
import com.example.swcapstone_android.data.model.ProfileRequest
import com.example.swcapstone_android.data.model.ReissueRequest
import com.example.swcapstone_android.data.model.TokenResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

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

    @POST("/api/v1/auth/reissue")
    suspend fun reissueToken(
        @Body request: ReissueRequest
    ): Response<TokenResponse>

    @GET("/api/v1/nests/pins")
    suspend fun getNearbyPins(
        @Header("Authorization") token: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radiusMeter") radiusMeter: Int,
        @Query("categoryIds") categoryIds: List<Int>? = null
    ): Response<PinResponse>

    @GET("api/v1/nests/ad-pins")
    suspend fun getAdPins(
        @Header("Authorization") token: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radiusMeter") radiusMeter: Int,
        @Query("categoryIds") categoryIds: List<Int>? = null
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
    ): Response<NestDetailResponse>

    @GET("/api/v1/users/interests")
    suspend fun getUserInterests(
        @Header("Authorization") token: String
    ): Response<InterestResponse>

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

    @FormUrlEncoded
    @Headers(
        "Accept: application/json",
        "User-Agent: MyCapstoneApp/1.0"
    )
    @POST("api/interpreter") // URL 전체가 아닌 경로만 지정 (osmInstance에 베이스 주소가 있으니)
    suspend fun getOsmWalkingPaths(
        @Field("data") query: String // encoded=true 없이 @Field 사용
    ): Response<OsmResponse>

    @POST("/api/v1/inquiries")
    suspend fun createInquiry(
        @Header("Authorization") authHeader: String,
        @Body request: InquiryRequest
    ): Response<Unit>

    @GET("/api/v1/inquiries/me")
    suspend fun getMyInquiries(
        @Header("Authorization") token: String
    ): Response<InquiryResponse>

    @GET("/api/v1/notices")
    suspend fun getNotices(
        @Header("Authorization") token: String
    ): Response<NoticeResponse>
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