package com.example.swcapstone_android.ui.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.swcapstone_android.BuildConfig
import com.example.swcapstone_android.data.TokenManager

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    val accessToken = tokenManager.accessToken

    fun getMypageUrl(): String {
        return "${BuildConfig.WEB_URL}/category"
    }
}