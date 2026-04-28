package com.example.swcapstone_android.ui.write

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    val accessToken by viewModel.accessToken.collectAsState(initial = null)

    // 브릿지 인스턴스
    val webBridge = remember {
        WriteBridge(
            onImageRequest = { /* 갤러리 로직 나중에 */ },
            onPublishRequest = { data -> /* 발행 로직 나중에 */ }
        )
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
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.apply {
                            javaScriptEnabled = true      // 자바스크립트 허용
                            domStorageEnabled = true       // 로컬 스토리지 허용
                            allowFileAccess = true         // 파일 접근 허용
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
                update = { webView ->
                    webBridge.setData(token = accessToken, lat = lat, lng = lng)
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