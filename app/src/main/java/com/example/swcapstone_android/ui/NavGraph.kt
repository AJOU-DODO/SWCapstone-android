package com.example.swcapstone_android.ui

import com.example.swcapstone_android.ui.splash.SplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.swcapstone_android.ui.login.LoginScreen
import com.example.swcapstone_android.ui.login.LoginViewModel
import com.example.swcapstone_android.ui.splash.SplashViewModel
import com.example.swcapstone_android.ui.userdetail.UserDetailScreen
import com.example.swcapstone_android.ui.userdetail.UserDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun NavGraph(modifier: Modifier = Modifier, navController: NavHostController){
    NavHost(navController = navController, startDestination = Screen.SplashScreen.route){
        composable(route = Screen.SplashScreen.route) {
            val splashViewModel: SplashViewModel = viewModel()
            val scope = rememberCoroutineScope()

            SplashScreen(
                viewModel = splashViewModel,
                onSplashFinished = {
                    scope.launch {
                        kotlinx.coroutines.delay(2000)
                        navController.navigate(Screen.LoginScreen.route) {
                            popUpTo(Screen.SplashScreen.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("login") {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate("user_detail") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("user_detail") {
            val userDetailViewModel: UserDetailViewModel = viewModel()
            UserDetailScreen(
                viewModel = userDetailViewModel,
                onComplete = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true } // 로그인/상세화면은 뒤로가기 안 되게 제거
                    }
                }
            )
        }
    }
}