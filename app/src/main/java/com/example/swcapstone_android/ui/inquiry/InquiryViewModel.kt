package com.example.swcapstone_android.ui.inquiry

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.InquiryRequest
import com.example.swcapstone_android.data.model.InquiryType
import com.example.swcapstone_android.data.remote.ApiService
import com.example.swcapstone_android.data.remote.RetrofitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class InquiryViewModel @JvmOverloads constructor(
    application: Application,
    private val tokenManager: TokenManager = TokenManager(application),
    private val apiService: ApiService = RetrofitClient.instance
) : AndroidViewModel(application) {

    // UI 상태 관리 (유형 추가 🌟)
    var selectedType by mutableStateOf(InquiryType.SUGGESTION)
    var title by mutableStateOf("")
    var content by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun submitInquiry(onSuccess: () -> Unit) {
        if (title.isBlank() || content.isBlank()) {
            errorMessage = "제목과 내용을 모두 입력해주세요."
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val token = tokenManager.accessToken.first()
                if (token != null) {
                    // 🌟 백엔드 명세대로 type 성분을 DTO에 얹어서 전송!
                    val request = InquiryRequest(
                        type = selectedType.name, // "SUGGESTION" 문자열로 변환
                        title = title,
                        content = content
                    )
                    Log.d("INQUIRY_API_DEBUG", "📍 Type    : ${selectedType.name}")
                    Log.d("INQUIRY_API_DEBUG", "📍 Title   : $title")
                    Log.d("INQUIRY_API_DEBUG", "📍 Content : $content")
                    val response = apiService.createInquiry("Bearer $token", request)

                    if (response.isSuccessful) {
                        isSuccess = true
                        onSuccess()
                    } else {
                        errorMessage = "전송에 실패했습니다. (에러 코드: ${response.code()})"
                    }
                } else {
                    errorMessage = "로그인 정보가 유효하지 않습니다."
                }
            } catch (e: Exception) {
                errorMessage = "네트워크 오류가 발생했습니다: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}