package com.example.swcapstone_android.ui.write

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.swcapstone_android.BuildConfig
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swcapstone_android.data.bridge.WriteBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class WriteViewModel(application: Application) : AndroidViewModel(application) {

    private var _jsCommand = mutableStateOf<String?>(null)
    val jsCommand: String? get() = _jsCommand.value

    private val tokenManager = com.example.swcapstone_android.data.TokenManager(application)
    val accessToken = tokenManager.accessToken // Flow<String?>
    var writeUrl by mutableStateOf("${BuildConfig.WEB_URL}/nest-editor")
        private set

    // 웹뷰 로딩 상태 관리
    var isLoading by mutableStateOf(true)
        private set

    fun updateLoading(loading: Boolean) {
        isLoading = loading
    }

    fun clearJsCommand() { _jsCommand.value = null }

    fun handleImageSelection(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) { // 백그라운드 스레드에서 처리
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                // 1. 이미지 압축 (성능과 전송 속도를 위해 필수)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                val byteArray = outputStream.toByteArray()

                // 2. Base64 변환
                val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                val dataUri = "data:image/jpeg;base64,$base64String"

                // 3. 웹뷰 JS 호출 명령어 준비 (메인 스레드에서 UI 업데이트)
                withContext(Dispatchers.Main) {
                    _jsCommand.value = "javascript:window.onImageReceived('$dataUri')"
                }
            } catch (e: Exception) {
                Log.e("WriteVM", "이미지 변환 실패: ${e.message}")
            }
        }
    }
}