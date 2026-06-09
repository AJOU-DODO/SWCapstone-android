package com.example.swcapstone_android.ui.inquiry

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.InquiryType
import com.example.swcapstone_android.data.remote.ApiService  // 실제 인터페이스명으로 교체
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class InquiryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var application: Application
    private lateinit var mockTokenManager: TokenManager
    private lateinit var mockApiService: ApiService
    private lateinit var viewModel: InquiryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()

        mockTokenManager = mockk()
        mockApiService = mockk()
        every { mockTokenManager.accessToken } returns flowOf("mock_token")

        viewModel = InquiryViewModel(application, mockTokenManager, mockApiService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ─────────────────────────────────────────────
    // 초기 상태 검증
    // ─────────────────────────────────────────────

    @Test
    fun 초기_selectedType은_SUGGESTION이다() {
        assertEquals(InquiryType.SUGGESTION, viewModel.selectedType)
    }

    @Test
    fun 초기_title은_빈_문자열이다() {
        assertEquals("", viewModel.title)
    }

    @Test
    fun 초기_content는_빈_문자열이다() {
        assertEquals("", viewModel.content)
    }

    @Test
    fun 초기_isLoading은_false이다() {
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun 초기_isSuccess는_false이다() {
        assertFalse(viewModel.isSuccess)
    }

    @Test
    fun 초기_errorMessage는_null이다() {
        assertNull(viewModel.errorMessage)
    }

    // ─────────────────────────────────────────────
    // 유효성 검사 검증
    // ─────────────────────────────────────────────

    @Test
    fun title이_비어있으면_errorMessage가_설정된다() = runTest(testDispatcher) {
        viewModel.title = ""
        viewModel.content = "내용"
        viewModel.submitInquiry { }

        assertEquals("제목과 내용을 모두 입력해주세요.", viewModel.errorMessage)
    }

    @Test
    fun content가_비어있으면_errorMessage가_설정된다() = runTest(testDispatcher) {
        viewModel.title = "제목"
        viewModel.content = ""
        viewModel.submitInquiry { }

        assertEquals("제목과 내용을 모두 입력해주세요.", viewModel.errorMessage)
    }

    @Test
    fun title과_content_모두_비어있으면_errorMessage가_설정된다() = runTest(testDispatcher) {
        viewModel.title = ""
        viewModel.content = ""
        viewModel.submitInquiry { }

        assertEquals("제목과 내용을 모두 입력해주세요.", viewModel.errorMessage)
    }

    @Test
    fun title이_공백만_있으면_유효성_검사에_실패한다() = runTest(testDispatcher) {
        viewModel.title = "   "
        viewModel.content = "내용"
        viewModel.submitInquiry { }

        assertEquals("제목과 내용을 모두 입력해주세요.", viewModel.errorMessage)
    }

    @Test
    fun 유효성_검사_실패시_API가_호출되지_않는다() = runTest(testDispatcher) {
        viewModel.title = ""
        viewModel.content = ""
        viewModel.submitInquiry { }

        coVerify(exactly = 0) { mockApiService.createInquiry(any(), any()) }
    }

    // ─────────────────────────────────────────────
    // 정상 흐름 검증
    // ─────────────────────────────────────────────

    @Test
    fun 정상_제출시_onSuccess_콜백이_호출된다() = runTest(testDispatcher) {
        coEvery { mockApiService.createInquiry(any(), any()) } returns Response.success(null)

        var callbackCalled = false
        viewModel.title = "제목"
        viewModel.content = "내용"
        viewModel.submitInquiry { callbackCalled = true }

        assertTrue(callbackCalled)
    }

    @Test
    fun 정상_제출시_isSuccess가_true가_된다() = runTest(testDispatcher) {
        coEvery { mockApiService.createInquiry(any(), any()) } returns Response.success(null)

        viewModel.title = "제목"
        viewModel.content = "내용"
        viewModel.submitInquiry { }

        assertTrue(viewModel.isSuccess)
    }

    @Test
    fun 정상_제출시_isLoading이_false로_복원된다() = runTest(testDispatcher) {
        coEvery { mockApiService.createInquiry(any(), any()) } returns Response.success(null)

        viewModel.title = "제목"
        viewModel.content = "내용"
        viewModel.submitInquiry { }

        assertFalse(viewModel.isLoading)
    }

    @Test
    fun 정상_제출시_Bearer_토큰으로_API가_호출된다() = runTest(testDispatcher) {
        coEvery { mockApiService.createInquiry(any(), any()) } returns Response.success(null)

        viewModel.title = "제목"
        viewModel.content = "내용"
        viewModel.submitInquiry { }

        coVerify { mockApiService.createInquiry("Bearer mock_token", any()) }
    }

    @Test
    fun 정상_제출시_selectedType이_request에_반영된다() = runTest(testDispatcher) {
        coEvery { mockApiService.createInquiry(any(), any()) } returns Response.success(null)

        viewModel.selectedType = InquiryType.BUG
        viewModel.title = "제목"
        viewModel.content = "내용"
        viewModel.submitInquiry { }

        coVerify {
            mockApiService.createInquiry(
                any(),
                match { it.type == "BUG" }
            )
        }
    }

    @Test
    fun 정상_제출시_title과_content가_request에_반영된다() = runTest(testDispatcher) {
        coEvery { mockApiService.createInquiry(any(), any()) } returns Response.success(null)

        viewModel.title = "테스트 제목"
        viewModel.content = "테스트 내용"
        viewModel.submitInquiry { }

        coVerify {
            mockApiService.createInquiry(
                any(),
                match { it.title == "테스트 제목" && it.content == "테스트 내용" }
            )
        }
    }

    // ─────────────────────────────────────────────
    // 실패 흐름 검증
    // ─────────────────────────────────────────────

    @Test
    fun API_실패시_errorMessage에_에러코드가_포함된다() = runTest(testDispatcher) {
        coEvery {
            mockApiService.createInquiry(any(), any())
        } returns Response.error(500, "error".toResponseBody())

        viewModel.title = "제목"
        viewModel.content = "내용"
        viewModel.submitInquiry { }

        assertNotNull(viewModel.errorMessage)
        assertTrue(viewModel.errorMessage!!.contains("500"))
    }

    @Test
    fun API_실패시_isSuccess가_false로_유지된다() = runTest(testDispatcher) {
        coEvery {
            mockApiService.createInquiry(any(), any())
        } returns Response.error(500, "error".toResponseBody())

        viewModel.title = "제목"
        viewModel.content = "내용"
        viewModel.submitInquiry { }

        assertFalse(viewModel.isSuccess)
    }

    @Test
    fun API_실패시_isLoading이_false로_복원된다() = runTest(testDispatcher) {
        coEvery {
            mockApiService.createInquiry(any(), any())
        } returns Response.error(500, "error".toResponseBody())

        viewModel.title = "제목"
        viewModel.content = "내용"
        viewModel.submitInquiry { }

        assertFalse(viewModel.isLoading)
    }

    @Test
    fun 예외_발생시_errorMessage에_네트워크_오류가_포함된다() = runTest(testDispatcher) {
        coEvery {
            mockApiService.createInquiry(any(), any())
        } throws RuntimeException("연결 실패")

        viewModel.title = "제목"
        viewModel.content = "내용"
        viewModel.submitInquiry { }

        assertNotNull(viewModel.errorMessage)
        assertTrue(viewModel.errorMessage!!.contains("네트워크 오류"))
    }

    @Test
    fun 예외_발생시_isLoading이_false로_복원된다() = runTest(testDispatcher) {
        coEvery {
            mockApiService.createInquiry(any(), any())
        } throws RuntimeException("타임아웃")

        viewModel.title = "제목"
        viewModel.content = "내용"
        viewModel.submitInquiry { }

        assertFalse(viewModel.isLoading)
    }

    // ─────────────────────────────────────────────
    // 토큰 없음 검증
    // ─────────────────────────────────────────────

    @Test
    fun 토큰이_null이면_API가_호출되지_않는다() = runTest(testDispatcher) {
        every { mockTokenManager.accessToken } returns flowOf(null)

        viewModel.title = "제목"
        viewModel.content = "내용"
        viewModel.submitInquiry { }

        coVerify(exactly = 0) { mockApiService.createInquiry(any(), any()) }
    }

    @Test
    fun 토큰이_null이면_로그인_오류_errorMessage가_설정된다() = runTest(testDispatcher) {
        every { mockTokenManager.accessToken } returns flowOf(null)

        viewModel.title = "제목"
        viewModel.content = "내용"
        viewModel.submitInquiry { }

        assertEquals("로그인 정보가 유효하지 않습니다.", viewModel.errorMessage)
    }
}