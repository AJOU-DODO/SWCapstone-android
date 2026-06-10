package com.example.swcapstone_android.ui.unlock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.etc.UrlProvider

class UnlockViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    val accessToken = tokenManager.accessToken

    // 둥지 상세 URL 생성
    fun getNestUrl(nestId: Long): String {
        return "${UrlProvider.baseUrl}/nests/$nestId"
    }
}