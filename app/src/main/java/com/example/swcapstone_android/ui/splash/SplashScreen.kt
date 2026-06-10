package com.example.swcapstone_android.ui.splash

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import com.example.swcapstone_android.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    viewModel: SplashViewModel,
    onSplashFinished: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // GPS 보안 체크 상태
    var securityStatus by remember { mutableStateOf("CHECKING") }

    // 현재 기기에 진짜로 정밀 위치 권한이 붙어있는지 검사
    fun hasLocationPermissions(ctx: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fineLocation && coarseLocation
    }

    //  안드로이드 뼈대 시스템에 권한 팝업을 요청하고 응답
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isFineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val isCoarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (isFineGranted && isCoarseGranted) {
            val result = viewModel.checkGpsSecurity()
            securityStatus = result

            if (result == "NORMAL") {
                scope.launch {
                    delay(1500) // 최소 로고 구경 시간
                    viewModel.checkLoginStatus { destination -> onSplashFinished(destination) }
                }
            }
        } else {
            // 보안 검사는 패스하고 로그인 화면으로 이동시켜서 추후 유도
            securityStatus = "NORMAL"
            scope.launch {
                delay(1500)
                viewModel.checkLoginStatus { destination -> onSplashFinished(destination) }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermissions(context)) {
            // 이미 권한이 있는 기존 유저
            val result = viewModel.checkGpsSecurity()
            securityStatus = result

            if (result == "NORMAL") {
                delay(2000) // 2초 뒤에 워프
                viewModel.checkLoginStatus { destination -> onSplashFinished(destination) }
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.dodo)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
            contentDescription = "App Logo",
            modifier = Modifier.size(150.dp)
        )

        // GPS 위변조 단속 철창 다이얼로그
        if (securityStatus != "CHECKING" && securityStatus != "NORMAL") {
            val dialogTitle = "보안 경고"
            val dialogMessage = if (securityStatus == "FAKE_APP") {
                "기기 내에 위치 조작(Fake GPS) 앱이 감지되었습니다.\n안전한 서비스 이용을 위해 해당 앱을 삭제하신 후 다시 실행해 주세요."
            } else {
                "개발자 옵션의 '모의 위치(Mock Location)' 설정이 활성화되어 있습니다.\n설정을 해제하신 후 다시 실행해 주세요."
            }

            AlertDialog(
                onDismissRequest = { /* 백버튼 탈출 꼼수 원천 봉쇄 */ },
                title = { Text(text = dialogTitle, fontWeight = FontWeight.Bold, color = Color.Red) },
                text = { Text(text = dialogMessage, fontSize = 14.sp, color = Color.Black) },
                confirmButton = {
                    Button(
                        onClick = {
                            // 프로세스 잔여물 없이 깔끔하게 앱 프로세스 강제종료
                            (context as? Activity)?.finishAffinity()
                            System.exit(0)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.dodo))
                    ) {
                        Text("확인", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}