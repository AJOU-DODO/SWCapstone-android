package com.example.swcapstone_android.ui.userdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swcapstone_android.ui.theme.DODO // 기존에 정의한 배경색

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    viewModel: UserDetailViewModel,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DODO) // 이미지의 연한 베이지색 배경
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "프로필을 완성하세요.",
            fontSize = 16.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 프로필 이미지 아이콘 부분
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFFE0E0E0), shape = CircleShape)
                .border(2.dp, Color(0xFF5A5A42), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        // 닉네임 입력 필드
        TextField(
            value = viewModel.nickname,
            onValueChange = { viewModel.nickname = it },
            placeholder = {
                // placeholder 내부에도 정렬을 맞추기 위해 Box나 TextAlign 추가 가능
                Text("닉네임", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            },
            modifier = Modifier.fillMaxWidth(0.8f),
            // ⭐ 이 부분이 핵심 수정 사항이야!
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = Color(0xFF5A5A42),
                focusedIndicatorColor = Color(0xFF5A5A42),
                unfocusedIndicatorColor = Color.Gray,
            ),
            // Material3 TextField에는 textAlign이 직접 없으니 TextStyle로 지정해
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
        )

        Spacer(modifier = Modifier.height(80.dp))

        // 시작하기 버튼
        OutlinedButton(
            onClick = { viewModel.saveProfile(onComplete) },
            modifier = Modifier
                .width(120.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF5A5A42)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5A5A42))
        ) {
            Text("시작하기")
        }
    }
}