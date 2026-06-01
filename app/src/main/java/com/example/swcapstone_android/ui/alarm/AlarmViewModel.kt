package com.example.swcapstone_android.ui.alarm

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.etc.UrlProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)

    var alarmUrl by mutableStateOf("")
        private set

    var accessTokenCache: String = ""
        private set

    fun initData(nestId: String) {
        viewModelScope.launch {
            accessTokenCache = tokenManager.accessToken.first() ?: ""
            alarmUrl = "${UrlProvider.baseUrl}/nests/$nestId"
        }

    }
}