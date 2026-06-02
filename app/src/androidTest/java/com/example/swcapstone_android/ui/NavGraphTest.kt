package com.example.swcapstone_android.ui

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavGraphTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun 앱_시작_시_기본_출발지는_스플래시_화면이다() {
        // Given & When: 가짜 NavController를 준비하고 NavGraph를 컴포즈 화면에 올림
        lateinit var navController: TestNavHostController

        composeTestRule.setContent {
            val context = LocalContext.current
            navController = TestNavHostController(context).apply {
                // 테스트할 때 라이프사이클 에러가 나지 않도록 바인딩
                navigatorProvider.addNavigator(androidx.navigation.compose.ComposeNavigator())
            }
            NavGraph(navController = navController)
        }

        // Then: 현재 백스택의 최상단 라우트가 SplashScreen의 경로와 일치하는지 검증
        // (Screen.SplashScreen.route가 "splash"라고 가정)
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        assertEquals("splash_screen", currentRoute)
    }

    @Test
    fun 일반_알림_인텐트를_품고_스플래시가_종료되면_둥지ID_파라미터를_가지고_홈화면으로_이동한다() {
        // Given: 일반 알림 데이터(둥지 ID: 123)를 NavGraph에 주입한 상황 시뮬레이션
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavGraph(
                navController = navController,
                startSelectedNestId = "123",
                startNotificationType = "NORMAL_NOTIFICATION"
            )
        }

        // When: 스플래시 화면 내부에서 finished 처리가 되었다고 가정하여 가상 시간 속행
        // 런타임 딜레이 타임(예: 2초)을 강제로 흘려보내 라우팅을 발동시킵니다.
        composeTestRule.mainClock.advanceTimeBy(2500)

        // Then: 홈 화면의 컴포넌트 타이틀인 "DODO" 텍스트가 정상 노출되는지 검증
        composeTestRule
            .onNodeWithText("DODO")
            .assertIsDisplayed()
    }

    @Test
    fun NEST_LIKE_알림_인텐트를_품고_스플래시가_종료되면_알림_상세_화면으로_직행한다() {
        // Given: '좋아요' 알림 데이터(둥지 ID: 999)를 주입
        composeTestRule.setContent {
            val navController = rememberNavController()
            NavGraph(
                navController = navController,
                startSelectedNestId = "999",
                startNotificationType = "NEST_LIKE" // 🌟 좋아요 타입
            )
        }

        // When: 스플래시 완료 딜레이 속행
        composeTestRule.mainClock.advanceTimeBy(2500)

        // Then: "alarm_screen/999"로 튕겼는지 검증하기 위해
        // 알림 상세 화면(AlarmScreen) 내부의 유니크한 UI 텍스트가 노출되었는지 확인
        // (실제 알림 창 내부 텍스트인 "알림" 혹은 네가 정한 대표 타이틀로 매칭해줘)
        composeTestRule
            .onNodeWithText("알림")
            .assertIsDisplayed()
    }

    @Test
    fun 홈화면에서_카테고리_버튼을_클릭하면_카테고리_화면으로_라우트가_이동한다() {
        // Given: 처음부터 홈 화면으로 바로 진입하여 로드 완료된 상태 구축
        lateinit var navController: TestNavHostController
        composeTestRule.setContent {
            val context = LocalContext.current
            navController = TestNavHostController(context).apply {
                navigatorProvider.addNavigator(androidx.navigation.compose.ComposeNavigator())
            }
            NavGraph(navController = navController, startSelectedNestId = null)
        }

        // 스플래시 스킵하여 홈으로 안착
        composeTestRule.mainClock.advanceTimeBy(2500)

        // When: 홈 화면 하단에 있는 '카테고리' 메뉴를 가상으로 클릭 시뮬레이션
        // (HomeScreen 내부 메뉴의 contentDescription 이나 Text가 "카테고리" 라고 가정)
        composeTestRule.onNodeWithText("카테고리").performClick()

        // Then: navController의 최상단 라우트 상태 주소가 "category"로 바뀌었는지 정밀 검증
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        assertEquals("category", currentRoute)
    }
}