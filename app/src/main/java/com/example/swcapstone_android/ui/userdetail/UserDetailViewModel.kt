package com.example.swcapstone_android.ui.userdetail

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swcapstone_android.data.TokenManager
import kotlinx.coroutines.launch

class UserDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)

    // 닉네임 입력 상태
    var nickname by mutableStateOf("")

    fun saveProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // TODO: 여기서 서버 API(POST /api/user/profile 등)를 호출해서 닉네임 저장
            // 지금은 토큰이 잘 있는지 확인하는 용도로 로그만 찍을게

            // 성공했다고 가정하고 메인으로 이동
            onSuccess()
        }
    }
}