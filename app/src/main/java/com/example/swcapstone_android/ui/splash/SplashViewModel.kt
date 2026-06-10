package com.example.swcapstone_android.ui.splash

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.DeviceRequest
import com.example.swcapstone_android.data.model.ReissueRequest
import com.example.swcapstone_android.data.remote.RetrofitClient
import com.example.swcapstone_android.ui.Screen
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SplashViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val tokenManager = TokenManager(application)
    private val geofenceManager = com.example.swcapstone_android.util.GeofenceManager(application)

    fun checkGpsSecurity(): String {
        // Fake GPS 패키지 블랙리스트 검사
        if (isFakeGpsAppInstalled()) {
            return "FAKE_APP"
        }

        // 모의 위치(Mock Location) 설정 검사
        if (isMockLocationEnabled()) {
            return "MOCK_LOCATION"
        }

        return "NORMAL"
    }

    private fun isFakeGpsAppInstalled(): Boolean {
        val pm = context.packageManager
        val fakeApps = listOf(
            "com.lexa.fakegps",                // Fake GPS Location
            "com.theappninjas.fakegpsjoystick", // GPS Joystick
            "com.incorporateapps.fakegps.fre",  // Fake GPS Location Spoofer
        )

        for (packageName in fakeApps) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(packageName, 0)
                }
                Log.w("SplashSecurity", "⚠️ 조작 앱 발견: $packageName")
                return true
            } catch (e: PackageManager.NameNotFoundException) {

            }
        }
        return false
    }

    // 개발자 옵션의 모의 위치 시스템
    private fun isMockLocationEnabled(): Boolean {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // GPS 및 네트워크 프로바이더 둘 다 검사
            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                val location = locationManager.getLastKnownLocation(provider) ?: continue

                // 안드로이드 11(API 31) 이상 표준 검증 방식
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (location.isMock) return true
                } else {
                    // 구버전 안드로이드 감지 방식
                    if (location.extras != null && location.extras?.getBoolean("mockLocation", false) == true) {
                        return true
                    }
                }
            }
            false
        } catch (e: SecurityException) {
            Log.w("SplashSecurity", "위치 권한이 없어 모의 위치 검사를 스킵합니다 (SecurityException catch 완료)")
            false
        } catch (e: Exception) {
            Log.e("SplashSecurity", "모의 위치 검사 중 일반 에러 발생: ${e.message}")
            false
        }
    }

    fun checkLoginStatus(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                geofenceManager.removeAllGeofences()
                Log.d("Splash", "이전 지오펜스 데이터 초기화 완료")
            } catch (e: Exception) {
                Log.e("Splash", "지오펜스 초기화 실패: ${e.message}")
            }

            val token = tokenManager.accessToken.first()
            val refreshToken = tokenManager.refreshToken.first()
            Log.d("Splash", "읽어온 토큰: $token")

            if (token == null || refreshToken == null) {
                onResult(Screen.LoginScreen.route) // 토큰 없으면 로그인
                return@launch
            }

            try {
                val request = ReissueRequest(refreshToken = refreshToken)
                val responseR = RetrofitClient.instance.reissueToken(request)
                Log.d("Splash", "response: ${responseR.body()!!.data.accessToken}")
                if (responseR.isSuccessful && responseR.body() != null) {
                    val token = responseR.body()!!.data
                    tokenManager.saveTokens(
                        token.accessToken,
                        token.refreshToken
                    )
                    registerFcmToken(token.accessToken)

                    Log.d("Splash", "서버 응답 및 FCM 등록 성공!")
                    if (!token.onboarded) {
                        onResult(Screen.DetailScreen.route) // 로그인했지만 프로필 없으면 설정
                    } else {
                        onResult(Screen.HomeScreen.route) // 둘 다 있으면 메인
                    }
                } else {
                    Log.e("Splash", "서버 응답 실패: 코드 ${responseR.code()}, 메시지 ${responseR.message()}")
                    onResult(Screen.LoginScreen.route) //에러 시 로그인
                }
            } catch (e: Exception) {
                Log.e("Splash", "예외 발생: ${e.message}")
                onResult(Screen.LoginScreen.route)
            }
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