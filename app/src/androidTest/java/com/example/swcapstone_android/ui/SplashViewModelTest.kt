package com.example.swcapstone_android.ui

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.ui.Screen
import com.example.swcapstone_android.ui.splash.SplashViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SplashViewModelTest {

    private lateinit var application: Application
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: SplashViewModel

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        tokenManager = TokenManager(application)
        viewModel = SplashViewModel(application)
    }

    @After
    fun tearDown() = runTest {
        tokenManager.clearTokens()
    }

    @Test
    fun 액세스토큰이_없으면_LoginScreen으로_이동한다() = runTest {
        // Given: 토큰 없는 상태 (tearDown에서 초기화되므로 기본 상태)
        tokenManager.clearTokens()

        var result = ""
        val latch = java.util.concurrent.CountDownLatch(1)

        viewModel.checkLoginStatus { destination ->
            result = destination
            latch.countDown()
        }

        latch.await(3, java.util.concurrent.TimeUnit.SECONDS)

        assertEquals(Screen.LoginScreen.route, result)
    }

    @Test
    fun 리프레시토큰이_없으면_LoginScreen으로_이동한다() = runTest {
        tokenManager.clearTokens()

        var result = ""
        val latch = java.util.concurrent.CountDownLatch(1)

        viewModel.checkLoginStatus { destination ->
            result = destination
            latch.countDown()
        }

        latch.await(3, java.util.concurrent.TimeUnit.SECONDS)

        assertEquals(Screen.LoginScreen.route, result)
    }

    @Test
    fun checkLoginStatus_호출시_destination이_비어있지_않다() = runTest {
        // 토큰 유무와 무관하게 콜백이 반드시 호출되어야 함
        var result = ""
        val latch = java.util.concurrent.CountDownLatch(1)

        viewModel.checkLoginStatus { destination ->
            result = destination
            latch.countDown()
        }

        val completed = latch.await(5, java.util.concurrent.TimeUnit.SECONDS)

        assert(completed) { "checkLoginStatus 콜백이 5초 내에 호출되지 않았습니다." }
        assert(result.isNotEmpty()) { "destination이 비어있습니다." }
    }

    @Test
    fun checkLoginStatus_결과는_유효한_라우트_중_하나이다() = runTest {
        val validRoutes = setOf(
            Screen.LoginScreen.route,
            Screen.HomeScreen.route,
            Screen.DetailScreen.route
        )

        var result = ""
        val latch = java.util.concurrent.CountDownLatch(1)

        viewModel.checkLoginStatus { destination ->
            result = destination
            latch.countDown()
        }

        latch.await(5, java.util.concurrent.TimeUnit.SECONDS)

        assert(result in validRoutes) {
            "유효하지 않은 destination: $result"
        }
    }
}
