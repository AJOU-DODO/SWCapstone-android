package com.example.swcapstone_android.data.remote

import android.content.Context
import com.example.swcapstone_android.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private lateinit var applicationContext: Context

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
            .create(ApiService::class.java)
    }

    // S3 업로드용
    val s3Instance: S3Service by lazy {
        Retrofit.Builder()
            .baseUrl("https://dummy.url/") // @Url을 쓰므로 아무 주소나
            .client(okHttpClient)
            .build()
            .create(S3Service::class.java)
    }

    private val osmHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // OSM 전용 인스턴스
    val osmInstance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://overpass-api.de/")
            .client(osmHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}