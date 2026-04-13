package com.example.swcapstone_android.data.remote

import com.example.swcapstone_android.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.javaClass

object RetrofitClient {
    private const val BASE_URL = BuildConfig.BASE_URL

    // 로그 확인을 위한 인터셉터
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // 일반 서버 통신용
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java) // ⚠️ 사용 중인 서비스 인터페이스에 맞게 수정 가능
    }

    // S3 업로드용 (Base URL이 무의미하므로 별도로 관리하면 편함)
    val s3Instance: S3Service by lazy {
        Retrofit.Builder()
            .baseUrl("https://dummy.url/") // @Url을 쓰므로 아무 주소나 넣어도 됨
            .client(okHttpClient)
            .build()
            .create(S3Service::class.java)
    }
}