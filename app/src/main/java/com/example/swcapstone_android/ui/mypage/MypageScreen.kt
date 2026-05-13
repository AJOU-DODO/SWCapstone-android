package com.example.swcapstone_android.ui.mypage

import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swcapstone_android.data.bridge.MypageBridge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MypageScreen(
    onBackClick: () -> Unit,
    onNavigateToPostcard: () -> Unit,
    viewModel: MypageViewModel = viewModel()
) {
    val context = LocalContext.current

    val accessToken by viewModel.accessToken.collectAsState(initial = null)
    val url = remember { viewModel.getMypageUrl() }

    val jsCommand = viewModel.jsCommand

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.handleImageSelection(context, it) }
    }

    val mypageBridge = remember(accessToken) {
        MypageBridge(
            accessToken = accessToken,
            onImageRequest = { galleryLauncher.launch("image/*") },
            onPostcardRequest = { onNavigateToPostcard() }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("마이페이지", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF1F3E9),
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (accessToken != null) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = WebViewClient()
                            addJavascriptInterface(mypageBridge, "AndroidBridge")
                            loadUrl(url)
                        }
                    },
                    modifier = Modifier.fillMaxSize(),

                    update = { webView ->
                        jsCommand?.let { command ->
                            webView.loadUrl("javascript:$command")
                            viewModel.clearJsCommand()
                        }
                    }
                )
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}