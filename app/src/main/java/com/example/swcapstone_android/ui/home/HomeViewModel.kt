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

    fun updatePermissionStatus(granted: Boolean) {
        isLocationPermissionGranted = granted
    }
}