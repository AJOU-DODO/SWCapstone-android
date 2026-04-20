package com.example.swcapstone_android.ui.home

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false // 반만 펼쳐지는 드래그 가능
    )

    // 위치 권한 상태 기억 (Accompanist Permissions 라이브러리)
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    // 화면 진입 시 권한 요청
    LaunchedEffect(Unit) {
        locationPermissionState.launchPermissionRequest()
    }

    // 권한 허용 여부를 ViewModel에 업데이트
    LaunchedEffect(locationPermissionState.status.isGranted) {
        viewModel.updatePermissionStatus(locationPermissionState.status.isGranted)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 구글 지도 컴포넌트
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = viewModel.cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = viewModel.isLocationPermissionGranted
            ),
            onMapClick = { viewModel.showBottomSheet = false },
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            // 마커 표시
            viewModel.markers.forEach { position ->
                Marker(
                    state = MarkerState(position = position),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                    onClick = {
                        viewModel.onMarkerClick(it.position)
                        true
                    }
                )
            }
        }

        // 상단 바 (DODO 로고)
        HomeTopBar(modifier = Modifier.align(Alignment.TopCenter))

        // 하단 버튼들 (알림, 메뉴)
        HomeBottomButtons(
            onAlarmClick = { /* 알림 이동 */ },
            onMenuClick = { /* 메뉴 열기 */ },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (viewModel.showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.showBottomSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFFFAF7E4), // 이미지와 비슷한 색감
                dragHandle = { BottomSheetDefaults.DragHandle() } // '...' 부분
            ) {
                // 바텀 시트 내부 내용
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f) // 화면의 70% 정도 높이까지 올라옴
                        .padding(bottom = 16.dp)
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                webViewClient = WebViewClient() // 새 창 뜨지 않게 방지
                                loadUrl(viewModel.selectedUrl)
                            }
                        },
                        update = { /* 갱신 필요 시 처리 */ },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
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
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        FloatingActionButton(
            onClick = onAlarmClick,
            containerColor = Color(0xFFF1F3E9),
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notification),
                contentDescription = "Notification"
            )
        }

        FloatingActionButton(
            onClick = onMenuClick,
            containerColor = Color(0xFFF1F3E9),
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
        }
    }
}