package com.example.swcapstone_android.data.bridge

import android.util.Log
import android.webkit.JavascriptInterface

class CategoryBridge(private val accessToken: String?) {
    @JavascriptInterface
    fun getAccessToken(): String {
        Log.d("CategoryBridge", "데이터 가져감+${accessToken}")
        return accessToken ?: ""
    }
}