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
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.swcapstone_android.data.model.InquiryItem
import com.example.swcapstone_android.data.model.NoticeItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InquiryHistoryScreen(
    onBackClick: () -> Unit,
    onNavigateToCreateInquiry: () -> Unit,
    viewModel: InquiryHistoryViewModel
) {
    val mainGreenColor = Color(0xFF2B6340)
    val backgroundIvory = Color(0xFFFAF7E4)

    // 0: 공지사항 탭, 1: 1:1 문의내역 탭
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("공지사항", "1:1 문의내역")

    // 🌟 [수정] 탭이 스위칭될 때마다 뷰모델의 최신 상태를 실시간으로 갱신하도록 트리거 고정!
    LaunchedEffect(selectedTabIndex) {
        viewModel.refresh(isNoticeTab = (selectedTabIndex == 0))
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("고객지원 센터", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                        }
                    },
                    actions = {
                        // 1:1 문의 탭 활성화 상태일 때만 우측 상단에 작게 "문의하기" 버튼을 노출
                        if (selectedTabIndex == 1) {
                            TextButton(
                                onClick = onNavigateToCreateInquiry,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("문의하기", color = mainGreenColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundIvory)
                )

                // 🌟 [최신 명세] SecondaryTabRow 적용하여 깔끔한 5:5 반반 스위칭 탭 구성
                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = backgroundIvory,
                    contentColor = mainGreenColor,
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            },
                            selectedContentColor = mainGreenColor,
                            unselectedContentColor = Color.Gray
                        )
                    }
                }
            }
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
            } else {
                // 🌟 [버그 수정 완료] selectedTabIndex 조건문에 맞춰 리스트 독립 매핑
                if (selectedTabIndex == 0) {
                    // 📢 [공지사항 탭]
                    if (viewModel.noticeList.isEmpty()) {
                        Text(
                            text = "등록된 공지사항이 없습니다.",
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
                            items(viewModel.noticeList) { item ->
                                NoticeExpandableCard(item = item, mainGreen = mainGreenColor)
                            }
                        }
                    }
                } else {
                    // ✉️ [1:1 문의내역 탭]
                    if (viewModel.inquiryList.isEmpty()) {
                        // 🌟 [기획 패치] 작성한 내역이 비어있을 때 다이렉트로 작성을 유도하는 공석 UI 배치
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "작성하신 문의 내역이 없습니다.",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = onNavigateToCreateInquiry,
                                colors = ButtonDefaults.buttonColors(containerColor = mainGreenColor)
                            ) {
                                Text("첫 문의하기 작성", fontWeight = FontWeight.Bold)
                            }
                        }
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
    }
}

// 🌟 [공지사항 전용] 아코디언 확장 카드 컴포저블
@Composable
fun NoticeExpandableCard(item: NoticeItem, mainGreen: Color) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f, label = "arrow_rotate")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = mainGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = item.categoryDescription,
                        color = mainGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(text = item.createdAt.take(10), fontSize = 11.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationState)
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "열기")
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = item.content,
                        fontSize = 13.sp,
                        color = Color(0xFF333333),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// 🌟 [1:1 문의내역 전용] 답변 유무 색상 반영 아코디언 카드 컴포저블
@Composable
fun InquiryExpandableCard(item: InquiryItem, mainGreen: Color) {
    var isExpanded by remember { mutableStateOf(false) }
    val arrowRotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "arrow_rotate")

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
                    // 처리 완료 시 요청한 피드백 반영 컬러(0xFF2B6340) 주입, 대기 시 빨간색 주입
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}