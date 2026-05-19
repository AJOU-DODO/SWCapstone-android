package com.example.swcapstone_android.ui.setting

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.core.content.edit

class SettingViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("dodo_settings", Context.MODE_PRIVATE)

    // 카테고리 설정 상태 (기본값: true)
    var isCategoryEnabled by mutableStateOf(sharedPrefs.getBoolean("category_enabled", true))
        private set

    // 반경 설정 상태 (기본값: 2000 - 보통)
    var searchRadius by mutableStateOf(sharedPrefs.getInt("search_radius", 2000))
        private set

    var isDevMode by mutableStateOf(sharedPrefs.getBoolean("dev_mode", false))
        private set
    var customWebUrl by mutableStateOf(sharedPrefs.getString("custom_web_url", "") ?: "")
        private set

    // 카테고리 설정 변경 및 저장
    fun toggleCategory(enabled: Boolean) {
        isCategoryEnabled = enabled
        sharedPrefs.edit { putBoolean("category_enabled", enabled) }
    }

    // 반경 설정 변경 및 저장
    fun updateRadius(radius: Int) {
        searchRadius = radius
        sharedPrefs.edit { putInt("search_radius", radius) }
    }

    fun toggleDevMode(enabled: Boolean) {
        isDevMode = enabled
        sharedPrefs.edit { putBoolean("dev_mode", enabled) }
    }

    // [추가] 커스텀 URL 업데이트
    fun updateCustomUrl(url: String) {
        customWebUrl = url
        sharedPrefs.edit { putString("custom_web_url", url) }
    }
}