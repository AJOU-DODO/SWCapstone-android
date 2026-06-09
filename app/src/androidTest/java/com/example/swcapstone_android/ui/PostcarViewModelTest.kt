package com.example.swcapstone_android.ui.postcard

import android.app.Application
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.PostcardRequest
import com.example.swcapstone_android.data.model.PresignedData
import com.example.swcapstone_android.data.model.PresignedResponse
import com.example.swcapstone_android.data.remote.RetrofitClient
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
class PostcardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var application: Application
    private lateinit var mockTokenManager: TokenManager

    // ✅ uploadToS3를 제어하기 위한 TestDouble
    private lateinit var viewModel: TestPostcardViewModel

    // ─────────────────────────────────────────────
    // uploadToS3를 Mock으로 대체한 TestDouble
    // ─────────────────────────────────────────────
    inner class TestPostcardViewModel(
        application: Application,
        tokenManager: TokenManager,
        private val s3Result: Boolean = true
    ) : PostcardViewModel(application, tokenManager) {
        var uploadToS3Called = false
        var uploadedUrl: String? = null

        override suspend fun uploadToS3(url: String, uri: Uri): Boolean {
            uploadToS3Called = true
            uploadedUrl = url
            return s3Result
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        application = ApplicationProvider.getApplicationContext()

        mockTokenManager = mockk()
        every { mockTokenManager.accessToken } returns flowOf("mock_token")

        mockkObject(RetrofitClient)

        viewModel = TestPostcardViewModel(application, mockTokenManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // ─────────────────────────────────────────────
    // 상태 초기값 검증
    // ─────────────────────────────────────────────

    @Test
    fun 초기_selectedImageUri는_null이다() {
        assertNull(viewModel.selectedImageUri)
    }

    @Test
    fun 초기_message는_빈_문자열이다() {
        assertEquals("", viewModel.message)
    }

    @Test
    fun 초기_isUploading은_false이다() {
        assertFalse(viewModel.isUploading)
    }

    // ─────────────────────────────────────────────
    // updateImage / updateMessage 검증
    // ─────────────────────────────────────────────

    @Test
    fun updateImage_호출시_selectedImageUri가_업데이트된다() {
        val uri = mockk<Uri>()
        viewModel.updateImage(uri)
        assertEquals(uri, viewModel.selectedImageUri)
    }

    @Test
    fun updateImage에_null_전달시_selectedImageUri가_null이_된다() {
        val uri = mockk<Uri>()
        viewModel.updateImage(uri)
        viewModel.updateImage(null)
        assertNull(viewModel.selectedImageUri)
    }

    @Test
    fun updateMessage_호출시_message가_업데이트된다() {
        viewModel.updateMessage("안녕하세요!")
        assertEquals("안녕하세요!", viewModel.message)
    }

    // ─────────────────────────────────────────────
    // sendPostcard - 이미지 미선택 시
    // ─────────────────────────────────────────────

    @Test
    fun selectedImageUri가_null이면_sendPostcard가_즉시_반환된다() = runTest(testDispatcher) {
        var callbackCalled = false
        viewModel.sendPostcard { callbackCalled = true }

        assertFalse(callbackCalled)
        assertFalse(viewModel.isUploading)
        coVerify(exactly = 0) { RetrofitClient.instance.getPresignedUrl(any(), any()) }
    }

    // ─────────────────────────────────────────────
    // sendPostcard - 정상 흐름
    // ─────────────────────────────────────────────

    @Test
    fun 정상_흐름에서_onSuccess_콜백이_호출된다() = runTest(testDispatcher) {
        setupSuccessfulFlow()

        var callbackCalled = false
        viewModel.updateImage(mockk<Uri>())
        viewModel.sendPostcard { callbackCalled = true }

        assertTrue(callbackCalled)
    }

    @Test
    fun 정상_흐름에서_isUploading이_false로_복원된다() = runTest(testDispatcher) {
        setupSuccessfulFlow()

        viewModel.updateImage(mockk<Uri>())
        viewModel.sendPostcard { }

        assertFalse(viewModel.isUploading)
    }

    @Test
    fun 정상_흐름에서_getPresignedUrl이_Bearer_토큰으로_호출된다() = runTest(testDispatcher) {
        setupSuccessfulFlow()

        viewModel.updateImage(mockk<Uri>())
        viewModel.sendPostcard { }

        coVerify {
            RetrofitClient.instance.getPresignedUrl(
                token = "Bearer mock_token",
                fileName = any()
            )
        }
    }

    @Test
    fun 정상_흐름에서_createPostcard가_올바른_content로_호출된다() = runTest(testDispatcher) {
        setupSuccessfulFlow()

        viewModel.updateImage(mockk<Uri>())
        viewModel.updateMessage("테스트 메시지")
        viewModel.sendPostcard { }

        coVerify {
            RetrofitClient.instance.createPostcard(
                authHeader = "Bearer mock_token",  // token → authHeader
                request = match { it.content == "테스트 메시지" }
            )
        }
    }

    @Test
    fun 정상_흐름에서_uploadToS3가_호출된다() = runTest(testDispatcher) {
        setupSuccessfulFlow()

        viewModel.updateImage(mockk<Uri>())
        viewModel.sendPostcard { }

        assertTrue(viewModel.uploadToS3Called)
    }

    // ─────────────────────────────────────────────
    // sendPostcard - 실패 흐름
    // ─────────────────────────────────────────────

    @Test
    fun presignedUrl_요청_실패시_onSuccess가_호출되지_않는다() = runTest(testDispatcher) {
        coEvery {
            RetrofitClient.instance.getPresignedUrl(any(), any())
        } returns Response.error(500, "error".toResponseBody())

        var callbackCalled = false
        viewModel.updateImage(mockk<Uri>())
        viewModel.sendPostcard { callbackCalled = true }

        assertFalse(callbackCalled)
        assertFalse(viewModel.isUploading)
    }

    @Test
    fun createPostcard_실패시_onSuccess가_호출되지_않는다() = runTest(testDispatcher) {
        setupPresignedUrlSuccess()
        coEvery {
            RetrofitClient.instance.createPostcard(any(), any())
        } returns Response.error(500, "error".toResponseBody())

        var callbackCalled = false
        viewModel.updateImage(mockk<Uri>())
        viewModel.sendPostcard { callbackCalled = true }

        assertFalse(callbackCalled)
        assertFalse(viewModel.isUploading)
    }

    @Test
    fun 예외_발생시_isUploading이_false로_복원된다() = runTest(testDispatcher) {
        coEvery {
            RetrofitClient.instance.getPresignedUrl(any(), any())
        } throws RuntimeException("네트워크 오류")

        viewModel.updateImage(mockk<Uri>())
        viewModel.sendPostcard { }

        assertFalse(viewModel.isUploading)
    }

    // ─────────────────────────────────────────────
    // 헬퍼 함수
    // ─────────────────────────────────────────────

    private fun setupPresignedUrlSuccess() {
        val mockPresignedData = mockk<PresignedData>()
        every { mockPresignedData.presignedUrl } returns "https://s3.example.com/presigned"
        every { mockPresignedData.fileUrl } returns "https://s3.example.com/final.jpg"

        val mockBody = mockk<PresignedResponse>()
        every { mockBody.data } returns mockPresignedData

        coEvery {
            RetrofitClient.instance.getPresignedUrl(any(), any())
        } returns Response.success(mockBody)
    }

    private fun setupSuccessfulFlow() {
        setupPresignedUrlSuccess()
        coEvery {
            RetrofitClient.instance.createPostcard(any(), any())
        } returns Response.success(null)
    }
}