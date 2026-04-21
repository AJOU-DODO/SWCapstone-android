package com.example.swcapstone_android.data.bridge

import android.webkit.JavascriptInterface

class WebBridge {
    private var accessToken: String? = null
    private var nestId: Long? = null

    fun setData(token: String? = null, id: Long? = null) {
        token?.let { this.accessToken = it }
        id?.let { this.nestId = it }
    }

    @JavascriptInterface
    fun getAccessToken(): String = accessToken ?: ""

    @JavascriptInterface
    fun getNestId(): Long = nestId ?: 0L
}