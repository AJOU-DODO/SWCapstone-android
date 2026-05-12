package com.example.swcapstone_android.data.bridge

import android.webkit.JavascriptInterface
import android.util.Log

class MypageBridge(private val accessToken: String?) {
    @JavascriptInterface
    fun getAccessToken(): String {
        Log.d("UnlockBridge", "데이터 가져감+${accessToken}")
        return accessToken ?: ""
    }
}