package com.example.swcapstone_android.ui

import com.example.swcapstone_android.ui.splash.SplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.swcapstone_android.ui.category.CategoryScreen
import com.example.swcapstone_android.ui.category.CategoryViewModel
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
import com.example.swcapstone_android.ui.unlock.UnlockScreen
import com.example.swcapstone_android.ui.unlock.UnlockViewModel
import com.example.swcapstone_android.ui.mypage.MypageScreen
import com.example.swcapstone_android.ui.mypage.MypageViewModel
import com.example.swcapstone_android.ui.postcard.PostcardScreen
import com.example.swcapstone_android.ui.postcard.PostcardViewModel

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startSelectedNestId: String? = null
){
    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route,
        modifier = modifier
    ) {
        composable(route = Screen.SplashScreen.route) {
            val splashViewModel: SplashViewModel = viewModel()

            SplashScreen(
                viewModel = splashViewModel,
                onSplashFinished = { destination ->
                    var finalRoute = destination

                    if (destination == Screen.HomeScreen.route && startSelectedNestId != null) {
                        finalRoute = Screen.HomeScreen.route + "?nestId=$startSelectedNestId"
                    }

                    navController.navigate(finalRoute) {
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

        composable(
            Screen.HomeScreen.route + "?nestId={nestId}"
        ) { backStackEntry ->
            val nestId = backStackEntry.arguments?.getString("nestId")
            val finalNestId = nestId ?: startSelectedNestId
            HomeScreen(
                initialSelectedNestId = finalNestId,
                onNavigateToSetting = {
                    navController.navigate("setting") // Screen 클래스에 정의했다면 Screen.SettingScreen.route
                },
                onNavigateToWrite = { lat, lng ->
                    navController.navigate("write/$lat/$lng")
                },
                onNavigateToUnlock = { id ->
                    navController.navigate("unlock/$id")
                },
                onNavigateToMypage = { navController.navigate("mypage") },
                onNavigateToCategory = { navController.navigate("category") }
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

        composable("unlock/{nestId}") { backStackEntry ->
            val nestId = backStackEntry.arguments?.getString("nestId")?.toLong() ?: 0L
            val unlockViewModel: UnlockViewModel = viewModel()

             UnlockScreen(
                 nestId = nestId,
                 viewModel = unlockViewModel,
                 onFinished = { navController.popBackStack() }
             )
        }

        composable("mypage") {
            val mypageViewModel: MypageViewModel = viewModel()
            MypageScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToPostcard = { navController.navigate("postcard") },
                viewModel = mypageViewModel
            )
        }

        composable("postcard") {
            val mypageBackStackEntry = remember(navController) {
                navController.getBackStackEntry("mypage")
            }
            val mypageViewModel: MypageViewModel = viewModel(mypageBackStackEntry)
            val postcardViewModel: PostcardViewModel = viewModel()
            PostcardScreen(
                onBackClick = { navController.popBackStack() },
                postcardViewModel = postcardViewModel,
                mypageViewModel = mypageViewModel
            )
        }

        composable("category") {
            val categoryViewModel: CategoryViewModel = viewModel()
            CategoryScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = categoryViewModel
            )
        }
    }
}