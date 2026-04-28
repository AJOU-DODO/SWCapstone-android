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
import com.example.swcapstone_android.ui.home.HomeScreen
import com.example.swcapstone_android.ui.login.LoginScreen
import com.example.swcapstone_android.ui.login.LoginViewModel
import com.example.swcapstone_android.ui.setting.SettingScreen
import com.example.swcapstone_android.ui.setting.SettingViewModel
import com.example.swcapstone_android.ui.splash.SplashViewModel
import com.example.swcapstone_android.ui.userdetail.UserDetailScreen
import com.example.swcapstone_android.ui.userdetail.UserDetailViewModel
import com.example.swcapstone_android.ui.write.WriteScreen
import com.example.swcapstone_android.ui.write.WriteViewModel
import kotlinx.coroutines.launch

@Composable
fun NavGraph(modifier: Modifier = Modifier, navController: NavHostController){
    NavHost(navController = navController, startDestination = Screen.SplashScreen.route){
        composable(route = Screen.SplashScreen.route) {
            val splashViewModel: SplashViewModel = viewModel()

            SplashScreen(
                viewModel = splashViewModel,
                onSplashFinished = { destination ->

                    navController.navigate(destination) {
                        popUpTo(Screen.SplashScreen.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.LoginScreen.route) {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { isOnboarded ->
                    val destination = if (isOnboarded) {
                        Screen.HomeScreen.route
                    } else {
                        Screen.DetailScreen.route
                    }

                    navController.navigate(destination) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.DetailScreen.route) {
            val userDetailViewModel: UserDetailViewModel = viewModel()
            UserDetailScreen(
                viewModel = userDetailViewModel,
                onComplete = {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true } // 뒤로가기 금지
                    }
                }
            )
        }

        composable(Screen.HomeScreen.route) {
            HomeScreen(
                onNavigateToSetting = {
                    navController.navigate("setting") // Screen 클래스에 정의했다면 Screen.SettingScreen.route
                },
                onNavigateToWrite = { lat, lng ->
                    navController.navigate("write/$lat/$lng")
                }
            )
        }

        composable("setting") {
            val settingViewModel: SettingViewModel = viewModel()
            SettingScreen(
                onBackClick = {
                    navController.popBackStack() // 뒤로가기
                },
                viewModel = settingViewModel
            )
        }

        composable("write/{lat}/{lng}") { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDouble() ?: 0.0
            val lng = backStackEntry.arguments?.getString("lng")?.toDouble() ?: 0.0

            val writeViewModel: WriteViewModel = viewModel()
            WriteScreen(
                onBackClick = { navController.popBackStack() },
                lat = lat,
                lng = lng,
                viewModel = writeViewModel
            )
        }
    }
}