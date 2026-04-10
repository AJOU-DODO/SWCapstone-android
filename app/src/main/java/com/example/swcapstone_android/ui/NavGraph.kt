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
        composable(route = Screen.LoginScreen.route) {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(viewModel = loginViewModel) {
                // navController.navigate(Screen.NextScreen.route)
            }
        }
        /*composable(route = Screen.NextScreen.route) {
            NextScreen()
        }*/
    }
}