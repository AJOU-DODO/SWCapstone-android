package com.example.swcapstone_android.data.bridge

import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONArray

class WebBridge(private val onNestSelected: (Long) -> Unit) {
    private var accessToken: String? = null
    private var nestIds: List<Long> = emptyList()

    fun setData(token: String? = null, ids: List<Long> = emptyList()) {
        token?.let {
            this.accessToken = it
            Log.d("WebBridge", "Token 세팅됨: ${it.take(10)}...")
        }
        ids.let {
            this.nestIds = it
            Log.d("WebBridge", "NestIds 세팅됨: $it")
        }
    }

    @JavascriptInterface
    fun getAccessToken(): String {
        Log.d("WebBridge", "JS가 토큰 요청함")
        return accessToken ?: ""
    }

    @JavascriptInterface
    fun getNestIds(): String {
        val jsonArrayString = JSONArray(nestIds).toString()
        Log.d("WebBridge", "JS가 NestIds 가져감: $jsonArrayString")
        return jsonArrayString
    }

    @JavascriptInterface
    fun sendNestIdSelected(id: Long) {
        Log.d("WebBridge", "웹에서 선택된 Nest ID: $id")
        onNestSelected(id)
    }
}