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
import com.example.swcapstone_android.BuildConfig
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.PinData
import com.example.swcapstone_android.data.model.TokenData
import com.example.swcapstone_android.data.remote.RetrofitClient
import com.example.swcapstone_android.util.GeofenceManager
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class HomeViewModel(application: Application) : AndroidViewModel(application) {

    var distanceToSelectedPin by mutableStateOf<Int?>(null)
        private set

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 3000L // 3초마다 업데이트
    ).build()

    private val geofenceManager = GeofenceManager(application)
    private val tokenManager = TokenManager(application)
    val accessToken = tokenManager.accessToken
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

    var selectedUrl by mutableStateOf("${BuildConfig.WEB_URL}/nests")

    private val _selectedNestIds = MutableStateFlow<List<Long>>(emptyList())
    val selectedNestIds: StateFlow<List<Long>> = _selectedNestIds.asStateFlow()

    var selectedPinId by mutableStateOf<Long?>(null)
        private set
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val userLocation = result.lastLocation ?: return

            // 선택된 핀이 있을 때만 거리 계산
            selectedPinId?.let { id ->
                markers.find { it.id == id }?.let { pin ->
                    val pinLocation = android.location.Location("").apply {
                        latitude = pin.position.latitude
                        longitude = pin.position.longitude
                    }
                    val distance = userLocation.distanceTo(pinLocation)

                    // 10m 단위로 끊어서 업데이트 (예: 28m -> 20m)
                    distanceToSelectedPin = (distance.toInt() / 10) * 10
                }
            } ?: run {
                distanceToSelectedPin = null // 선택된 핀 없으면 거리 안 띄움
            }
        }
    }

    fun updatePermissionStatus(granted: Boolean) {
        val isChanged = isLocationPermissionGranted != granted
        isLocationPermissionGranted = granted
        Log.d("Home", "위치 권한 확인")

        if (isChanged && granted) {
            fetchPinsAtUserLocation()
        }
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
                    Log.d("Home", "핀 가져오기 성공: ${markers.size}개")
                } else {
                    Log.e("Home", "핀 가져오기 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Home", "네트워크 오류: ${e.message}")
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
                Log.d("Home", "내 위치 확인: ${it.latitude}, ${it.longitude}")

                // 카메라를 내 위치로 이동
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(it.latitude, it.longitude), 17.5f
                )

                // 해당 좌표로 서버에 핀 요청
                fetchNearbyPins(it.latitude, it.longitude)
            }
        }.addOnFailureListener {
            Log.e("HomeVM", "위치를 가져올 수 없음: ${it.message}")
        }
    }

    fun onMarkerClick(pin: PinData) {
        _selectedNestIds.value = listOf(pin.id)
        selectedUrl = "${BuildConfig.WEB_URL}/nests"
        showBottomSheet = true
    }

    fun onClusterMarkerClick(ids: List<Long>) {
        _selectedNestIds.value = ids
        selectedUrl = "${BuildConfig.WEB_URL}/nests" // 여러 개일 때 리스트를 보여줄 페이지
        showBottomSheet = true
    }

    fun registerGeofence(id: String, lat: Double, lng: Double) {
        geofenceManager.addGeofence(id, lat, lng)
    }

    @SuppressLint("MissingPermission")
    fun getActualLocation(onLocationRetrieved: (LatLng) -> Unit) {
        if (!isLocationPermissionGranted) return

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            location?.let {
                onLocationRetrieved(LatLng(it.latitude, it.longitude))
            }
        }.addOnFailureListener {
            Log.e("HomeVM", "현재 위치를 가져오지 못했습니다: ${it.message}")
            // 위치를 못 가져올 경우를 대비해 지도 중심점이라도 반환하는 백업 로직
            onLocationRetrieved(cameraPositionState.position.target)
        }
    }

    fun selectPin(id: Long) {
        selectedPinId = id
        showBottomSheet = false // 바텀시트 닫기

        // 해당 핀 위치로 카메라 이동 (선택 사항)
        markers.find { it.id == id }?.let { pin ->
            viewModelScope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(pin.position, 17.5f)
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    fun stopTracking() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}