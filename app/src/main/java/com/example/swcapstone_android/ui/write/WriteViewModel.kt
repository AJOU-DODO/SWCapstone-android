package com.example.swcapstone_android.ui.write

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.swcapstone_android.BuildConfig

class WriteViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = com.example.swcapstone_android.data.TokenManager(application)
    val accessToken = tokenManager.accessToken // Flow<String?>
    var writeUrl by mutableStateOf("${BuildConfig.WEB_URL}/nest-editor")
        private set

    // 웹뷰 로딩 상태 관리
    var isLoading by mutableStateOf(true)
        private set

    fun updateLoading(loading: Boolean) {
        isLoading = loading
    }
}