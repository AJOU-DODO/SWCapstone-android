package com.example.swcapstone_android.ui

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.ui.login.LoginViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class LoginViewModelTest {

    private lateinit var application: Application
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: LoginViewModel

    // 테스트용 유효한 JSON (onboarded = true)
    private val validJsonOnboarded = """
        {
            "status": "SUCCESS",
            "data": {
                "accessToken": "test_access_token",
                "refreshToken": "test_refresh_token",
                "accessTokenExpiresIn": 3600,
                "onboarded": true
            },
            "message": null
        }
    """.trimIndent()

    // 테스트용 유효한 JSON (onboarded = false)
    private val validJsonNotOnboarded = """
        {
            "status": "SUCCESS",
            "data": {
                "accessToken": "test_access_token",
                "refreshToken": "test_refresh_token",
                "accessTokenExpiresIn": 3600,
                "onboarded": false
            },
            "message": null
        }
    """.trimIndent()

    // status가 SUCCESS가 아닌 경우
    private val failJson = """
        {
            "status": "FAIL",
            "data": null,
            "message": "인증 실패"
        }
    """.trimIndent()

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        tokenManager = TokenManager(application)
        viewModel = LoginViewModel(application)
    }

    @After
    fun tearDown() = runTest {
        tokenManager.clearTokens()
    }

    @Test
    fun 유효한_JSON이고_SUCCESS이면_onSuccess_콜백이_호출된다() {
        var callbackCalled = false
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult(validJsonOnboarded) { _ ->
            callbackCalled = true
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)
        assertTrue("onSuccess 콜백이 호출되지 않았습니다.", callbackCalled)
    }

    @Test
    fun onboarded가_true이면_콜백에서_true를_반환한다() {
        var result: Boolean? = null
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult(validJsonOnboarded) { onboarded ->
            result = onboarded
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)
        assertEquals(true, result)
    }

    @Test
    fun onboarded가_false이면_콜백에서_false를_반환한다() {
        var result: Boolean? = null
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult(validJsonNotOnboarded) { onboarded ->
            result = onboarded
            latch.countDown()
        }

        latch.await(15, TimeUnit.SECONDS)
        assertEquals(false, result)
    }

    @Test
    fun 유효한_JSON이면_TokenManager에_토큰이_저장된다() = runTest {
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult(validJsonOnboarded) {
            latch.countDown()
        }

        latch.await(5, TimeUnit.SECONDS)

        val savedAccess = tokenManager.accessToken.first()
        val savedRefresh = tokenManager.refreshToken.first()

        assertEquals("test_access_token", savedAccess)
        assertEquals("test_refresh_token", savedRefresh)
    }

    @Test
    fun status가_SUCCESS가_아니면_onSuccess_콜백이_호출되지_않는다() {
        var callbackCalled = false
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult(failJson) {
            callbackCalled = true
            latch.countDown()
        }

        // 콜백이 호출되지 않아야 하므로 타임아웃까지 기다림
        val completed = latch.await(3, TimeUnit.SECONDS)

        assertFalse("FAIL 상태에서 onSuccess가 호출되었습니다.", callbackCalled)
        assertFalse(completed)
    }

    @Test
    fun 잘못된_JSON이면_onSuccess_콜백이_호출되지_않는다() {
        var callbackCalled = false
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult("invalid_json_string!!!") {
            callbackCalled = true
            latch.countDown()
        }

        val completed = latch.await(3, TimeUnit.SECONDS)

        assertFalse("잘못된 JSON에서 onSuccess가 호출되었습니다.", callbackCalled)
        assertFalse(completed)
    }

    @Test
    fun 빈_문자열_입력시_onSuccess_콜백이_호출되지_않는다() {
        var callbackCalled = false
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult("") {
            callbackCalled = true
            latch.countDown()
        }

        val completed = latch.await(3, TimeUnit.SECONDS)

        assertFalse("빈 문자열에서 onSuccess가 호출되었습니다.", callbackCalled)
        assertFalse(completed)
    }
}
