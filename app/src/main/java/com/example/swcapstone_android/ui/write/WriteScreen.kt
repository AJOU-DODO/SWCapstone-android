package com.example.swcapstone_android.ui.write

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swcapstone_android.data.bridge.WriteBridge

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
            onPublishRequest = { data -> /* 발행 로직 나중에 */ }
        )
    }

    LaunchedEffect(viewModel.jsCommand) {
        viewModel.jsCommand?.let { command ->
            webViewRef?.loadUrl(command)
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
                    containerColor = Color(0xFFF1F3E9) // 홈 화면과 통일감 있는 색상
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                            allowFileAccess = true        // 파일 접근 허용
                            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
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

            // 로딩 중일 때 중앙에 프로그레스 바 표시
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF386641)
                )
            }
        }
    }
}