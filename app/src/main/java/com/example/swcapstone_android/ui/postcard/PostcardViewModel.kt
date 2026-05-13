package com.example.swcapstone_android.ui.postcard

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class PostcardViewModel : ViewModel() {
    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    var message by mutableStateOf("")
        private set

    fun updateImage(uri: Uri?) {
        selectedImageUri = uri
    }

    fun updateMessage(text: String) {
        message = text
    }

    fun sendPostcard() {
        // 서버 전송 로직 (Multipart 등을 활용해 이미지와 텍스트 전송)
        Log.d("Postcard", "전송 시작: URI=$selectedImageUri, Msg=$message")
    }
}