package com.example.swcapstone_android.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swcapstone_android.R
import com.example.swcapstone_android.ui.theme.DODO

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    // 이미지 시안의 도도 로고 및 텍스트 녹색
    val mainGreenColor = Color(0xFF386641)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DODO)
            .padding(horizontal = 24.dp), // 좌우 여백
        horizontalAlignment = Alignment.CenterHorizontally // 내부 요소들 가로 중앙 정렬
    ) {
        // 1. 상단 여백 (로고를 약간 위쪽에 배치)
        Spacer(modifier = Modifier.height(100.dp))

        // 2. 도도새 로고 이미지
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground), // 로고 리소스 ID
            contentDescription = "Dodo Logo",
            modifier = Modifier.size(180.dp), // 이미지 크기 조절
            contentScale = ContentScale.Fit
        )

        // 3. "DODO" 텍스트 (로고 바로 아래)
        Text(
            text = "DODO",
            color = mainGreenColor,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold, // 굵게
            style = MaterialTheme.typography.headlineLarge
        )

        // 4. 로고 세트와 슬로건 사이 여백
        Spacer(modifier = Modifier.height(60.dp))

        // 5. 슬로건 텍스트 (두 줄, 중앙 정렬)
        Text(
            text = "가장 가까운 곳에서 시작되는\n특별한 탐험",
            color = Color.Black,
            fontSize = 18.sp,
            lineHeight = 26.sp, // 줄간격
            textAlign =  TextAlign.Center, // 텍스트 중앙 정렬
            modifier = Modifier.fillMaxWidth()
        )

        // 6. 슬로건과 버튼 사이 여백 (최대한 아래로 밀어내기 위해 weight 사용)
        Spacer(modifier = Modifier.weight(1f))

        // 7. 구글 로그인 버튼 (중앙 하단 배치)
        GoogleSignInButton(
            onClick = {
                // TODO: 실제 구글 로그인 로직 호출
                onLoginSuccess() // 테스트용
            },
            modifier = Modifier.padding(bottom = 80.dp) // 바닥에서의 여백
        )
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