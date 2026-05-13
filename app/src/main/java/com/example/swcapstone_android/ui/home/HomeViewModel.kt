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

    var isTrackingMode by mutableStateOf(false)

    var showUnlockConfirm by mutableStateOf(false)
    private var pendingUnlockId: Long? = null
    private var pendingLat: Double = 0.0
    private var pendingLng: Double = 0.0

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
        position = CameraPosition.fromLatLngZoom(LatLng(37.5665, 126.9780), 16.5f)
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

    private val _navigateToUnlock = MutableStateFlow<Long?>(null)
    val navigateToUnlock = _navigateToUnlock.asStateFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val userLocation = result.lastLocation ?: return

            if (isTrackingMode) {
                viewModelScope.launch {
                    try {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(LatLng(userLocation.latitude, userLocation.longitude))
                                    .zoom(19f)
                                    .bearing(userLocation.bearing)
                                    .build()
                            ),
                            durationMs = 1000
                        )
                    } catch (e: Exception) {
                        Log.d("Home", "카메라 애니메이션 중첩 혹은 취소됨: ${e.message}")
                    }
                }
            }

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

                    if (distance <= 10f) {
                        showBottomSheet = false
                        pendingUnlockId = id
                        pendingLat = userLocation.latitude
                        pendingLng = userLocation.longitude
                        showUnlockConfirm = true // 팝업
                        isTrackingMode = false
                        stopTracking() // 해금 시도 시 트래킹 중단 (반복 호출 방지)
                    }
                }
            } ?: run {
                distanceToSelectedPin = null // 선택된 핀 없으면 거리 안 띄움
            }
        }
    }

    private fun checkAndUnlockNest(id: Long, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val token = tokenManager.accessToken.first() ?: return@launch
                val authHeader = "Bearer $token"

                // 1. 상세 정보 조회
                val response = RetrofitClient.instance.getNestDetail(authHeader, id)
                val body = response.body()

                if (response.isSuccessful && body?.status == "SUCCESS") {
                    val nestData = body.data // 이제 data 필드에 접근 가능

                    if (!nestData.unlocked) {
                        // 2. 잠겨있다면 해금 요청
                        val locationBody = mapOf("latitude" to lat, "longitude" to lng)
                        val unlockResponse = RetrofitClient.instance.unlockNest(authHeader, id, locationBody)

                        val unlockBody = unlockResponse.body()

                        if (unlockResponse.isSuccessful && unlockBody?.status == "SUCCESS") {
                            Log.d("Home", "해금 성공: $id")

                            _navigateToUnlock.value = id

                            geofenceManager.removeGeofence(id.toString())
                        }
                    }
                    else {
                        // 이미 해금된 상태라면 바로 이동
                        Log.d("Home", "이미 해금된 둥지입니다.")
                        _navigateToUnlock.value = id
                        geofenceManager.removeGeofence(id.toString())
                    }
                    stopTracking()
                    distanceToSelectedPin = null
                    selectedPinId = null
                }
            } catch (e: Exception) {
                Log.e("Home", "해금 프로세스 오류: ${e.message}")
            }
        }
    }

    fun onUnlockNavigated() {
        _navigateToUnlock.value = null
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

        isTrackingMode = true

        // 현재 기기의 정밀한 위치를 1회성으로 요청
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            location?.let {
                Log.d("Home", "내 위치 확인: ${it.latitude}, ${it.longitude}")

                // 카메라를 내 위치로 이동
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(it.latitude, it.longitude), 16.5f
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
        isTrackingMode = true

        // 해당 핀 위치로 카메라 이동 (선택 사항)
        markers.find { it.id == id }?.let { pin ->
            viewModelScope.launch {
                try {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(pin.position, 17.5f)
                    )
                } catch (e: Exception) {
                    Log.d("Home", "핀 선택 애니메이션 취소됨")
                }
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

    fun confirmUnlock() {
        val id = pendingUnlockId ?: return

        selectedPinId = null
        distanceToSelectedPin = null
        isTrackingMode = false

        checkAndUnlockNest(id, pendingLat, pendingLng)

        showUnlockConfirm = false
    }

    // 사용자가 팝업에서 [아니오]를 눌렀을 때 실행
    fun dismissUnlockConfirm() {
        showUnlockConfirm = false
        startTracking()
    }
}