package com.example.swcapstone_android.ui.login

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.remote.RetrofitClient
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class LoginViewModelMockTest {

    private lateinit var application: Application
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: LoginViewModel

    private val validJsonOnboarded = """
        {
            "status": "SUCCESS",
            "data": {
                "accessToken": "mock_access_token",
                "refreshToken": "mock_refresh_token",
                "accessTokenExpiresIn": 3600,
                "onboarded": true
            },
            "message": null
        }
    """.trimIndent()

    private val validJsonNotOnboarded = """
        {
            "status": "SUCCESS",
            "data": {
                "accessToken": "mock_access_token",
                "refreshToken": "mock_refresh_token",
                "accessTokenExpiresIn": 3600,
                "onboarded": false
            },
            "message": null
        }
    """.trimIndent()

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        tokenManager = TokenManager(application)

        // RetrofitClient 싱글톤 Mock
        mockkObject(RetrofitClient)
        coEvery {
            RetrofitClient.instance.registerDevice(any(), any())
        } returns Response.success(null)

        // FirebaseMessaging Mock
        mockkStatic(FirebaseMessaging::class)
        val mockFirebase = mockk<FirebaseMessaging>()
        every { FirebaseMessaging.getInstance() } returns mockFirebase
        every { mockFirebase.token } returns Tasks.forResult("mock_fcm_token")

        viewModel = LoginViewModel(application)
    }

    @After
    fun tearDown() = runTest {
        tokenManager.clearTokens()
        unmockkAll()
    }

    // ─────────────────────────────────────────────
    // 콜백 호출 검증
    // ─────────────────────────────────────────────

    @Test
    fun FCM_Mock_환경에서_onboarded_true이면_콜백이_즉시_호출된다() {
        var result: Boolean? = null
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult(validJsonOnboarded) { onboarded ->
            result = onboarded
            latch.countDown()
        }

        latch.await(3, TimeUnit.SECONDS)
        assertEquals(true, result)
    }

    @Test
    fun FCM_Mock_환경에서_onboarded_false이면_콜백이_즉시_호출된다() {
        var result: Boolean? = null
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult(validJsonNotOnboarded) { onboarded ->
            result = onboarded
            latch.countDown()
        }

        latch.await(3, TimeUnit.SECONDS)
        assertEquals(false, result)
    }

    // ─────────────────────────────────────────────
    // 토큰 저장 검증
    // ─────────────────────────────────────────────

    @Test
    fun FCM_Mock_환경에서_토큰이_정확하게_저장된다() = runTest {
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult(validJsonOnboarded) { latch.countDown() }
        latch.await(3, TimeUnit.SECONDS)

        assertEquals("mock_access_token", tokenManager.accessToken.first())
        assertEquals("mock_refresh_token", tokenManager.refreshToken.first())
    }

    // ─────────────────────────────────────────────
    // FCM API 호출 검증
    // ─────────────────────────────────────────────

    @Test
    fun 로그인_성공시_registerDevice_API가_호출된다() {
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult(validJsonOnboarded) { latch.countDown() }
        latch.await(3, TimeUnit.SECONDS)

        coVerify { RetrofitClient.instance.registerDevice(any(), any()) }
    }

    @Test
    fun registerDevice_API_호출시_Bearer_토큰이_포함된다() {
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult(validJsonOnboarded) { latch.countDown() }
        latch.await(3, TimeUnit.SECONDS)

        coVerify {
            RetrofitClient.instance.registerDevice(
                token = "Bearer mock_access_token",
                request = any()
            )
        }
    }

    @Test
    fun registerDevice_API_호출시_deviceType이_ANDROID이다() {
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult(validJsonOnboarded) { latch.countDown() }
        latch.await(3, TimeUnit.SECONDS)

        coVerify {
            RetrofitClient.instance.registerDevice(
                token = any(),
                request = match { it.deviceType == "ANDROID" }
            )
        }
    }

    @Test
    fun registerDevice_API_호출시_FCM_토큰이_포함된다() {
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult(validJsonOnboarded) { latch.countDown() }
        latch.await(3, TimeUnit.SECONDS)

        coVerify {
            RetrofitClient.instance.registerDevice(
                token = any(),
                request = match { it.fcmToken == "mock_fcm_token" }
            )
        }
    }

    // ─────────────────────────────────────────────
    // FCM 실패 시 콜백 정상 호출 확인
    // ─────────────────────────────────────────────

    @Test
    fun registerDevice_실패해도_onSuccess_콜백은_정상_호출된다() {
        coEvery {
            RetrofitClient.instance.registerDevice(any(), any())
        } returns Response.error(500, "error".toResponseBody())

        var result: Boolean? = null
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult(validJsonOnboarded) { onboarded ->
            result = onboarded
            latch.countDown()
        }

        latch.await(3, TimeUnit.SECONDS)
        assertEquals(true, result)
    }

    @Test
    fun Firebase_예외_발생해도_onSuccess_콜백은_정상_호출된다() {
        val mockFirebase = mockk<FirebaseMessaging>()
        every { FirebaseMessaging.getInstance() } returns mockFirebase
        every { mockFirebase.token } throws RuntimeException("Firebase 초기화 실패")

        var result: Boolean? = null
        val latch = CountDownLatch(1)

        viewModel.handleLoginResult(validJsonOnboarded) { onboarded ->
            result = onboarded
            latch.countDown()
        }

        latch.await(3, TimeUnit.SECONDS)
        assertEquals(true, result)
    }
}
