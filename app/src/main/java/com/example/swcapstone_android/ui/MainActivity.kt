package com.example.swcapstone_android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.example.swcapstone_android.ui.theme.SWCapstoneandroidTheme

class MainActivity : ComponentActivity() {

    private var nestIdState by mutableStateOf<String?>(null)
    private var notificationTypeState by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialNestId = intent.getStringExtra("SELECTED_NEST_ID")
        val initialNotificationType = intent.getStringExtra("NOTIFICATION_TYPE")

        if (initialNestId != null) {
            nestIdState = initialNestId
            notificationTypeState = initialNotificationType
            Log.d("FCM_ROUTING", "앱 종료 상태에서 알림 구동 (NEST) -> nestId: $initialNestId")
        } else if (initialNotificationType == "POSTCARD") {
            nestIdState = "0"
            notificationTypeState = initialNotificationType
            Log.d("FCM_ROUTING", "앱 종료 상태에서 알림 구동 (POSTCARD) -> type만 감지")
        }

        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()

            LaunchedEffect(nestIdState, notificationTypeState) {
                if (nestIdState != null) {
                    val id = nestIdState
                    val type = notificationTypeState

                    Log.d("FCM_ROUTING", "알림 처리 시작 -> type: $type, nestId: $id")

                    navController.navigate("alarm_screen/$id?type=$type") {
                        launchSingleTop = true
                    }

                    nestIdState = null
                    notificationTypeState = null
                }
            }
            SWCapstoneandroidTheme {
                val view = LocalView.current
                if (!view.isInEditMode) {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).apply {
                        hide(WindowInsetsCompat.Type.navigationBars())
                        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
                Scaffold(
                    modifier = Modifier.Companion.fillMaxSize(),

                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) { innerPadding ->
                    NavGraph(modifier = Modifier.padding(innerPadding),
                        navController = navController,
                        startSelectedNestId = nestIdState ?: initialNestId,
                        startNotificationType = notificationTypeState ?: initialNotificationType)
                }
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        val nestId = intent.getStringExtra("SELECTED_NEST_ID")
        val notificationType = intent.getStringExtra("NOTIFICATION_TYPE")

        if (nestId != null) {
            nestIdState = nestId
            notificationTypeState = notificationType
        }
    }

    fun triggerNewIntentForTest(intent: Intent) {
        onNewIntent(intent)
    }
}