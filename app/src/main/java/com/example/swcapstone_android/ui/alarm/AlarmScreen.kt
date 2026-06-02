package com.example.swcapstone_android.ui.alarm

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.swcapstone_android.data.bridge.AlarmWebBridge

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AlarmScreen(
    nestId: String,
    notificationType: String?,
    viewModel: AlarmViewModel,
    onBackClick: () -> Unit
) {
    LaunchedEffect(nestId) {
        viewModel.initData(nestId, notificationType)
    }

    val alarmBridge = remember(viewModel) {
        AlarmWebBridge(getTokenBlock = { viewModel.accessTokenCache })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("반응 확인", fontWeight = FontWeight.Bold, color = Color(0xFF386641)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color(0xFF386641)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAF7E4)) // DODO 테마 느낌의 베이지 톤 탑바
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (viewModel.alarmUrl.isNotEmpty()) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()

                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            addJavascriptInterface(alarmBridge, "AndroidBridge")
                            loadUrl(viewModel.alarmUrl)
                        }
                    },
                    update = { webView ->
                        if (webView.url != viewModel.alarmUrl) {
                            webView.loadUrl(viewModel.alarmUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}