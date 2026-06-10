package com.example.swcapstone_android.data.bridge

import android.util.Log

class WriteBridge(
    private val onImageRequest: () -> Unit,
    private val onPublishRequest: (Int) -> Unit
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

}