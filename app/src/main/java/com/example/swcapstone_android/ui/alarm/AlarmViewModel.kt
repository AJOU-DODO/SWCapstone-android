package com.example.swcapstone_android.ui.alarm

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.etc.UrlProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application, mockTokenManager: TokenManager) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    var alarmUrl by mutableStateOf("")
        private set

    var accessTokenCache: String = ""
        private set

    fun initData(nestId: String, type: String?) {
        viewModelScope.launch {
            accessTokenCache = tokenManager.accessToken.first() ?: ""

            when (type) {
                "POSTCARD" -> {
                    alarmUrl = "${UrlProvider.baseUrl}/mypage/posts?tab=sent"
                }
                else -> {
                    alarmUrl = "${UrlProvider.baseUrl}/nests/$nestId"
                }
            }
            Log.d("ALARM_VIEWMODEL", "최종 브릿지 웹뷰 URL 매칭 완료 -> $alarmUrl")
        }
    }
}