package com.example.swcapstone_android.ui.login

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swcapstone_android.BuildConfig
import com.example.swcapstone_android.data.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    val loginUrl = "${BuildConfig.BASE_URL}/api/v1/auth/google"
    private val tokenManager = TokenManager(application)

    fun handleLoginResult(jsonString: String, onSuccess: () -> Unit) {
        try {
            val cleanJson = jsonString.removeSurrounding("\"").replace("\\\"", "\"")
            val response = Gson().fromJson(cleanJson, LoginResponse::class.java)

            if (response.status == "SUCCESS" && response.data != null) {
                viewModelScope.launch {
                    // 토큰 저장
                    tokenManager.saveTokens(
                        response.data.accessToken,
                        response.data.refreshToken
                    )
                    Log.d("Login", "토큰 저장 완료!")
                    onSuccess()
                }
            }
        } catch (e: Exception) {
            Log.e("Login", "JSON 파싱 에러: ${e.message}")
        }
    }
}

// 응답 데이터 모델
data class LoginResponse(val status: String, val data: TokenData?, val message: String?)
data class TokenData(val accessToken: String, val refreshToken: String, val accessTokenExpiresIn: Int)