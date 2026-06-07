package com.example.swcapstone_android.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swcapstone_android.BuildConfig
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.DeviceRequest
import com.example.swcapstone_android.data.remote.RetrofitClient
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    val loginUrl = "${BuildConfig.BASE_URL}/oauth2/authorization/google"
    private val tokenManager = TokenManager(application)

    fun handleLoginResult(jsonString: String, onSuccess: (Boolean) -> Unit) {
        try {
            val cleanJson = jsonString.removeSurrounding("\"").replace("\\\"", "\"")
            val response = Gson().fromJson(cleanJson, LoginResponse::class.java)

            if (response.status == "SUCCESS" && response.data != null) {
                viewModelScope.launch {
                    tokenManager.saveTokens(
                        response.data.accessToken,
                        response.data.refreshToken
                    )
                    Log.d("Login", "토큰 저장 완료!")
                    onSuccess(response.data.onboarded)  // FCM 기다리지 않고 바로 호출
                }

                // FCM 등록은 별도 코루틴으로 분리 (화면 전환 블로킹 안 함)
                viewModelScope.launch {
                    registerFcmToken(response.data.accessToken)
                }
            }
        } catch (e: Exception) {
            Log.e("Login", "JSON 파싱 에러: ${e.message}")
        }
    }

    private suspend fun registerFcmToken(accessToken: String) {
        try {
            // Firebase에서 현재 토큰 가져오기 (Task를 suspend로 변환)
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            Log.d("Splash", "현재 FCM 토큰: $fcmToken")

            val deviceRequest = DeviceRequest(
                fcmToken = fcmToken,
                deviceType = "ANDROID"
            )

            // API 호출 (Bearer 토큰 포함)
            val response = RetrofitClient.instance.registerDevice(
                token = "Bearer $accessToken",
                request = deviceRequest
            )

            if (response.isSuccessful) {
                Log.d("Splash", "기기 등록 성공")
            } else {
                Log.e("Splash", "기기 등록 실패: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("Splash", "FCM 등록 중 에러: ${e.message}")
        }
    }
}

// 응답 데이터 모델
data class LoginResponse(val status: String, val data: TokenData?, val message: String?)
data class TokenData(val accessToken: String, val refreshToken: String, val accessTokenExpiresIn: Int, val onboarded: Boolean)