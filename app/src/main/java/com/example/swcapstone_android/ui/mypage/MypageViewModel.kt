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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class MypageViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    val accessToken = tokenManager.accessToken

    private var _jsCommand = mutableStateOf<String?>(null)
    val jsCommand: String? get() = _jsCommand.value

    private var webView: WebView? = null

    fun getMypageUrl(): String {
        return "${BuildConfig.WEB_URL}/mypage"
    }

    fun clearJsCommand() {
        _jsCommand.value = null
    }

    fun requestWebReload() {
        val script = "window.requestReload()"
        _jsCommand.value = script
        Log.d("MypageVM", "새로고침 스크립트 전달")
    }

    fun getOrCreateWebView(context: android.content.Context, bridge: Any, url: String): WebView {
        if (webView == null) {
            webView = WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                addJavascriptInterface(bridge, "AndroidBridge")
                loadUrl(url)
            }
        }
        return webView!!
    }

    override fun onCleared() {
        super.onCleared()
        webView?.removeAllViews()
        webView?.destroy()
        webView = null
    }

    fun handleImageSelection(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) { // 백그라운드 스레드에서 처리
            try {
                val dataUri = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return@use null

                    val exifInputStream = context.contentResolver.openInputStream(uri)
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
                        originalBitmap.recycle() // 원본은 메모리에서 해제
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
                    Log.d("WriteVM", "전달할 스크립트 길이: ${script.length}")
                }
            } catch (e: Exception) {
                Log.e("WriteVM", "이미지 변환 실패: ${e.message}")
            }
        }
    }
}