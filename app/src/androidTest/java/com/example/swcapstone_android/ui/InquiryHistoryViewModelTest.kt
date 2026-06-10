package com.example.swcapstone_android.ui.inquiryhistory

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.InquiryItem
import com.example.swcapstone_android.data.model.InquiryPageData
import com.example.swcapstone_android.data.model.InquiryResponse
import com.example.swcapstone_android.data.remote.ApiService
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
class InquiryHistoryViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var application: Application
    private lateinit var mockTokenManager: TokenManager
    private lateinit var mockApiService: ApiService

    // ─────────────────────────────────────────────
    // 더미 데이터
    // ─────────────────────────────────────────────

    private val dummyInquiryItems = listOf(
        InquiryItem(
            id = 1L, type = "ACCOUNT", typeDescription = "계정",
            title = "문의 1", content = "내용 1", answer = null,
            status = "PENDING", statusDescription = "답변 대기",
            createdAt = "2024-01-01T00:00:00.000Z", answeredAt = null
        ),
        InquiryItem(
            id = 2L, type = "BUG", typeDescription = "버그",
            title = "문의 2", content = "내용 2", answer = "답변입니다",
            status = "ANSWERED", statusDescription = "답변 완료",
            createdAt = "2024-01-02T00:00:00.000Z", answeredAt = "2024-01-03T00:00:00.000Z"
        ),
        InquiryItem(
            id = 3L, type = "BUG", typeDescription = "버그",
            title = "문의 3", content = "내용 3", answer = null,
            status = "PENDING", statusDescription = "답변 대기",
            createdAt = "2024-01-04T00:00:00.000Z", answeredAt = null
        )
    )

    // ─────────────────────────────────────────────
    // Response 헬퍼
    // ─────────────────────────────────────────────

    private fun successResponse(items: List<InquiryItem> = dummyInquiryItems): Response<InquiryResponse> {
        val mockContent = mockk<InquiryPageData>()
        every { mockContent.content } returns items

        val mockBody = mockk<InquiryResponse>()
        every { mockBody.status } returns "SUCCESS"
        every { mockBody.code } returns "200"
        every { mockBody.message } returns "OK"
        every { mockBody.data } returns mockContent

        return Response.success(mockBody)
    }

    private fun failureResponse(code: Int = 500): Response<InquiryResponse> {
        return Response.error(code, "error".toResponseBody())
    }

    // ─────────────────────────────────────────────
    // setUp / tearDown
    // ─────────────────────────────────────────────

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()

        mockTokenManager = mockk()
        mockApiService = mockk()

        every { mockTokenManager.accessToken } returns flowOf("mock_token")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // init에서 fetchMyInquiries가 호출되므로 항상 Mock 설정 후 생성
    private fun createViewModel(isNoticeTab: Boolean = false) = InquiryHistoryViewModel(
        application = application,
        tokenManager = mockTokenManager,
        apiService = mockApiService
    )

    // ─────────────────────────────────────────────
    // init 블록 검증
    // ─────────────────────────────────────────────

    @Test
    fun 생성시_자동으로_fetchMyInquiries가_호출된다() {
        coEvery { mockApiService.getMyInquiries(any()) } returns successResponse()

        createViewModel()

        coVerify(exactly = 1) { mockApiService.getMyInquiries(any()) }
    }

    @Test
    fun 생성시_정상_응답이면_inquiryList가_채워진다() {
        coEvery { mockApiService.getMyInquiries(any()) } returns successResponse()

        val vm = createViewModel()

        assertEquals(3, vm.inquiryList.size)
        assertEquals("문의 1", vm.inquiryList[0].title)
    }

    // ─────────────────────────────────────────────
    // 정상 흐름 검증
    // ─────────────────────────────────────────────

    @Test
    fun fetchMyInquiries_성공시_inquiryList가_업데이트된다() {
        coEvery { mockApiService.getMyInquiries(any()) } returns successResponse()

        val vm = createViewModel()

        assertEquals(dummyInquiryItems.size, vm.inquiryList.size)
    }

    @Test
    fun fetchMyInquiries_성공시_isLoading이_false로_복원된다() {
        coEvery { mockApiService.getMyInquiries(any()) } returns successResponse()

        val vm = createViewModel()

        assertFalse(vm.isLoading)
    }

    @Test
    fun fetchMyInquiries_성공시_errorMessage가_null이다() {
        coEvery { mockApiService.getMyInquiries(any()) } returns successResponse()

        val vm = createViewModel()

        assertNull(vm.errorMessage)
    }

    @Test
    fun fetchMyInquiries_Bearer_토큰으로_API가_호출된다() {
        coEvery { mockApiService.getMyInquiries(any()) } returns successResponse()

        createViewModel()

        coVerify { mockApiService.getMyInquiries("Bearer mock_token") }
    }

    @Test
    fun fetchMyInquiries_재호출시_기존_리스트가_교체된다() {
        val firstItems = listOf(
            InquiryItem(
                id = 1L, type = "ACCOUNT", typeDescription = "계정",
                title = "첫번째", content = "내용", answer = null,
                status = "PENDING", statusDescription = "답변 대기",
                createdAt = "2024-01-01T00:00:00.000Z", answeredAt = null
            )
        )
        val secondItems = listOf(
            InquiryItem(
                id = 2L, type = "BUG", typeDescription = "버그",
                title = "두번째", content = "내용", answer = null,
                status = "PENDING", statusDescription = "답변 대기",
                createdAt = "2024-01-02T00:00:00.000Z", answeredAt = null
            ),
            InquiryItem(
                id = 3L, type = "BUG", typeDescription = "버그",
                title = "세번째", content = "내용", answer = null,
                status = "PENDING", statusDescription = "답변 대기",
                createdAt = "2024-01-03T00:00:00.000Z", answeredAt = null
            )
        )

        coEvery { mockApiService.getMyInquiries(any()) } returns successResponse(firstItems)
        val vm = createViewModel()
        assertEquals(1, vm.inquiryList.size)

        coEvery { mockApiService.getMyInquiries(any()) } returns successResponse(secondItems)
        vm.refresh(isNoticeTab = false)
        assertEquals(2, vm.inquiryList.size)
        assertEquals("두번째", vm.inquiryList[0].title)
    }

    // ─────────────────────────────────────────────
    // 실패 흐름 검증
    // ─────────────────────────────────────────────

    @Test
    fun fetchMyInquiries_API_실패시_errorMessage가_설정된다() {
        coEvery { mockApiService.getMyInquiries(any()) } returns failureResponse(500)

        val vm = createViewModel()

        assertNotNull(vm.errorMessage)
        assertTrue(vm.errorMessage!!.contains("500"))
    }

    @Test
    fun fetchMyInquiries_API_실패시_isLoading이_false로_복원된다() {
        coEvery { mockApiService.getMyInquiries(any()) } returns failureResponse()

        val vm = createViewModel()

        assertFalse(vm.isLoading)
    }

    @Test
    fun fetchMyInquiries_status가_SUCCESS가_아니면_errorMessage가_설정된다() {
        val mockBody = mockk<InquiryResponse>()
        every { mockBody.status } returns "FAIL"
        every { mockBody.code } returns "ERROR_001"
        every { mockBody.message } returns "서버 오류입니다."
        every { mockBody.data } returns mockk()

        coEvery { mockApiService.getMyInquiries(any()) } returns Response.success(mockBody)

        val vm = createViewModel()

        assertEquals("서버 오류입니다.", vm.errorMessage)
    }

    @Test
    fun fetchMyInquiries_예외_발생시_errorMessage가_설정된다() {
        coEvery {
            mockApiService.getMyInquiries(any())
        } throws RuntimeException("네트워크 연결 실패")

        val vm = createViewModel()

        assertNotNull(vm.errorMessage)
        assertTrue(vm.errorMessage!!.contains("네트워크 오류"))
    }

    @Test
    fun fetchMyInquiries_예외_발생시_isLoading이_false로_복원된다() {
        coEvery {
            mockApiService.getMyInquiries(any())
        } throws RuntimeException("타임아웃")

        val vm = createViewModel()

        assertFalse(vm.isLoading)
    }

    // ─────────────────────────────────────────────
    // 토큰 없음 검증
    // ─────────────────────────────────────────────

    @Test
    fun 토큰이_null이면_API가_호출되지_않는다() {
        every { mockTokenManager.accessToken } returns flowOf(null)

        createViewModel()

        coVerify(exactly = 0) { mockApiService.getMyInquiries(any()) }
    }

    @Test
    fun 토큰이_null이면_세션만료_errorMessage가_설정된다() {
        every { mockTokenManager.accessToken } returns flowOf(null)

        val vm = createViewModel()

        assertEquals("로그인 세션이 만료되었습니다. 다시 로그인해 주세요.", vm.errorMessage)
    }

    // ─────────────────────────────────────────────
    // refresh 검증
    // ─────────────────────────────────────────────

    @Test
    fun refresh_호출시_fetchMyInquiries가_추가로_호출된다() {
        coEvery { mockApiService.getMyInquiries(any()) } returns successResponse()

        val vm = createViewModel()
        vm.refresh(isNoticeTab = false)

        // init 1번 + refresh 1번 = 총 2번
        coVerify(exactly = 2) { mockApiService.getMyInquiries(any()) }
    }
}