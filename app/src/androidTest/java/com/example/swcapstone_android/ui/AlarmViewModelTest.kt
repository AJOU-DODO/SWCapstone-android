package com.example.swcapstone_android.ui

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.etc.UrlProvider
import com.example.swcapstone_android.ui.alarm.AlarmViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AlarmViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var application: Application
    private lateinit var mockTokenManager: TokenManager  // ✅ 직접 Mock 객체 생성
    private lateinit var viewModel: AlarmViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        application = ApplicationProvider.getApplicationContext()

        // mockkConstructor 대신 mockk로 직접 Mock 생성
        mockTokenManager = mockk<TokenManager>()
        every { mockTokenManager.accessToken } returns flowOf("mock_access_token")

        // Mock을 생성자로 주입
        viewModel = AlarmViewModel(application, mockTokenManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun initData_호출시_accessToken이_캐시에_저장된다() = runTest(testDispatcher) {
        viewModel.initData(nestId = "nest_123", type = null)
        assertEquals("mock_access_token", viewModel.accessTokenCache)
    }

    @Test
    fun accessToken이_null이면_빈_문자열로_저장된다() = runTest(testDispatcher) {
        every { mockTokenManager.accessToken } returns flowOf(null)
        // Mock이 바뀌었으므로 새 인스턴스 필요
        val vm = AlarmViewModel(application, mockTokenManager)
        vm.initData(nestId = "nest_123", type = null)
        assertEquals("", vm.accessTokenCache)
    }

    @Test
    fun type이_POSTCARD이면_sent_탭_URL이_설정된다() = runTest(testDispatcher) {
        viewModel.initData(nestId = "nest_123", type = "POSTCARD")
        assertEquals("${UrlProvider.baseUrl}/mypage/posts?tab=sent", viewModel.alarmUrl)
    }

    @Test
    fun type이_null이면_nest_URL이_설정된다() = runTest(testDispatcher) {
        viewModel.initData(nestId = "nest_123", type = null)
        assertEquals("${UrlProvider.baseUrl}/nests/nest_123", viewModel.alarmUrl)
    }

    @Test
    fun type이_알수없는_값이면_nest_URL이_설정된다() = runTest(testDispatcher) {
        viewModel.initData(nestId = "nest_456", type = "UNKNOWN_TYPE")
        assertEquals("${UrlProvider.baseUrl}/nests/nest_456", viewModel.alarmUrl)
    }

    @Test
    fun type이_빈_문자열이면_nest_URL이_설정된다() = runTest(testDispatcher) {
        viewModel.initData(nestId = "nest_789", type = "")
        assertEquals("${UrlProvider.baseUrl}/nests/nest_789", viewModel.alarmUrl)
    }

    @Test
    fun nestId가_URL에_정확히_반영된다() = runTest(testDispatcher) {
        viewModel.initData(nestId = "my-special-nest-99", type = null)
        assertTrue(viewModel.alarmUrl.contains("my-special-nest-99"))
    }

    @Test
    fun POSTCARD_타입은_nestId와_무관하게_동일한_URL이_설정된다() = runTest(testDispatcher) {
        viewModel.initData(nestId = "any_nest", type = "POSTCARD")
        assertFalse(viewModel.alarmUrl.contains("any_nest"))
        assertTrue(viewModel.alarmUrl.endsWith("?tab=sent"))
    }

    @Test
    fun initData_호출_전_alarmUrl은_빈_문자열이다() {
        assertEquals("", viewModel.alarmUrl)
    }

    @Test
    fun initData_호출_전_accessTokenCache는_빈_문자열이다() {
        assertEquals("", viewModel.accessTokenCache)
    }
}