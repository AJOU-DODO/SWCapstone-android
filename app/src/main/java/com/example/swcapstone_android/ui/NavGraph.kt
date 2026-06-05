package com.example.swcapstone_android.ui

import android.app.Notification
import com.example.swcapstone_android.ui.splash.SplashScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
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
import com.example.swcapstone_android.ui.alarm.AlarmScreen
import com.example.swcapstone_android.ui.alarm.AlarmViewModel

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startSelectedNestId: String? = null,
    startNotificationType: String? = null
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
                        finalRoute = if (startNotificationType == "NEST" || startNotificationType == "POSTCARD") {
                            "alarm_screen/$startSelectedNestId?type=$startNotificationType"
                        } else {
                            Screen.HomeScreen.route + "?initialSelectedNestId=$startSelectedNestId"
                        }
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
            route = Screen.HomeScreen.route + "?initialSelectedNestId={initialSelectedNestId}",
            arguments = listOf(
                navArgument("initialSelectedNestId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val argumentNestId = backStackEntry.arguments?.getString("initialSelectedNestId")
            val finalNestId = argumentNestId ?: startSelectedNestId
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
                onNavigateToMypage = { navController.navigate("mypage_graph") },
                onNavigateToCategory = { navController.navigate("category") }
            )
        }

        composable(
            route = "alarm_screen/{nestId}?type={type}",
            arguments = listOf(
                navArgument("nestId") { type = NavType.StringType },
                navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val nestId = backStackEntry.arguments?.getString("nestId") ?: ""
            // 주소창에서 추출한 type 값 (NEST 또는 POSTCARD)
            val notificationType = backStackEntry.arguments?.getString("type")

            val alarmViewModel: AlarmViewModel = viewModel()

            AlarmScreen(
                nestId = nestId,
                notificationType = notificationType, // 🌟 AlarmScreen 컴포저블 내부로 꽂아주기!
                viewModel = alarmViewModel,
                onBackClick = {
                    navController.navigate(Screen.HomeScreen.route) {
                        // 뒤로가기 시 쿼리스트링을 포함한 정확한 라우트 매칭 청소
                        popUpTo("alarm_screen/$nestId?type=$notificationType") { inclusive = true }
                    }
                }
            )
        }

        composable("setting") {
            val settingViewModel: SettingViewModel = viewModel()
            SettingScreen(
                onBackClick = {
                    navController.popBackStack() // 뒤로가기
                },
                onInquiryClick = { navController.navigate("inquiry") },
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

        navigation(startDestination = "mypage", route = "mypage_graph") {
            composable("mypage") { backStackEntry ->
                // 부모 그래프(mypage_graph)의 백스택 엔트리를 가져옴
                val mypageGraphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("mypage_graph")
                }
                // 부모 스코프의 뷰모델로 인스턴스 생성
                val mypageViewModel: MypageViewModel = viewModel(mypageGraphEntry)

                MypageScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToPostcard = { navController.navigate("postcard") },
                    viewModel = mypageViewModel
                )
            }

            composable("postcard") { backStackEntry ->
                // 똑같이 부모 그래프(mypage_graph)의 백스택 엔트리를 공유
                val mypageGraphEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("mypage_graph")
                }
                // 마이페이지와 완벽히 동일한 뷰모델 인스턴스를 보장받음
                val mypageViewModel: MypageViewModel = viewModel(mypageGraphEntry)
                val postcardViewModel: PostcardViewModel = viewModel()

                PostcardScreen(
                    onBackClick = { navController.popBackStack() },
                    postcardViewModel = postcardViewModel,
                    mypageViewModel = mypageViewModel
                )
            }
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