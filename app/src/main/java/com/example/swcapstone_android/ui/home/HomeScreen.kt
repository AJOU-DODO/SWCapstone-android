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
               onNavigateToInquiryHistory: () -> Unit,
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var isMenuExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val webBridge = remember {
        WebBridge(onNestSelected = { id ->
            viewModel.selectPin(id)

            viewModel.markers.find { it.id == id }?.let { selectedPin ->
                viewModel.registerGeofence(
                    id = selectedPin.id.toString(),
                    lat = selectedPin.position.latitude,
                    lng = selectedPin.position.longitude
                )
                viewModel.startTracking()
                Log.d("Home", "지오펜스 등록 호출: ${selectedPin.id}")
            }
            Log.d("Home", "선택된 ID 처리: $id")
        })
    }

    val selectedIds by viewModel.selectedNestIds.collectAsState()
    val accessToken by viewModel.accessToken.collectAsState(initial = null)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

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

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(unlockNestId) {
        unlockNestId?.let { id ->
            onNavigateToUnlock(id)
            viewModel.onUnlockNavigated()
        }
    }

    LaunchedEffect(locationPermissionState.status.isGranted, viewModel.markers, initialSelectedNestId) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        } else {
            viewModel.updatePermissionStatus(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
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
                    true
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
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                },
                clusterItemContent = { pin ->
                    Box(modifier = Modifier.size(44.dp)) {
                        val iconRes = if (pin.isAd) R.drawable.ic_nest_gift else R.drawable.ic_nest_single
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = if (pin.isAd) "광고 둥지" else "일반 둥지",
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
                MarkerComposable(
                    state = MarkerState(position = selectedPin.position),
                    keys = arrayOf(selectedPin.id),
                    zIndex = 2f
                ) {
                    Box(modifier = Modifier.size(44.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_flag_destination),
                            contentDescription = selectedPin.title,
                            tint = Color.Unspecified,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        if (viewModel.isArrowVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 200.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_navigation_arrow),
                    contentDescription = "Direction Arrow",
                    tint = Color(0xFF386641),
                    modifier = Modifier
                        .size(48.dp)
                        .graphicsLayer { rotationZ = viewModel.arrowRotation }
                )
            }
        }

        viewModel.distanceToSelectedPin?.let { distance ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 84.dp),
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

        HomeTopBar(modifier = Modifier.align(Alignment.TopCenter))

        // 🌟 [정밀 팩토링 완료] 하단 버튼들 인자값 완벽 결합
        HomeBottomButtons(
            onWriteClick = {
                viewModel.getActualLocation { actualLatLng ->
                    onNavigateToWrite(actualLatLng.latitude, actualLatLng.longitude)
                }
            },
            onLocationClick = {
                viewModel.fetchPinsAtUserLocation() // 👈 락 풀고 빼먹었던 인자 정상 주입!
            },
            onMenuClick = { isMenuExpanded = !isMenuExpanded },
            isMenuExpanded = isMenuExpanded,
            onSubMenuClick = { menuLabel ->
                isMenuExpanded = false
                when(menuLabel) {
                    "문의" -> onNavigateToInquiryHistory()
                    "카테고리" -> onNavigateToCategory()
                    "마이페이지" -> onNavigateToMypage()
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f)
                        .padding(bottom = 16.dp)
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = WebViewClient()
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
                            webBridge.setData(token = accessToken, ids = selectedIds)
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
        color = Color(0xFFFAF7E4),
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
    onWriteClick: () -> Unit,
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
        FloatingActionButton(
            onClick = onWriteClick,
            containerColor = Color(0xFFF1F3E9),
            shape = CircleShape,
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.BottomStart)
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_write), contentDescription = "Write", tint = Color(0xFF386641))
        }

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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomEnd),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 🌟 [수정 반영] 중복 글쓰기 제거하고 "설정"이 정상 등판하도록 서브 메뉴 리스트 정정!
            val menuItems = remember {
                listOf(
                    "설정" to R.drawable.ic_setting,
                    "문의" to R.drawable.ic_inquiry,
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
                            painter = painterResource(id = menuItem.second),
                            contentDescription = menuItem.first,
                            tint = Color(0xFF386641),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = onMenuClick,
                containerColor = if (isMenuExpanded) Color(0xFF386641) else Color(0xFFF1F3E9),
                contentColor = if (isMenuExpanded) Color.White else Color.Black,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    if (isMenuExpanded) Icons.Default.Close else Icons.Default.Menu,
                    contentDescription = "Menu"
                )
            }
        }
    }
}