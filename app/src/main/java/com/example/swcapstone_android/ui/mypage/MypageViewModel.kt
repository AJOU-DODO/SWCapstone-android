package com.example.swcapstone_android.ui.mypage

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.mutableStateOf
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swcapstone_android.BuildConfig
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.etc.UrlProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class MypageViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    val accessToken = tokenManager.accessToken

    private var _jsCommand = mutableStateOf<String?>(null)
    val jsCommand: String? get() = _jsCommand.value

    fun getMypageUrl(): String {
        return "${UrlProvider.baseUrl}/mypage"
    }

    fun clearJsCommand() {
        _jsCommand.value = null
    }

    fun requestWebReload() {
        val script = "window.requestReload()"
        _jsCommand.value = script
        Log.d("MypageVM", "새로고침 스크립트 전달")
    }

    fun handleImageSelection(uri: android.net.Uri) {
        val contentResolver = getApplication<Application>().contentResolver

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dataUri = contentResolver.openInputStream(uri)?.use { inputStream ->
                    val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return@use null

                    // Exif 읽기용 스트림 다시 열기
                    val exifInputStream = contentResolver.openInputStream(uri)
                    val orientation = exifInputStream?.use {
                        ExifInterface(it).getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                    } ?: ExifInterface.ORIENTATION_NORMAL

                    val degrees = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }

                    val finalBitmap = if (degrees != 0f) {
                        val matrix = Matrix().apply { postRotate(degrees) }
                        val rotated = Bitmap.createBitmap(
                            originalBitmap, 0, 0,
                            originalBitmap.width, originalBitmap.height,
                            matrix, true
                        )
                        originalBitmap.recycle()
                        rotated
                    } else {
                        originalBitmap
                    }

                    val outputStream = ByteArrayOutputStream()
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
                    finalBitmap.recycle()
                    val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                    "data:image/jpeg;base64,$base64String"
                } ?: return@launch

                withContext(Dispatchers.Main) {
                    val script = "window.onImageReceived('$dataUri')"
                    _jsCommand.value = script
                    Log.d("MypageVM", "전달할 스크립트 설정 완료")
                }
            } catch (e: Exception) {
                Log.e("MypageVM", "이미지 변환 실패: ${e.message}")
            }
        }
    }
}