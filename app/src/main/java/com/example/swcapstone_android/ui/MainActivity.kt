package com.example.swcapstone_android.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialNestId = intent.getStringExtra("SELECTED_NEST_ID")
        enableEdgeToEdge()

        setContent {
            val navController = rememberNavController()
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
                        startSelectedNestId = nestIdState ?: initialNestId)
                }
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val nestId = intent.getStringExtra("SELECTED_NEST_ID")
        if (nestId != null) {
            nestIdState = nestId
        }
    }
}