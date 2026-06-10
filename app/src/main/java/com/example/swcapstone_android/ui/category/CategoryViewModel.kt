package com.example.swcapstone_android.ui.category

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.etc.UrlProvider

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    val accessToken = tokenManager.accessToken

    fun getMypageUrl(): String {
        return "${UrlProvider.baseUrl}/category"
    }
}