package com.example.swcapstone_android.ui.unlock

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swcapstone_android.data.bridge.UnlockBridge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlockScreen(
    nestId: Long,
    onFinished: () -> Unit,
    viewModel: UnlockViewModel = viewModel()
) {
    val accessToken by viewModel.accessToken.collectAsState(initial = null)
    Log.d("UnlockScreen", "accessToken: $accessToken")
    val url = remember(nestId) { viewModel.getNestUrl(nestId) }
    val unlockBridge = remember(accessToken) { UnlockBridge(accessToken) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("둥지 탐험", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onFinished) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF1F3E9)
                )
            )
        }
    ) { innerPadding ->
        if (accessToken != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()
                            addJavascriptInterface(unlockBridge, "AndroidBridge")
                            loadUrl(url)
                        }
                    },
                    update = { webView ->
                        // 필요한 경우 여기서 추가 업데이트 로직
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}