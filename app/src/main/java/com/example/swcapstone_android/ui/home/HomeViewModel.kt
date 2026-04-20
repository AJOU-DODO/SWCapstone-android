package com.example.swcapstone_android.ui.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState


class HomeViewModel(application: Application) : AndroidViewModel(application) {
    // 현재 지도 카메라 상태
    var cameraPositionState by mutableStateOf<CameraPositionState>(CameraPositionState(
        position = CameraPosition.fromLatLngZoom(LatLng(37.5665, 126.9780), 15f)
    ))

    // 위치 권한 허용 여부
    var isLocationPermissionGranted by mutableStateOf(false)

    // 마커 데이터 (예시 데이터)
    val markers = listOf(
        LatLng(37.5665, 126.9785),
        LatLng(37.5670, 126.9790),
        LatLng(37.5655, 126.9770)
    )

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
}