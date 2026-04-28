package com.example.swcapstone_android.data.bridge

import android.util.Log
import android.webkit.JavascriptInterface
import org.json.JSONObject

class WriteBridge(
    private val onImageRequest: () -> Unit,
    private val onPublishRequest: (String) -> Unit
) {
    private var accessToken: String? = null
    private var lat: Double = 0.0
    private var lng: Double = 0.0

    // 데이터를 나중에 주입하는 함수
    fun setData(token: String? = null, lat: Double? = null, lng: Double? = null) {
        token?.let { this.accessToken = it }
        lat?.let { this.lat = it }
        lng?.let { this.lng = it }
        Log.d("WriteBridge", "데이터 업데이트됨: ${accessToken?.take(5)}, $lat, $lng")
    }

    @JavascriptInterface
    fun getAccessToken(): String {
        Log.d("WriteBridge", "데이터 가져감")
        return accessToken ?: ""
    }

    @JavascriptInterface
    fun getLocation(): String {
        val json = JSONObject().apply {
            put("latitude", lat)
            put("longitude", lng)
        }
        Log.d("WriteBridge", "위치 가져감")
        return json.toString()
    }

    @JavascriptInterface
    fun requestImage() {
        onImageRequest()
    }

    @JavascriptInterface
    fun publish(data: String) {
        onPublishRequest(data)
    }
}