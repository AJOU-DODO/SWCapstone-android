package com.example.swcapstone_android.ui.home

import kotlinx.coroutines.flow.first
import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.PinData
import com.example.swcapstone_android.data.model.TokenData
import com.example.swcapstone_android.data.remote.RetrofitClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch


class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    // 현재 지도 카메라 상태
    var cameraPositionState by mutableStateOf<CameraPositionState>(CameraPositionState(
        position = CameraPosition.fromLatLngZoom(LatLng(37.5665, 126.9780), 15f)
    ))

    // 위치 권한 허용 여부
    var isLocationPermissionGranted by mutableStateOf(false)

    // 마커 데이터 (예시 데이터)
    var markers = mutableStateListOf<PinData>()
        private set

    var showBottomSheet by mutableStateOf(false)

    // 선택된 장소의
    var selectedUrl by mutableStateOf("https://www.google.com")

    fun onMarkerClick(position: LatLng) {
        // 핀을 누르면 호출될 함수
        selectedUrl = "https://www.google.com" // 지금은 구글로 고정
        showBottomSheet = true
    }

    fun updatePermissionStatus(granted: Boolean) {
        isLocationPermissionGranted = granted
    }

    fun fetchNearbyPins(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val token = tokenManager.accessToken.first() ?: ""
                val authHeader = "Bearer $token"

                val response = RetrofitClient.instance.getNearbyPins(authHeader, lat, lng)
                if (response.isSuccessful && response.body() != null) {
                    // 기존 마커 비우고 새로 추가
                    markers.clear()
                    response.body()?.data?.let { markers.addAll(it) }
                    Log.d("HomeVM", "핀 가져오기 성공: ${markers.size}개")
                } else {
                    Log.e("HomeVM", "핀 가져오기 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("HomeVM", "네트워크 오류: ${e.message}")
            }
        }
    }

    @SuppressLint("MissingPermission") // 호출 전 권한 체크를 하므로 억제
    fun fetchPinsAtUserLocation() {
        if (!isLocationPermissionGranted) return

        // 현재 기기의 정밀한 위치를 1회성으로 요청
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            location?.let {
                Log.d("HomeVM", "내 위치 확인: ${it.latitude}, ${it.longitude}")

                // 1. 카메라를 내 위치로 이동
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(it.latitude, it.longitude), 15f
                )

                // 2. 해당 좌표로 서버에 핀 요청
                fetchNearbyPins(it.latitude, it.longitude)
            }
        }.addOnFailureListener {
            Log.e("HomeVM", "위치를 가져올 수 없음: ${it.message}")
        }
    }
}