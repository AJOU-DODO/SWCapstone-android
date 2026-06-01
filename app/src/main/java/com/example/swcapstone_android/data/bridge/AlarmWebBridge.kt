package com.example.swcapstone_android.data.bridge

import android.webkit.JavascriptInterface

class AlarmWebBridge(private val getTokenBlock: () -> String) {

    @JavascriptInterface
    fun getAccessToken(): String {
        return getTokenBlock()
    }
}