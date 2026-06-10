package com.example.swcapstone_android.data.bridge

class MypageBridge(private val accessToken: String?,
                   private val onImageRequest: () -> Unit,
                   private val onPostcardRequest: () -> Unit
) {

    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

}