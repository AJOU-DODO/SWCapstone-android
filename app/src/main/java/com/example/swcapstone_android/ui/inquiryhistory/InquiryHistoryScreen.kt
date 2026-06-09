package com.example.swcapstone_android.ui.inquiryhistory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.swcapstone_android.data.model.InquiryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiryHistoryScreen(
    onBackClick: () -> Unit,
    onNavigateToCreateInquiry: () -> Unit,
    viewModel: InquiryHistoryViewModel = viewModel() // 🌟 전용 뷰모델 주입으로 정정
) {
    val mainGreenColor = Color(0xFF2B6340)
    val backgroundIvory = Color(0xFFFAF7E4)

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("문의 내역 확인", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    TextButton(
                        onClick = onNavigateToCreateInquiry,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "문의하기",
                            color = mainGreenColor, // 테마 컬러 매핑
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundIvory)
            )
        },
        containerColor = backgroundIvory
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = mainGreenColor
                )
            } else if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage ?: "",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (viewModel.inquiryList.isEmpty()) {
                Text(
                    text = "작성하신 문의 내역이 없습니다.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(viewModel.inquiryList) { item ->
                        InquiryExpandableCard(item = item, mainGreen = mainGreenColor)
                    }
                }
            }
        }
    }
}

@Composable
fun InquiryExpandableCard(item: InquiryItem, mainGreen: Color) {
    var isExpanded by remember { mutableStateOf(false) }
    val arrowRotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrow")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isAnswered = item.statusDescription == "처리 완료"
                Surface(
                    color = if (isAnswered) Color(0xFF2B6340) else Color(0xFFFF6F61),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = item.statusDescription,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(text = item.formattedDate, fontSize = 11.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.rotate(arrowRotationAngle)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF0F0F0))

                    Text(text = "내 문의 내용:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = item.content, fontSize = 13.sp, color = Color(0xFF333333), lineHeight = 18.sp)

                    if (!item.answer.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F3E9), shape = RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(text = "담당자 답변", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = mainGreen)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = item.answer, fontSize = 13.sp, color = Color.Black, lineHeight = 18.sp)
                        }
                    } else {
                        // 답변이 없을 때는 깔끔하게 여백 공백만 배치
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}