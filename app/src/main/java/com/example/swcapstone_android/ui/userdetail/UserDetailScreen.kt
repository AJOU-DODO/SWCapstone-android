package com.example.swcapstone_android.ui.userdetail

import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.swcapstone_android.ui.theme.DODO

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    viewModel: UserDetailViewModel,
    onComplete: () -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) { Log.d("Permission", "Notification permission denied") }
    }

    // 화면 진입 시 권한 요청
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.selectedImageUri = uri
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DODO)
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

        // 프로필 이미지 아이콘
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color.LightGray, CircleShape)
                .clip(CircleShape)
                .clickable {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.selectedImageUri != null) {
                AsyncImage(
                    model = viewModel.selectedImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("사진 선택")
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        // 닉네임 입력
        TextField(
            value = viewModel.nickname,
            onValueChange = { viewModel.nickname = it },
            placeholder = {
                Text("닉네임", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            },
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                cursorColor = Color(0xFF5A5A42),
                focusedIndicatorColor = Color(0xFF5A5A42),
                unfocusedIndicatorColor = Color.Gray,
            ),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(80.dp))

        OutlinedButton(
            onClick = { viewModel.onStartClick(onComplete) },
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

    if (viewModel.showDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showDialog = false },
            confirmButton = {
                TextButton(onClick = { viewModel.showDialog = false }) { Text("확인") }
            },
            title = { Text("알림") },
            text = { Text("사진과 닉네임을 모두 입력해주세요!") }
        )
    }
}