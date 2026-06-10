package com.example.swcapstone_android.ui.postcard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.swcapstone_android.R
import com.example.swcapstone_android.ui.mypage.MypageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostcardScreen(
    onBackClick: () -> Unit,
    postcardViewModel: PostcardViewModel = viewModel(),
    mypageViewModel: MypageViewModel = viewModel()
) {
    // 1. PhotoPicker 런처 설정
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // 사진 선택 후 결과 처리
        if (uri != null) {
            postcardViewModel.updateImage(uri)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("엽서 작성", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        postcardViewModel.sendPostcard {
                            mypageViewModel.requestWebReload()
                            onBackClick()
                        }
                    }) {
                        Text("발행", color = Color(0xFF386641), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFAF7E4),
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                )
            )
        },
        containerColor = Color(0xFFFAF7E4) // 전체 배경 베이지톤
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 엽서 컨테이너 (흰색 카드)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 사진 영역
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color(0xFFF1F3E9), RoundedCornerShape(12.dp))
                            .clickable {
                                // 이미지 전용 PhotoPicker 실행
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (postcardViewModel.selectedImageUri != null) {
                            AsyncImage(
                                model = postcardViewModel.selectedImageUri,
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_add_photo), // 사진 추가 아이콘
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("사진 추가하기", color = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 메시지 입력 영역
                    OutlinedTextField(
                        value = postcardViewModel.message,
                        onValueChange = { if (it.length <= 200) postcardViewModel.updateMessage(it) },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        placeholder = { Text("메시지를 입력하세요...", color = Color.LightGray) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1A1A1A),
                            unfocusedTextColor = Color(0xFF1A1A1A),
                            focusedBorderColor = Color(0xFF386641),
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )

                    Text(
                        text = "${postcardViewModel.message.length} / 200",
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}