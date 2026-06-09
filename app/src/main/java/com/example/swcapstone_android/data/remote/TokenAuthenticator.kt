package com.example.swcapstone_android.data.remote

import android.content.Context
import com.example.swcapstone_android.BuildConfig
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.ReissueRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TokenAuthenticator(
    private val context: Context,
    private val tokenManager: TokenManager,
    private val baseUrl: String = BuildConfig.BASE_URL
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // 1. RefreshToken 가져오기
        val refreshToken = runBlocking { tokenManager.refreshToken.first() } ?: return null

        // 2. 새 토큰 요청
        val res = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        // 3. 재발급 API 호출
        val reissueRes = runBlocking {
            res.reissueToken(ReissueRequest(refreshToken = refreshToken))
        }

        return if (reissueRes.isSuccessful && reissueRes.body() != null) {
            val newData = reissueRes.body()!!.data
            // 4. 새 토큰 저장
            runBlocking { tokenManager.saveTokens(newData.accessToken, newData.refreshToken) }

            response.request.newBuilder()
                .header("Authorization", "Bearer ${newData.accessToken}")
                .build()
        } else {
            null
        }
    }
}