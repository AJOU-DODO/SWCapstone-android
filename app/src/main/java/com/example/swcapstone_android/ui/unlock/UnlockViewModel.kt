package com.example.swcapstone_android.ui.unlock

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.swcapstone_android.BuildConfig
import com.example.swcapstone_android.data.TokenManager

class UnlockViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    val accessToken = tokenManager.accessToken

    // 둥지 상세 URL 생성
    fun getNestUrl(nestId: Long): String {
        return "${BuildConfig.WEB_URL}/nests/$nestId"
    }
}