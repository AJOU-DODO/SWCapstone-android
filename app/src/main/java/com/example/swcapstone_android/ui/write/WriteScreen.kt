package com.example.swcapstone_android.ui.write

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swcapstone_android.data.bridge.WriteBridge
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.MapUiSettings
import com.google.android.gms.maps.model.LatLng


@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteScreen(
    onBackClick: () -> Unit,
    lat: Double,
    lng: Double,
    viewModel: WriteViewModel = viewModel()
) {
    val context = LocalContext.current

    val accessToken by viewModel.accessToken.collectAsState(initial = null)

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.handleImageSelection(context, it) }
    }

    // 브릿지 인스턴스
    val webBridge = remember {
        WriteBridge(
            onImageRequest = { galleryLauncher.launch("image/*") },
            onPublishRequest = { radius -> viewModel.requestPublication(radius) }
        )
    }

    LaunchedEffect(viewModel.jsCommand) {
        viewModel.jsCommand?.let { command ->
            webViewRef?.evaluateJavascript(command, null)
            viewModel.clearJsCommand()
        }
    }

    LaunchedEffect(accessToken, lat, lng) {
        webBridge.setData(token = accessToken, lat = lat, lng = lng)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("둥지 만들기", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFAF7E4) // 홈 화면과 통일감 있는 색상
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
        {
            if (accessToken != null) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            webViewRef = this
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.apply {
                                javaScriptEnabled = true      // 자바스크립트 허용
                                domStorageEnabled = true       // 로컬 스토리지 허용
                                allowFileAccess = false
                                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            }

                            addJavascriptInterface(webBridge, "AndroidBridge")

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    viewModel.updateLoading(false) // 로딩 바 숨기기
                                }
                            }

                            loadUrl(viewModel.writeUrl)
                        }
                    },
                    update = {

                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (viewModel.showPublishConfirm) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .fillMaxHeight(0.7f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("이 위치에 발행할까요?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("설정한 반경: ${viewModel.publishRadius}m", fontSize = 14.sp, color = Color.Gray)

                            // 지도 미리보기 영역
                            Box(modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = rememberCameraPositionState {
                                        position = CameraPosition.fromLatLngZoom(LatLng(lat, lng), 17f)
                                    },
                                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                                ) {
                                    // 중앙 마커
                                    val markerState = remember { MarkerState(position = LatLng(lat, lng)) }
                                    Marker(state = markerState)
                                    // 실제 해금 범위 원형 표시
                                    Circle(
                                        center = LatLng(lat, lng),
                                        radius = viewModel.publishRadius.toDouble(),
                                        fillColor = Color(0x33386641), // 연한 초록색 채우기
                                        strokeColor = Color(0xFF386641), // 진한 초록색 테두리
                                        strokeWidth = 2f
                                    )
                                }
                            }

                            // 버튼 레이아웃
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.dismissConfirm() }, // 아니오 -> 그냥 닫기
                                    modifier = Modifier.weight(1f)
                                ) { Text("아니오") }

                                Button(
                                    onClick = { viewModel.sendApproveToWeb() }, // 예 -> window.getApprove() 실행
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF386641))
                                ) { Text("예") }
                            }
                        }
                    }
                }
            }
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF386641)
                )
            }
        }
    }
}