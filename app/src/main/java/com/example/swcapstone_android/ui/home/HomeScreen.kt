package com.example.swcapstone_android.ui.home

import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swcapstone_android.R
import com.example.swcapstone_android.data.bridge.WebBridge
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.compose.clustering.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class,
    MapsComposeExperimentalApi::class
)
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel(),
               onNavigateToSetting: () -> Unit,
               onNavigateToWrite: (Double, Double) -> Unit,
               onNavigateToUnlock: (Long) -> Unit,
               initialSelectedNestId: String? = null) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed) // 기존 Drawer 유지용
    var isMenuExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope() // 지도 작업에서 추가한 scope 유지

    val webBridge = remember {
        WebBridge(onNestSelected = { id ->
            viewModel.selectPin(id)

            viewModel.markers.find { it.id == id }?.let { selectedPin ->
                // 둥지 ID와 좌표를 넘겨 지오펜스 등록
                viewModel.registerGeofence(
                    id = selectedPin.id.toString(),
                    lat = selectedPin.position.latitude,
                    lng = selectedPin.position.longitude
                )
                viewModel.startTracking()
                Log.d("Home", "지오펜스 등록 호출: ${selectedPin.id}")
            }
            // hotfix에서 추가된 리스너 로직 유지
            Log.d("Home", "선택된 ID 처리: $id")
            // 필요하다면 여기서 viewModel의 함수를 호출하면 돼
        })
    }

    val selectedIds by viewModel.selectedNestIds.collectAsState()
    val accessToken by viewModel.accessToken.collectAsState(initial = null)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    // 위치 권한 상태 기억
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    val unlockNestId by viewModel.navigateToUnlock.collectAsState()

    LaunchedEffect(unlockNestId) {
        unlockNestId?.let { id ->
            onNavigateToUnlock(id)
            viewModel.onUnlockNavigated() // 중복 이동 방지 위해 리셋
        }
    }

    // 화면 진입 시 권한 요청
    LaunchedEffect(locationPermissionState.status.isGranted, viewModel.markers, initialSelectedNestId) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
        viewModel.updatePermissionStatus(locationPermissionState.status.isGranted)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 구글 지도 컴포넌트
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = viewModel.cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = viewModel.isLocationPermissionGranted,
                minZoomPreference = 14f,
                maxZoomPreference = 19f
            ),
            onMapClick = { viewModel.showBottomSheet = false },
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            // 마커 표시
            Clustering(
                items = viewModel.markers,
                onClusterItemClick = { pin ->
                    viewModel.onMarkerClick(pin)
                    true
                },
                onClusterClick = { cluster ->
                    val currentZoom = viewModel.cameraPositionState.position.zoom
                    if (currentZoom >= 19f) {
                        viewModel.onClusterMarkerClick(cluster.items.map { it.id })
                    } else {
                        // 직접 줌인 수행
                        scope.launch {
                            val targetZoom = (currentZoom + 1.25f).coerceAtMost(19f)

                            viewModel.cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    cluster.position, // 클릭된 클러스터의 중심 좌표
                                    targetZoom // 현재보다 1.5단계 더 확대
                                )
                            )
                        }
                    }
                    true // 직접 처리했으므로 true 반환
                }
            )

            viewModel.markers.find { it.id == viewModel.selectedPinId }?.let { selectedPin ->
                Marker(
                    state = MarkerState(position = selectedPin.position),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                    title = selectedPin.title,
                    zIndex = 1f // 다른 마커들보다 위에 보이게 설정
                )
            }
        }

        viewModel.distanceToSelectedPin?.let { distance ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 84.dp), // 상단 바(64dp)보다 아래에 위치하도록 조절
                color = if (distance <= 10) Color(0xFFE53935) else Color(0xFF386641),
                shape = CircleShape,
                shadowElevation = 4.dp
            ) {
                Text(
                    text = if (distance <= 10) "둥지에 도착했습니다!" else "목적지까지 약 ${distance}m",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            if (distance <= 10)
                viewModel.stopTracking()
            Log.d("Home", "지오펜스 해제 완료")
        }

        // 상단 바
        HomeTopBar(modifier = Modifier.align(Alignment.TopCenter))

        // 하단 버튼들 (알림, 메뉴)
        HomeBottomButtons(
            onAlarmClick = { /* 알림 이동 */ },
            onLocationClick = {
                viewModel.fetchPinsAtUserLocation()
            },
            onMenuClick = { isMenuExpanded = !isMenuExpanded },
            isMenuExpanded = isMenuExpanded,
            onSubMenuClick = { menuLabel ->
                isMenuExpanded = false // 메뉴 클릭 시 닫기
                when(menuLabel) {
                    "글쓰기" -> {
                        viewModel.getActualLocation { actualLatLng ->
                            onNavigateToWrite(actualLatLng.latitude, actualLatLng.longitude)
                        }
                }
                    "카테고리" -> { /* URL 변경 로직 */ }
                    "마이페이지" -> { /* URL 변경 로직 */ }
                    "설정" -> onNavigateToSetting()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (viewModel.showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.showBottomSheet = false },
                sheetState = sheetState,
                contentWindowInsets = { WindowInsets(0.dp) },
                containerColor = Color(0xFFFAF7E4),
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                // 바텀 시트 내부 내용
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f) // 화면의 90% 정도 높이까지 올라옴
                        .padding(bottom = 16.dp)
                ) {
                    AndroidView(
                        factory = { context ->
                            //WebView.setWebContentsDebuggingEnabled(true) 디버그 필요할때만
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = WebViewClient() // 새 창 뜨지 않게 방지
                                setOnTouchListener { v, event ->
                                    v.parent.requestDisallowInterceptTouchEvent(true)
                                    false
                                }
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                addJavascriptInterface(webBridge, "AndroidBridge")
                                loadUrl(viewModel.selectedUrl)
                            }
                        },
                        update = { webView ->
                            webBridge.setData(
                                token = accessToken,
                                ids = selectedIds
                            )

                            if (webView.url != viewModel.selectedUrl && viewModel.selectedUrl.isNotEmpty()) {
                                webView.loadUrl(viewModel.selectedUrl)
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }

        if (viewModel.showUnlockConfirm) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissUnlockConfirm() },
                title = { Text("새로운 둥지 발견!", fontWeight = FontWeight.Bold) },
                text = { Text("둥지 근처에 도착했습니다.\n이곳을 해금하고 탐험을 시작할까요?") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmUnlock() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF386641))
                    ) {
                        Text("예", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissUnlockConfirm() }) {
                        Text("아니오", color = Color.Gray)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun HomeTopBar(modifier: Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        color = Color(0xFFF1F3E9),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "DODO",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF386641),
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun HomeBottomButtons(
    onAlarmClick: () -> Unit,
    onMenuClick: () -> Unit,
    onLocationClick: () -> Unit,
    onSubMenuClick: (String) -> Unit,
    isMenuExpanded: Boolean,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 왼쪽: 알림 버튼
        FloatingActionButton(
            onClick = onAlarmClick,
            containerColor = Color(0xFFF1F3E9),
            shape = CircleShape,
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.BottomStart)
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_notification), contentDescription = "Notification")
        }

        // 중앙: 내 위치 버튼
        FloatingActionButton(
            onClick = onLocationClick,
            containerColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.BottomCenter)
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_my_location), contentDescription = "My Location", tint = Color(0xFF386641))
        }

        // 오른쪽: 메뉴 버튼 및 확장 메뉴
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomEnd),
            verticalArrangement = Arrangement.spacedBy(12.dp) // 버튼 사이 간격
        ) {
            // 확장될 서브 메뉴들
            val menuItems = listOf("설정", "글쓰기", "마이페이지", "카테고리")

            menuItems.forEachIndexed { index, label ->
                AnimatedVisibility(
                    visible = isMenuExpanded,
                    enter = fadeIn() + expandVertically() + slideInVertically { it / 2 },
                    exit = fadeOut() + shrinkVertically() + slideOutVertically { it / 2 }
                ) {
                    SmallFloatingActionButton(
                        onClick = { onSubMenuClick(label) },
                        containerColor = Color(0xFFF1F3E9),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        // 아이콘이 없다면 첫 글자만 텍스트로 표시하거나 공용 아이콘 사용
                        Text(text = label.take(1), color = Color(0xFF386641), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 메인 메뉴 버튼
            FloatingActionButton(
                onClick = onMenuClick,
                containerColor = if (isMenuExpanded) Color(0xFF386641) else Color(0xFFF1F3E9),
                contentColor = if (isMenuExpanded) Color.White else Color.Black,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                // 확장 상태에 따라 아이콘 변경 (X 모양 등)
                Icon(
                    if (isMenuExpanded) Icons.Default.Close else Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        }
    }
}