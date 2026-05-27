package com.example.swcapstone_android.ui.home

import kotlinx.coroutines.flow.first
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.swcapstone_android.BuildConfig
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.etc.UrlProvider
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
        Priority.PRIORITY_HIGH_ACCURACY, 1000L
    ).build()

    private val geofenceManager = GeofenceManager(application)
    private val tokenManager = TokenManager(application)
    val accessToken = tokenManager.accessToken
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)
    // 현재 지도 카메라 상태

    private val sharedPreferences = application.getSharedPreferences("dodo_settings", Context.MODE_PRIVATE)
    private fun isCategoryFilterEnabled(): Boolean {
        return sharedPreferences.getBoolean("category_enabled", true)
    }
    var cameraPositionState by mutableStateOf<CameraPositionState>(CameraPositionState(
        position = CameraPosition.fromLatLngZoom(LatLng(37.5665, 126.9780), 16.5f)
    ))

    // 위치 권한 허용 여부
    var isLocationPermissionGranted by mutableStateOf(false)

    // 마커 데이터 (예시 데이터)
    var markers = mutableStateListOf<PinData>()
        private set

    var showBottomSheet by mutableStateOf(false)

    var selectedUrl by mutableStateOf("${UrlProvider.baseUrl}/nests")

    private val _selectedNestIds = MutableStateFlow<List<Long>>(emptyList())
    val selectedNestIds: StateFlow<List<Long>> = _selectedNestIds.asStateFlow()

    var selectedPinId by mutableStateOf<Long?>(null)
        private set

    private val _navigateToUnlock = MutableStateFlow<Long?>(null)
    val navigateToUnlock = _navigateToUnlock.asStateFlow()

    var arrowRotation by mutableStateOf(0f)
        private set

    var isArrowVisible by mutableStateOf(false)
        private set

    var walkingPaths by mutableStateOf<List<List<LatLng>>>(emptyList())
        private set

    private var lastFetchedLocation: LatLng? = null

    private var lastFetchTime = 0L
    private val FETCH_COOLDOWN = 30000L

    private fun getSavedRadius(): Int {
        val radiusRaw = sharedPreferences.all["search_radius"] ?: 2000
        return when (radiusRaw) {
            is String -> radiusRaw.toIntOrNull() ?: 2000
            is Int -> radiusRaw
            else -> 2000
        }
    }

    private fun getZoomLevelForRadius(radius: Int): Float {
        return when (radius) {
            500 -> 16.5f   // 500m 반경은 좁고 정밀하게
            1000 -> 15.5f  // 1000m 반경은 표준
            2000 -> 14.5f  // 2000m 반경은 넓게 한눈에
            else -> 14.5f
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val userLocation = result.lastLocation ?: return
            val userLatLng = LatLng(userLocation.latitude, userLocation.longitude)

            fetchWalkingPathsAtUserLocation(userLatLng)

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
                            durationMs = 900
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
                    val bearingToPin = userLocation.bearingTo(pinLocation)
                    arrowRotation = bearingToPin - cameraPositionState.position.bearing
                    val distance = userLocation.distanceTo(pinLocation)
                    isArrowVisible = distance > 15f

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

                val currentRadius = getSavedRadius()

                val categoryIdsParam = if (isCategoryFilterEnabled()) {
                    Log.d("Home", "카테고리 필터 작동 중: 유저 관심사 조회 시도")

                    val interestResponse = RetrofitClient.instance.getUserInterests(authHeader)
                    if (interestResponse.isSuccessful && interestResponse.body() != null) {
                        interestResponse.body()?.data?.map { it.id } ?: emptyList()
                    } else {
                        Log.e("Home", "유저 관심사 가져오기 실패: ${interestResponse.code()}")
                        emptyList()
                    }
                } else {
                    Log.d("Home", "카테고리 필터 꺼짐: 전체 핀 조회")
                    null
                }

                val response = RetrofitClient.instance.getNearbyPins(
                    token = authHeader,
                    latitude = lat,
                    longitude = lng,
                    radiusMeter = currentRadius,
                    categoryIds = categoryIdsParam
                )
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

    @SuppressLint("MissingPermission")
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

                val userLatLng = LatLng(it.latitude, it.longitude)

                val currentRadius = getSavedRadius()
                val dynamicZoom = getZoomLevelForRadius(currentRadius)

                // 카메라를 내 위치로 이동
                viewModelScope.launch {
                    try {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(userLatLng, dynamicZoom),
                            durationMs = 400
                        )
                    } catch (e: Exception) {
                        Log.d("Home", "버튼 카메라 애니메이션 취소됨")
                    }
                }

                // 해당 좌표로 서버에 핀 요청
                fetchNearbyPins(it.latitude, it.longitude)

                fetchWalkingPathsAtUserLocation(userLatLng)
            }
        }.addOnFailureListener {
            Log.e("HomeVM", "위치를 가져올 수 없음: ${it.message}")
        }
    }

    fun onMarkerClick(pin: PinData) {
        _selectedNestIds.value = listOf(pin.id)
        selectedUrl = "${UrlProvider.baseUrl}/nests"
        showBottomSheet = true
    }

    fun onClusterMarkerClick(ids: List<Long>) {
        _selectedNestIds.value = ids
        selectedUrl = "${UrlProvider.baseUrl}/nests" // 여러 개일 때 리스트를 보여줄 페이지
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
                        CameraUpdateFactory.newLatLngZoom(pin.position, 19f)
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

    fun fetchWalkingPathsAtUserLocation(userLatLng: LatLng) {
        val currentTime = SystemClock.elapsedRealtime()

        // 30초마다만 가능
        if (currentTime - lastFetchTime < FETCH_COOLDOWN) {
            val remainingTime = (FETCH_COOLDOWN - (currentTime - lastFetchTime)) / 1000
            Log.d("OSM_OPTIMIZE", "30초 쿨타임 제한 중 (${remainingTime}초 남음). 호출 스킵.")
            return
        }

        lastFetchedLocation?.let { lastLoc ->
            val distanceResults = FloatArray(1)
            android.location.Location.distanceBetween(
                lastLoc.latitude, lastLoc.longitude,
                userLatLng.latitude, userLatLng.longitude,
                distanceResults
            )
            if (distanceResults[0] < 150f) {
                Log.d("OSM_OPTIMIZE", "유저가 아직 많이 안 움직임 (${distanceResults[0]}m). 호출 스킵.")
                return
            }
        }

        lastFetchTime = currentTime

        viewModelScope.launch {
            try {
                Log.d("OSM", "내 위치 기준 OSM 데이터 요청 시작")

                val lat = userLatLng.latitude
                val lng = userLatLng.longitude
                val delta = 0.005 // 내 주변 반경 약 500m로 범위 압축 (서버 부하 감소)

                val query = """
                [out:json][timeout:15];
                way["highway"~"footway|path|pedestrian"](${lat - delta},${lng - delta},${lat + delta},${lng + delta});
                out geom qt;
            """.trimIndent()

                val response = RetrofitClient.osmInstance.getOsmWalkingPaths(query)

                Log.d("OSM", "응답 코드: ${response.code()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("OSM", "Element Count: ${body?.elements?.size}")
                    val newPaths = body?.elements?.mapNotNull { element ->
                        element.geometry?.map { LatLng(it.lat, it.lon) }
                    } ?: emptyList()

                    Log.d("OSM", "Path Count: ${newPaths.size}")

                    // 🌟 [변경] 컴포즈 재그리기(Recomposition)가 확실하게 일어나도록 새 리스트 통째 할당
                    walkingPaths = newPaths

                    // 갱신 성공 시 현재 위치를 최종 호출 위치로 갱신
                    lastFetchedLocation = userLatLng
                } else {
                    Log.e("OSM", "서버 에러 코드: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Home", "OSM 로드 실패: ${e.message}")
                lastFetchTime = SystemClock.elapsedRealtime() - 25000L
            }
        }
    }
}