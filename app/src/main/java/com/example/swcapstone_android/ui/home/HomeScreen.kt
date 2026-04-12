package com.example.swcapstone_android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.swcapstone_android.ui.theme.DODO

// 임시 화면
@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DODO),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "HOME",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF386641),
            style = MaterialTheme.typography.headlineLarge
        )
    }
}