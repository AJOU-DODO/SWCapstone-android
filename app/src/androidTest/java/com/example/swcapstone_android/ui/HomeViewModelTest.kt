package com.example.swcapstone_android.ui

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.InterestData
import com.example.swcapstone_android.data.model.InterestResponse
import com.example.swcapstone_android.data.model.PinData
import com.example.swcapstone_android.data.model.PinResponse
import com.example.swcapstone_android.data.remote.RetrofitClient
import com.example.swcapstone_android.ui.home.HomeViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HomeViewModelMockTest {

    private lateinit var application: Application
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    private val dummyPin = PinData(
        id = 1L,
        latitude = 37.5665,
        longitude = 126.9780,
        isAd = false
    )

    private val dummyPin2 = PinData(
        id = 2L,
        latitude = 37.5700,
        longitude = 126.9800,
        isAd = false
    )

    private val dummyAdPin = PinData(
        id = 99L,
        latitude = 37.5600,
        longitude = 126.9700,
        isAd = true
    )

    private val dummyInterestResponse = InterestResponse(
        status = "SUCCESS",
        code = "200",
        message = null,
        data = listOf(
            InterestData(id = 1, name = "자연", createdAt = "2024-01-01T00:00:00"),
            InterestData(id = 2, name = "역사", createdAt = "2024-01-01T00:00:00")
        )
    )

    private val emptyInterestResponse = InterestResponse(
        status = "SUCCESS",
        code = "200",
        message = null,
        data = emptyList()
    )

    private fun makePinResponse(pins: List<PinData>) = PinResponse(
        status = "SUCCESS",
        code = "200",
        message = "ok",
        data = pins
    )

    private fun setupDefaultMocks(
        normalPins: List<PinData> = emptyList(),
        adPins: List<PinData> = emptyList(),
        interestResponse: InterestResponse = emptyInterestResponse
    ) {
        coEvery { RetrofitClient.instance.getUserInterests(any()) } returns
                Response.success(interestResponse)
        coEvery { RetrofitClient.instance.getNearbyPins(any(), any(), any(), any(), any()) } returns
                Response.success(makePinResponse(normalPins))
        coEvery { RetrofitClient.instance.getAdPins(any()) } returns
                Response.success(makePinResponse(adPins))
    }

    // viewModelScope 코루틴이 완료될 때까지 폴링 대기
    private fun waitUntil(timeoutMs: Long = 3000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            Thread.sleep(50)
        }
    }

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        tokenManager = TokenManager(application)
        runBlocking { tokenManager.saveTokens("test_access_token", "test_refresh_token") }
        mockkObject(RetrofitClient)
        viewModel = HomeViewModel(application)
    }

    @After
    fun tearDown() {
        runBlocking { tokenManager.clearTokens() }
        unmockkAll()
    }

    @Test
    fun fetchNearbyPins_성공시_markers에_핀이_추가된다() {
        setupDefaultMocks(normalPins = listOf(dummyPin))

        viewModel.fetchNearbyPins(37.5665, 126.9780)
        waitUntil { viewModel.markers.isNotEmpty() }

        assertTrue(viewModel.markers.isNotEmpty())
    }

    @Test
    fun fetchNearbyPins_성공시_markers_size가_응답과_일치한다() {
        setupDefaultMocks(normalPins = listOf(dummyPin, dummyPin2))

        viewModel.fetchNearbyPins(37.5665, 126.9780)
        waitUntil { viewModel.markers.size == 2 }

        assertEquals(2, viewModel.markers.size)
    }

    @Test
    fun fetchNearbyPins_광고핀도_markers에_포함된다() {
        setupDefaultMocks(adPins = listOf(dummyAdPin))

        viewModel.fetchNearbyPins(37.5665, 126.9780)
        waitUntil { viewModel.markers.any { it.isAd } }

        assertTrue(viewModel.markers.any { it.isAd })
    }

    @Test
    fun fetchNearbyPins_일반핀과_광고핀이_합산되어_markers에_들어간다() {
        setupDefaultMocks(normalPins = listOf(dummyPin, dummyPin2), adPins = listOf(dummyAdPin))

        viewModel.fetchNearbyPins(37.5665, 126.9780)
        waitUntil { viewModel.markers.size == 3 }

        assertEquals(3, viewModel.markers.size)
    }

    @Test
    fun fetchNearbyPins_카테고리필터_활성시_getUserInterests가_호출된다() {
        setupDefaultMocks(interestResponse = dummyInterestResponse)

        viewModel.fetchNearbyPins(37.5665, 126.9780)
        // getUserInterests 호출 완료까지 대기 (markers는 비어있을 수 있으므로 고정 대기)
        Thread.sleep(2000)

        coVerify { RetrofitClient.instance.getUserInterests(any()) }
    }

    // ─────────────────────────────────────────────
    // 실패 케이스 — 조건 충족 불가 → 고정 대기
    // ─────────────────────────────────────────────

    @Test
    fun fetchNearbyPins_서버_에러시_markers가_비어있다() {
        coEvery { RetrofitClient.instance.getUserInterests(any()) } returns
                Response.success(emptyInterestResponse)
        coEvery { RetrofitClient.instance.getNearbyPins(any(), any(), any(), any(), any()) } returns
                Response.error(500, "서버 에러".toResponseBody())
        coEvery { RetrofitClient.instance.getAdPins(any()) } returns
                Response.error(500, "서버 에러".toResponseBody())

        viewModel.fetchNearbyPins(37.5665, 126.9780)
        Thread.sleep(2000)

        assertTrue(viewModel.markers.isEmpty())
    }

    @Test
    fun fetchNearbyPins_네트워크_예외_발생시_markers가_비어있다() {
        coEvery { RetrofitClient.instance.getUserInterests(any()) } throws RuntimeException("네트워크 오류")
        coEvery { RetrofitClient.instance.getNearbyPins(any(), any(), any(), any(), any()) } throws RuntimeException("네트워크 오류")
        coEvery { RetrofitClient.instance.getAdPins(any()) } throws RuntimeException("네트워크 오류")

        viewModel.fetchNearbyPins(37.5665, 126.9780)
        Thread.sleep(2000)

        assertTrue(viewModel.markers.isEmpty())
    }

    // ─────────────────────────────────────────────
    // API 파라미터 검증
    // ─────────────────────────────────────────────

    @Test
    fun fetchNearbyPins_호출시_Bearer_토큰이_포함된다() {
        setupDefaultMocks()

        viewModel.fetchNearbyPins(37.5665, 126.9780)
        Thread.sleep(2000)

        coVerify {
            RetrofitClient.instance.getNearbyPins(
                token = "Bearer test_access_token",
                latitude = any(), longitude = any(),
                radiusMeter = any(), categoryIds = any()
            )
        }
    }

    @Test
    fun fetchNearbyPins_호출시_좌표가_정확하게_전달된다() {
        setupDefaultMocks()

        viewModel.fetchNearbyPins(37.5665, 126.9780)
        Thread.sleep(2000)

        coVerify {
            RetrofitClient.instance.getNearbyPins(
                token = any(),
                latitude = 37.5665,
                longitude = 126.9780,
                radiusMeter = any(), categoryIds = any()
            )
        }
    }
}
