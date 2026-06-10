package com.example.swcapstone_android.ui.inquiry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swcapstone_android.data.model.InquiryType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiryScreen(
    onBackClick: () -> Unit,
    viewModel: InquiryViewModel
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("1:1 문의하기", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAF7E4))
            )
        },
        containerColor = Color(0xFFFAF7E4)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "궁금하신 점이나 불편한 사항을 남겨주세요.",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            )

            // 🌟 [신규 추가] 문의 유형 선택 Chip 영역
            Text(
                text = "문의 유형",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )

            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp) // 좌우 살짝 여백
            ) {
                items(InquiryType.values().size) { index ->
                    val type = InquiryType.values()[index]
                    val isSelected = viewModel.selectedType == type

                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedType = type },
                        label = {
                            Text(
                                text = type.title,
                                fontSize = 13.sp, // 글자 크기 적당히 유지
                                maxLines = 1     // 🌟 절대 줄바꿈 되지 않도록 한 줄 제한!
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2B6340),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // 제목 입력창
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it },
                label = { Text("제목") },
                placeholder = { Text("제목을 입력해주세요") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFF2B6340),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 내용 입력창
            OutlinedTextField(
                value = viewModel.content,
                onValueChange = { viewModel.content = it },
                label = { Text("내용") },
                placeholder = { Text("내용을 상세히 적어주세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                maxLines = 10,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color(0xFF2B6340),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            viewModel.errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 전송 버튼
            Button(
                onClick = { viewModel.submitInquiry(onSuccess = onBackClick) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !viewModel.isLoading,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2B6340),
                    contentColor = Color.White
                )
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("문의하기 제출", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}