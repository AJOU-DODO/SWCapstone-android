package com.example.swcapstone_android.data.bridge

import android.util.Log

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

}