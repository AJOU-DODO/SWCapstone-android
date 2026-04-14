package com.example.swcapstone_android.ui.splash

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.remote.RetrofitClient
import com.example.swcapstone_android.ui.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)

    // 네비게이션 목적지 상태
    var startDestination by mutableStateOf("login")

    fun checkLoginStatus(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val token = tokenManager.accessToken.first()
            Log.d("Splash", "읽어온 토큰: $token")

            if (token == null) {
                onResult(Screen.LoginScreen.route) // 토큰 없으면 로그인
                return@launch
            }

            try {
                val response = RetrofitClient.instance.getMyInfo("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!.data
                    Log.d("Splash", "서버 응답 성공! 닉네임: ${user.nickname}")
                    if (user.nickname.isNullOrBlank()) {
                        onResult(Screen.DetailScreen.route) // 로그인했지만 프로필 없으면 설정
                    } else {
                        onResult(Screen.HomeScreen.route) // 둘 다 있으면 메인
                    }
                } else {
                    Log.e("Splash", "서버 응답 실패: 코드 ${response.code()}, 메시지 ${response.message()}")
                    onResult(Screen.LoginScreen.route) //에러 시 로그인
                }
            } catch (e: Exception) {
                Log.e("Splash", "예외 발생: ${e.message}")
                onResult(Screen.LoginScreen.route)
            }
        }
    }
}
