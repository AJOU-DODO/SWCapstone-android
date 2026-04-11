package com.example.swcapstone_android.ui.login

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.swcapstone_android.R
import com.example.swcapstone_android.ui.theme.DODO

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    var showWebView by remember { mutableStateOf(false) }
    val mainGreenColor = Color(0xFF386641)

    //버튼 눌렀을때 웹뷰
    if (showWebView) {
        LoginWebView(url = viewModel.loginUrl) { result ->
            viewModel.handleLoginResult(result) {
                showWebView = false
                onLoginSuccess()
            }
        }
    }
    else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DODO)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            // 로고 이미지
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "Dodo Logo",
                modifier = Modifier.size(180.dp),
                contentScale = ContentScale.Fit
            )

            Text(
                text = "DODO",
                color = mainGreenColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(60.dp))

            // 슬로건 텍스트
            Text(
                text = "가장 가까운 곳에서 시작되는\n특별한 탐험",
                color = Color.Black,
                fontSize = 18.sp,
                lineHeight = 26.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // 로그인 버튼
            GoogleSignInButton(
                onClick = {
                    // TODO: 실제 구글 로그인 로직 호출
                    showWebView = true // 테스트용
                },
                modifier = Modifier.padding(bottom = 80.dp) // 바닥에서의 여백
            )
        }
    }
}


@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(0.8f)
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.dp, Color(0xFF747775)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1F1F1F)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // 로고
            Image(
                painter = painterResource(id = R.drawable.ic_google_logo),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Google 계정으로 로그인",
                fontSize = 16.sp,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginWebView(url: String, onResult: (String) -> Unit) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        view?.evaluateJavascript("(function() { return document.body.innerText; })();") { result ->
                            if (!result.isNullOrBlank() && result != "null" && result.contains("SUCCESS")) {
                                onResult(result)
                            }
                        }
                    }
                }
                loadUrl(url)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}