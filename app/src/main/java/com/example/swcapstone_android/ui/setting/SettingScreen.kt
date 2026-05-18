package com.example.swcapstone_android.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBackClick: () -> Unit,
    viewModel: SettingViewModel = viewModel()
) {
    val mainGreenColor = Color(0xFF386641)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF1F3E9))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // --- 카테고리 설정 섹션 ---
            Text(
                text = "알림 및 표시 설정",
                fontSize = 14.sp,
                color = mainGreenColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "카테고리 필터 사용", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(text = "지도에서 카테고리별로 핀을 필터링합니다.", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = viewModel.isCategoryEnabled,
                    onCheckedChange = { viewModel.toggleCategory(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = mainGreenColor)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Color(0xFFEEEEEE))

            // --- 반경 설정 섹션 ---
            Text(
                text = "탐색 반경 설정",
                fontSize = 14.sp,
                color = mainGreenColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "주변의 둥지를 검색할 범위를 선택하세요.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            val radiusOptions = listOf(
                RadiusOption("좁게 (250m)", 250),
                RadiusOption("보통 (1000m)", 1000),
                RadiusOption("넓게 (2000m)", 2000) // 요청하신 수치대로 1000 설정
            )

            Column(Modifier.selectableGroup()) {
                radiusOptions.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = (option.value == viewModel.searchRadius),
                                onClick = { viewModel.updateRadius(option.value) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option.value == viewModel.searchRadius),
                            onClick = null, // Row 클릭으로 처리
                            colors = RadioButtonDefaults.colors(selectedColor = mainGreenColor)
                        )
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Color(0xFFEEEEEE))

            // --- [신규] 개발자 설정 섹션 ---
            Text(
                text = "개발자 설정",
                fontSize = 14.sp,
                color = mainGreenColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "개발자 모드", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(text = "커스텀 WEB_URL을 사용합니다.", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = viewModel.isDevMode,
                    onCheckedChange = { viewModel.toggleDevMode(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = mainGreenColor)
                )
            }

            // 개발자 모드가 켜져 있을 때만 입력창 표시
            androidx.compose.animation.AnimatedVisibility(visible = viewModel.isDevMode) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    OutlinedTextField(
                        value = viewModel.customWebUrl,
                        onValueChange = { viewModel.updateCustomUrl(it) },
                        label = { Text("테스트 WEB_URL 입력") },
                        placeholder = { Text("https://example.vercel.app") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = mainGreenColor,
                            focusedLabelColor = mainGreenColor
                        )
                    )
                    Text(
                        text = "현재 적용된 주소로 모든 웹뷰가 로드됩니다.",
                        fontSize = 11.sp,
                        color = mainGreenColor,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }
            }
        }
    }
}

data class RadiusOption(val label: String, val value: Int)