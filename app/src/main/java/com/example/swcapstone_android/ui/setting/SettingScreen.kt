package com.example.swcapstone_android.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBackClick: () -> Unit,
    onInquiryClick: () -> Unit,
    viewModel: SettingViewModel
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAF7E4))
            )
        },
        containerColor = Color(0xFFFAF7E4)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAF7E4))
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
                    Text(text = "카테고리 필터 사용", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Gray)
                    Text(text = "지도에서 카테고리별로 핀을 필터링합니다.", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = viewModel.isCategoryEnabled,
                    onCheckedChange = { viewModel.toggleCategory(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = mainGreenColor)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Color(0xFFFAF7E4))

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
                RadiusOption("좁게 (500m)", 500),
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
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = Color(0xFFFAF7E4))

            Text(
                text = "고객센터 및 지원",
                fontSize = 14.sp,
                color = mainGreenColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "앱 사용 중 불편한 점이나 제안사항을 남겨주세요.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            Button(
                onClick = onInquiryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = mainGreenColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "1:1 문의하기 작성",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

data class RadiusOption(val label: String, val value: Int)