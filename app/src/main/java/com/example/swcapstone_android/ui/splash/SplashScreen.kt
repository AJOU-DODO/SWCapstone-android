package com.example.swcapstone_android.ui.splash

import android.window.SplashScreen
import com.example.swcapstone_android.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.swcapstone_android.ui.Greeting
import com.example.swcapstone_android.ui.theme.SWCapstoneandroidTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // 2초 뒤에 다음 화면으로 이동하는 로직
    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                colorResource(id = R.color.dodo)), // 앱 테마에 맞는 배경색 설정
        contentAlignment = Alignment.Center
    ) {
        // 네가 말한 ic_launcher_foreground를 불러와서 배치
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "App Logo",
            modifier = Modifier.size(150.dp) // 로고 크기 조절
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SWCapstoneandroidTheme {
        SplashScreen(onTimeout = {})
    }
}