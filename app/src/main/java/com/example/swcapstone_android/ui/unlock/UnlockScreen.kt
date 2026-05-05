package com.example.swcapstone_android.ui.unlock

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UnlockScreen(
    nestId: Long,
    onFinished: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFFAF7E4) // 앱 테마와 맞춘 베이지톤 배경
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "둥지 해금 완료!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF386641)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 요청한 ID 숫자 표시
            Text(
                text = "ID: $nestId",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF386641)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onFinished,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF386641))
            ) {
                Text("탐험 시작하기", color = Color.White)
            }
        }
    }
}