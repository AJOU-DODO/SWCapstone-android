package com.example.swcapstone_android.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val user = viewModel.userData

    // 화면이 처음 켜질 때 내 정보 불러오기
    LaunchedEffect(Unit) {
        viewModel.fetchMyInfo()
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator() // 로딩 중 뺑뺑이
            } else if (user != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 1. 프로필 이미지 (CloudFront URL)
                    AsyncImage(
                        model = user.profileImageUrl,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape), // 동그랗게 자르기
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. 닉네임 표시
                    Text(
                        text = "${user.nickname}님, 환영합니다!",
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            } else {
                Text(text = "유저 정보를 불러올 수 없습니다.")
            }
        }
    }
}