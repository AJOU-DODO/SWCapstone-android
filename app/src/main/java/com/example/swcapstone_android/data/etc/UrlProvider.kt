package com.example.swcapstone_android.data.etc

import android.content.Context

object UrlProvider {
    private var prefs: android.content.SharedPreferences? = null

    // 앱 시작 시 Application Context를 통해 딱 한 번만 호출
    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("dodo_settings", Context.MODE_PRIVATE)
        }
    }

    val baseUrl: String
        get() {
            val isDevMode = prefs?.getBoolean("dev_mode", false) ?: false
            val customUrl = prefs?.getString("custom_web_url", "") ?: ""

            return if (isDevMode && customUrl.isNotEmpty()) {
                customUrl.trimEnd('/')
            } else {
                com.example.swcapstone_android.BuildConfig.WEB_URL.trimEnd('/')
            }
        }
}