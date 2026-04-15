package com.example.swcapstone_android.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.UserData
import com.example.swcapstone_android.data.remote.RetrofitClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)

    // 서버에서 받아온 유저 정보를 담을 상태
    var userData by mutableStateOf<UserData?>(null)
    var isLoading by mutableStateOf(false)

    fun fetchMyInfo() {
        viewModelScope.launch {
            isLoading = true
            try {
                val token = tokenManager.accessToken.first()
                if (token != null) {
                    val response = RetrofitClient.instance.getMyInfo("Bearer $token")
                    if (response.isSuccessful && response.body() != null) {
                        userData = response.body()!!.data
                    }
                }
            } catch (e: Exception) {
                // 에러 처리
            } finally {
                isLoading = false
            }
        }
    }
}