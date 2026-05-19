package com.example.swcapstone_android.ui.splash

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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(viewModel: SplashViewModel,
                 onSplashFinished: (String) -> Unit) {
    // 2초 뒤에 다음 화면으로 이동
    LaunchedEffect(Unit) {
        delay(2000)
        viewModel.checkLoginStatus { destination ->
            onSplashFinished(destination)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                colorResource(id = R.color.dodo)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "App Logo",
            modifier = Modifier.size(150.dp)
        )
    }
}