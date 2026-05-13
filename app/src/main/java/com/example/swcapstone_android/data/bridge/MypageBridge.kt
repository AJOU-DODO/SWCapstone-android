package com.example.swcapstone_android.data.bridge

import android.webkit.JavascriptInterface
import android.util.Log

class MypageBridge(private val accessToken: String?,
                   private val onImageRequest: () -> Unit,
                   private val onPostcardRequest: () -> Unit
) {

    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

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
        mainHandler.post {
            try {
                onPostcardRequest()
            } catch (e: Exception) {
                Log.e("MypageBridge", "엽서 화면 이동 실패: ${e.message}")
            }
        }
    }
}