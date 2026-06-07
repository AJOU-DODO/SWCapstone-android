package com.example.swcapstone_android.ui

import android.app.Application
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.TokenData
import com.example.swcapstone_android.data.model.TokenResponse
import com.example.swcapstone_android.data.remote.ApiService
import com.example.swcapstone_android.data.remote.RetrofitClient
import com.example.swcapstone_android.ui.splash.SplashViewModel
import com.example.swcapstone_android.util.GeofenceManager
import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class SplashTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var context: Application
    private lateinit var tokenManager: TokenManager
    private lateinit var geofenceManager: GeofenceManager
    private lateinit var apiService: ApiService
    private lateinit var viewModel: SplashViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        context = mockk(relaxed = true)
        tokenManager = mockk(relaxed = true)
        geofenceManager = mockk(relaxed = true)
        apiService = mockk(relaxed = true)

        // 싱글톤 구조의 RetrofitClient 대가리를 Mocking으로 제어하기 위함
        mockkObject(RetrofitClient)
        every { RetrofitClient.instance } returns apiService

        // FirebaseMessaging 정적 메서드 가로채기 모킹 및 가짜 토큰 반환 설정
        mockkStatic(FirebaseMessaging::class)
        val mockFirebase = mockk<FirebaseMessaging>(relaxed = true)
        every { FirebaseMessaging.getInstance() } returns mockFirebase
        // Task<String>이 await() 될 때를 대비해 기본 문자열을 뱉도록 완충재 주입
        coEvery { mockFirebase.token.await() } returns "fake_fcm_token"

        coEvery { apiService.registerDevice(any(), any()) } returns Response.success(Unit)

        // 뷰모델 생성 주입
        viewModel = SplashViewModel(context)

        // 내부 private 전역변수 강제 Mock 덮어쓰기 (리플렉션 기믹)
        val tokenManagerField = SplashViewModel::class.java.getDeclaredField("tokenManager")
        tokenManagerField.isAccessible = true
        tokenManagerField.set(viewModel, tokenManager)

        val geofenceField = SplashViewModel::class.java.getDeclaredField("geofenceManager")
        geofenceField.isAccessible = true
        geofenceField.set(viewModel, geofenceManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `로컬_토큰이_없으면_로그인_화면으로_이동한다`() = runTest(testDispatcher) {
        // Given: 토큰 플로우를 빈 값(null)으로 흐르게 설정
        every { tokenManager.accessToken } returns flowOf(null)
        every { tokenManager.refreshToken } returns flowOf(null)

        var destinationRoute = ""

        // When: 로그인 상태 체크 비즈니스 로직 발동
        viewModel.checkLoginStatus { route ->
            destinationRoute = route
        }

        // 코루틴 내의 로직들이 완전히 끝날 때까지 대기
        advanceUntilIdle()

        // Then: 지오펜스 초기화 함수가 불렸는지 체크하고, 최종 목적지가 Login 이 맞는지 단언(Assert)
        verify { geofenceManager.removeAllGeofences() }
        Assert.assertEquals(Screen.LoginScreen.route, destinationRoute)
    }

    @Test
    fun `토큰_재발급_성공하고_온보딩도_완료된_유저라면_홈_화면으로_이동한다`() = runTest(testDispatcher) {
        // Given: 로컬에 기존 토큰들이 저장되어 있다고 설정
        every { tokenManager.accessToken } returns flowOf("old_access_token")
        every { tokenManager.refreshToken } returns flowOf("old_refresh_token")

        // 서버에서 줄 가짜 성공 응답 DTO 생성 (onboarded = true)
        val fakeTokenData = TokenData(
            accessToken = "new_access_token",
            refreshToken = "new_refresh_token",
            onboarded = true // 🌟 온보딩 끝난 상태!
        )
        val fakeResponse = Response.success(
            TokenResponse(status = "SUCCESS", data = fakeTokenData)
        )

        coEvery { apiService.reissueToken(any()) } returns fakeResponse

        var destinationRoute = ""

        // When: 로직 구동
        viewModel.checkLoginStatus { route ->
            destinationRoute = route
        }
        advanceUntilIdle()

        // Then: 새로 발급받은 안심 토큰들이 저장소에 온전히 Update 되었는지 검증하고 Home 라우팅 단언!
        coVerify { tokenManager.saveTokens("new_access_token", "new_refresh_token") }
        Assert.assertEquals(Screen.HomeScreen.route, destinationRoute)
    }

    @Test
    fun `토큰은_있으나_온보딩이_미완료된_유저라면_프로필_설정_화면으로_이동한다`() = runTest(testDispatcher) {
        // Given: 로컬에 기존 토큰이 있다고 모킹
        every { tokenManager.accessToken } returns flowOf("old_access_token")
        every { tokenManager.refreshToken } returns flowOf("old_refresh_token")

        // 🌟 1. 뷰모델 body()!!.data 구조와 백엔드 명세가 일치하도록 가짜 데이터 정밀 조립
        val fakeTokenData = TokenData(
            accessToken = "new_access_token",
            refreshToken = "new_refresh_token",
            onboarded = false // 👈 온보딩 미완료 유저 세팅!
        )

        val fakeResponse = Response.success(
            TokenResponse(status = "SUCCESS", data = fakeTokenData)
        )
        coEvery { apiService.reissueToken(any()) } returns fakeResponse

        var destinationRoute = ""

        // When: 로직 구동
        viewModel.checkLoginStatus { route ->
            destinationRoute = route
        }

        // 가상 코루틴 시계의 모든 이벤트를 남김없이 탈탈 털어서 실행 완료시키기
        advanceUntilIdle()

        // Then: 이제 빈 값이 아니라 정석대로 detail_screen 주소로 견인하는지 단언!
        Assert.assertEquals(Screen.DetailScreen.route, destinationRoute)
    }
}