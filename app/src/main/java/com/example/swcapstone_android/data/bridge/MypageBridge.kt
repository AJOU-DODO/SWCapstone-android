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
        Log.d("UnlockBridge", "사진 요청")
        onImageRequest()
    }

    @JavascriptInterface
    fun requestPostcardMake() {
        Log.d("UnlockBridge", "엽서 요청")
        onPostcardRequest()
    }
}