package com.example.swcapstone_android.data.bridge

import android.webkit.JavascriptInterface
import android.util.Log

class MypageBridge(private val accessToken: String?,
                   private val onImageRequest: () -> Unit,
                   private val onPostcardRequest: () -> Unit
) {
    @JavascriptInterface
    fun getAccessToken(): String {
        Log.d("UnlockBridge", "데이터 가져감+${accessToken}")
        return accessToken ?: ""
    }

    @JavascriptInterface
    fun requestImageUpload() {
        Log.d("WriteBridge", "사진 요청")
        onImageRequest()
    }

    @JavascriptInterface
    fun requestPostcardMake() {
        onPostcardRequest()
    }
}