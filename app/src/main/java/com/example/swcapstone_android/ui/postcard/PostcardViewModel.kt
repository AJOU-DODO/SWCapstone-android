package com.example.swcapstone_android.ui.postcard

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.PostcardRequest
import com.example.swcapstone_android.data.model.PostcardResponse
import com.example.swcapstone_android.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

open class PostcardViewModel @JvmOverloads constructor(
    application: Application,
    private val tokenManager: TokenManager = TokenManager(application)
) : AndroidViewModel(application) {

    private val context = application

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    var message by mutableStateOf("")
        private set

    var isUploading by mutableStateOf(false)
        private set

    fun updateImage(uri: Uri?) { selectedImageUri = uri }
    fun updateMessage(text: String) { message = text }

    fun sendPostcard(onSuccess: () -> Unit) {
        val uri = selectedImageUri ?: return

        viewModelScope.launch {
            isUploading = true
            try {
                val token = tokenManager.accessToken.first() ?: return@launch
                val authHeader = "Bearer $token"
                val fileName = "postcard_${System.currentTimeMillis()}.jpg"

                val presignedRes = RetrofitClient.instance.getPresignedUrl(authHeader, fileName)
                if (presignedRes.isSuccessful && presignedRes.body() != null) {
                    val data = presignedRes.body()!!.data

                    val isUploadSuccess = uploadToS3(data.presignedUrl, uri)

                    if (isUploadSuccess) {
                        val requestBody = PostcardRequest(
                            imageUrl = data.fileUrl,
                            content = message
                        )
                        val response = callCreatePostcard(authHeader, requestBody)
                        if (response.isSuccessful) {
                            Log.d("Postcard", "엽서 발행 성공!")
                            withContext(Dispatchers.Main) {
                                onSuccess()
                            }
                        } else {
                            Log.e("Postcard", "엽서 저장 실패: ${response.code()}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Postcard", "전송 중 오류 발생: ${e.message}")
            } finally {
                isUploading = false
            }
        }
    }

    internal open suspend fun uploadToS3(url: String, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@withContext false
                inputStream.close()
                val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                val response = RetrofitClient.s3Instance.uploadImage(url, requestBody, "image/jpeg")
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("Postcard", "S3 업로드 실패: ${e.message}")
                false
            }
        }
    }
    internal open suspend fun callCreatePostcard(
        authHeader: String,
        request: PostcardRequest
    ): Response<PostcardResponse> {
        return RetrofitClient.instance.createPostcard(authHeader, request)
    }
}