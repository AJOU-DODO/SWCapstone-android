package com.example.swcapstone_android.ui.inquiryhistory

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.InquiryItem
import com.example.swcapstone_android.data.model.NoticeItem
import com.example.swcapstone_android.data.remote.ApiService
import com.example.swcapstone_android.data.remote.RetrofitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class InquiryHistoryViewModel(
    application: Application,
    private val tokenManager: TokenManager = TokenManager(application),
    private val apiService: ApiService = RetrofitClient.instance  // ApiService는 실제 인터페이스명으로 교체
) : AndroidViewModel(application) {

    // 관찰 가능한 컴포즈 전용 리스트 가방
    var inquiryList = mutableStateListOf<InquiryItem>()
        private set

    var noticeList = mutableStateListOf<NoticeItem>()
        private set

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        fetchMyInquiries()
    }

    fun refresh(isNoticeTab: Boolean) {
        if (isNoticeTab) {
            fetchNotices()
        } else {
            fetchMyInquiries()
        }
    }

    fun fetchNotices() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val token = tokenManager.accessToken.first()
                if (token != null) {
                    val response = apiService.getNotices("Bearer $token")
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()
                        if (body?.status == "SUCCESS" || body?.status == "200") { // 백엔드 성공 규격 매칭
                            noticeList.clear()
                            noticeList.addAll(body.data.content)
                        } else {
                            errorMessage = body?.message ?: "데이터 형식 오류가 발생했습니다."
                        }
                    } else {
                        errorMessage = "공지사항을 불러오지 못했습니다. (${response.code()})"
                    }
                } else {
                    errorMessage = "로그인 세션이 만료되었습니다."
                }
            } catch (e: Exception) {
                errorMessage = "네트워크 오류가 발생했습니다."
                Log.e("NoticeVM", "Fetch Notice Error", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchMyInquiries() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val token = tokenManager.accessToken.first()
                if (token != null) {
                    val authHeader = "Bearer $token"
                    val response = apiService.getMyInquiries(authHeader)

                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()
                        if (body?.status == "SUCCESS") {
                            inquiryList.clear()
                            inquiryList.addAll(body.data.content)
                            Log.d("InquiryHistoryVM", "내 문의 내역 로드 완료: ${inquiryList.size}개")
                        } else {
                            errorMessage = body?.message ?: "데이터 형식 오류가 발생했습니다."
                        }
                    } else {
                        errorMessage = "내역을 불러오지 못했습니다. (에러 코드: ${response.code()})"
                        Log.e("InquiryHistoryVM", "API Failure Code: ${response.code()}")
                    }
                } else {
                    errorMessage = "로그인 세션이 만료되었습니다. 다시 로그인해 주세요."
                }
            } catch (e: Exception) {
                errorMessage = "네트워크 오류가 발생했습니다: ${e.localizedMessage}"
                Log.e("InquiryHistoryVM", "Fetch Exception", e)
            } finally {
                isLoading = false
            }
        }
    }
}