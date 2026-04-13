package com.example.swcapstone_android.ui.userdetail

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import android.util.Log
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.ProfileRequest
import com.example.swcapstone_android.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class UserDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val contentResolver = application.contentResolver

    var selectedImageUri by mutableStateOf<Uri?>(null)
    var nickname by mutableStateOf("")
    var showDialog by mutableStateOf(false)

    fun onStartClick(onSuccess: () -> Unit) {
        // 유효성 검사
        if (nickname.isBlank() || selectedImageUri == null) {
            showDialog = true
        } else {
            saveProfileProcess(onSuccess)
        }
    }

    private fun saveProfileProcess(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val token = tokenManager.accessToken.first() ?: return@launch
                val authHeader = "Bearer $token"
                val fileName = "profile_${System.currentTimeMillis()}.jpg"

                // Presigned URL 요청
                val presignedRes = RetrofitClient.instance.getPresignedUrl(authHeader, fileName)
                if (presignedRes.isSuccessful && presignedRes.body() != null) {
                    val data = presignedRes.body()!!.data

                    // S3에 이미지 업로드
                    val isUploadSuccess = uploadToS3(data.presignedUrl, selectedImageUri!!)

                    if (isUploadSuccess) {
                        // 최종 프로필 정보 서버 전송
                        val profileRequest = ProfileRequest(
                            nickname = nickname,
                            fcmToken = "임시fcm",
                            profileImageUrl = data.fileUrl
                        )

                        val postRes = RetrofitClient.instance.updateProfile(authHeader, profileRequest)
                        if (postRes.isSuccessful) {
                            withContext(Dispatchers.Main) {
                                onSuccess()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("UserDetail", "Error: ${e.message}")
            }
        }
    }

    private suspend fun uploadToS3(url: String, uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@withContext false
                inputStream.close()

                val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                // S3 전용 인스턴스 사용
                val response = RetrofitClient.s3Instance.uploadImage(url, requestBody, "image/jpeg")
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
}