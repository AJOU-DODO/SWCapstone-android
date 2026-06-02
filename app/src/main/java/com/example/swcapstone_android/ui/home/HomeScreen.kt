package com.example.swcapstone_android.ui.home

import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.RoundCap
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
               onNavigateToMypage: () -> Unit,
               onNavigateToCategory: () -> Unit,
               initialSelectedNestId: String? = null) {
    val context = LocalContext.current

    var nestFullIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var nestSingleIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var destinationIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    LaunchedEffect(Unit) {
        nestFullIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_nest_full)
        nestSingleIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_nest_single)
        destinationIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_flag_destination)
    }

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

    val mapPaddingTop = if (viewModel.isTrackingMode) 400.dp else 0.dp

    val zoomLevel = viewModel.cameraPositionState.position.zoom
    val dynamicWidth = when {
        zoomLevel >= 18f -> 12f
        zoomLevel >= 16f -> 8f
        zoomLevel >= 14f -> 4f
        else -> 2f
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) { Log.d("Permission", "Notification permission denied") }
    }

    // 화면 진입 시 권한 요청
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(unlockNestId) {
        unlockNestId?.let { id ->
            onNavigateToUnlock(id)
            viewModel.onUnlockNavigated() // 중복 이동 방지 위해 리셋
        }
    }

    // 화면 진입 시 권한 요청
    LaunchedEffect(locationPermissionState.status.isGranted, viewModel.markers, initialSelectedNestId) {
            // GPS
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        } else {
            viewModel.updatePermissionStatus(true)

            // 알림
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 구글 지도 컴포넌트
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = viewModel.cameraPositionState,
            contentPadding = PaddingValues(top = mapPaddingTop),
            properties = MapProperties(
                isMyLocationEnabled = viewModel.isLocationPermissionGranted,
                minZoomPreference = 15f,
                maxZoomPreference = 19f,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
            ),
            onMapClick = { viewModel.showBottomSheet = false },
            onMapLoaded = {
                // 초기 로드 설정
            },
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
                            try {
                                val targetZoom = (currentZoom + 1.25f).coerceAtMost(19f)
                                viewModel.cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(cluster.position, targetZoom)
                                )
                            } catch (e: Exception) {
                                Log.d("Home", "클러스터 줌 애니메이션 취소됨")
                            }
                        }
                    }
                    true // 직접 처리했으므로 true 반환
                },

                clusterContent = { cluster ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_nest_full),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = cluster.size.toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 2.dp) // 숫자가 둥지 중앙 아래쪽에 잘 걸치도록 보정
                        )
                    }
                },
                // 단일 핀일 때: 알이 하나 있는 날렵한 둥지
                clusterItemContent = { _ ->
                    Box(modifier = Modifier.size(40.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_nest_single),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            )

            viewModel.walkingPaths.forEach { path ->
                Polyline(
                    points = path,
                    color = Color(0xFFF5F1E6),
                    width = dynamicWidth,
                    jointType = JointType.ROUND,
                    startCap = RoundCap(),
                    endCap = RoundCap(),
                    zIndex = 1f
                )
            }

            viewModel.markers.find { it.id == viewModel.selectedPinId }?.let { selectedPin ->
                // 이제 destinationIcon 변수(Bitmap)는 필요 없음! 컴포즈 UI로 직접 그림
                MarkerComposable(
                    state = MarkerState(position = selectedPin.position),
                    keys = arrayOf(selectedPin.id),
                    zIndex = 2f
                ) {
                    Box(modifier = Modifier.size(44.dp)) { // 우리가 처음에 정한 깃발 추천 크기
                        Icon(
                            painter = painterResource(id = R.drawable.ic_flag_destination),
                            contentDescription = selectedPin.title,
                            tint = Color.Unspecified, // 깃발의 다홍색 본래 색상 유지
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        if (viewModel.isArrowVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center) // 화면 중앙 혹은 상단에 배치
                    .padding(bottom = 200.dp) // 내 위치 아이콘보다 약간 위에 띄움
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_navigation_arrow), // 화살표 아이콘
                    contentDescription = "Direction Arrow",
                    tint = Color(0xFF386641),
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer {
                            rotationZ = viewModel.arrowRotation // 계산된 각도만큼 회전
                        }
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
                    "카테고리" -> { onNavigateToCategory() }
                    "마이페이지" -> { onNavigateToMypage() }
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
                        .fillMaxHeight(0.9f)
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
            .height(64.dp)
            .testTag("home_top_bar"),
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
            val menuItems = remember {
                listOf(
                    "설정" to R.drawable.ic_setting,
                    "글쓰기" to R.drawable.ic_write,
                    "마이페이지" to R.drawable.ic_mypage,
                    "카테고리" to R.drawable.ic_category
                )
            }

            menuItems.forEachIndexed { index, menuItem ->
                AnimatedVisibility(
                    visible = isMenuExpanded,
                    enter = fadeIn() + expandVertically() + slideInVertically { it / 2 },
                    exit = fadeOut() + shrinkVertically() + slideOutVertically { it / 2 }
                ) {
                    SmallFloatingActionButton(
                        onClick = { onSubMenuClick(menuItem.first) },
                        containerColor = Color(0xFFF1F3E9),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = menuItem.second), // 🛠️ .second로 드로어블 ID 전달
                            contentDescription = menuItem.first,
                            tint = Color(0xFF386641),
                            modifier = Modifier.size(24.dp) // 아이콘 크기 알맞게 조정
                        )
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