package com.example.swcapstone_android.ui

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swcapstone_android.data.TokenManager
import com.example.swcapstone_android.data.model.PresignedData
import com.example.swcapstone_android.data.model.PresignedResponse
import com.example.swcapstone_android.data.remote.RetrofitClient
import com.example.swcapstone_android.ui.userdetail.UserDetailViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.*
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class UserDetailViewModelTest {

    private lateinit var application: Application
    private lateinit var spyApplication: Application      // ✅ 추가
    private lateinit var mockContentResolver: ContentResolver  // ✅ 추가
    private lateinit var tokenManager: TokenManager
    private lateinit var viewModel: UserDetailViewModel
    private lateinit var mockUri: Uri                     // ✅ 공통 Uri

    private val dummyPresignedData = PresignedData(
        presignedUrl = "https://s3.amazonaws.com/test-presigned-url",
        fileUrl = "https://s3.amazonaws.com/test-file-url.jpg"
    )

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

        // ✅ ContentResolver Mock 설정
        mockContentResolver = mockk()
        spyApplication = spyk(application)
        every { spyApplication.contentResolver } returns mockContentResolver

        // ✅ 공통 Uri Mock — openInputStream은 각 테스트에서 필요시 설정
        mockUri = mockk()

        // RetrofitClient Mock
        mockkObject(RetrofitClient)

        // FirebaseMessaging Mock
        mockkStatic(FirebaseMessaging::class)
        val mockFirebase = mockk<FirebaseMessaging>()
        every { FirebaseMessaging.getInstance() } returns mockFirebase
        every { mockFirebase.token } returns Tasks.forResult("mock_fcm_token")

        // ✅ spyApplication으로 ViewModel 생성
        viewModel = UserDetailViewModel(spyApplication)
    }

    @After
    fun tearDown() {
        runBlocking { tokenManager.clearTokens() }
        unmockkAll()
    }

    // ─────────────────────────────────────────────
    // 초기 상태 검증 (ContentResolver 불필요)
    // ─────────────────────────────────────────────

    @Test
    fun 초기상태에서_nickname은_빈문자열이다() {
        assertEquals("", viewModel.nickname)
    }

    @Test
    fun 초기상태에서_selectedImageUri는_null이다() {
        assertNull(viewModel.selectedImageUri)
    }

    @Test
    fun 초기상태에서_showDialog는_false이다() {
        assertFalse(viewModel.showDialog)
    }

    @Test
    fun 초기상태에서_isUploading은_false이다() {
        assertFalse(viewModel.isUploading)
    }

    // ─────────────────────────────────────────────
    // 유효성 검사 (ContentResolver 불필요)
    // ─────────────────────────────────────────────

    @Test
    fun nickname이_비어있으면_showDialog가_true가_된다() {
        viewModel.nickname = ""
        viewModel.selectedImageUri = mockUri
        viewModel.onStartClick {}
        assertTrue(viewModel.showDialog)
    }

    @Test
    fun selectedImageUri가_null이면_showDialog가_true가_된다() {
        viewModel.nickname = "테스트닉네임"
        viewModel.selectedImageUri = null
        viewModel.onStartClick {}
        assertTrue(viewModel.showDialog)
    }

    @Test
    fun nickname과_uri_모두_없으면_showDialog가_true가_된다() {
        viewModel.nickname = ""
        viewModel.selectedImageUri = null
        viewModel.onStartClick {}
        assertTrue(viewModel.showDialog)
    }

    @Test
    fun nickname이_공백만_있으면_showDialog가_true가_된다() {
        viewModel.nickname = "   "
        viewModel.selectedImageUri = mockUri
        viewModel.onStartClick {}
        assertTrue(viewModel.showDialog)
    }

    @Test
    fun nickname과_uri가_모두_있으면_showDialog는_false이다() {
        viewModel.nickname = "테스트닉네임"
        viewModel.selectedImageUri = mockUri

        // ✅ ContentResolver Mock
        every { mockContentResolver.openInputStream(mockUri) } returns "bytes".byteInputStream()
        coEvery { RetrofitClient.instance.getPresignedUrl(any(), any()) } returns
                Response.error(500, "error".toResponseBody())

        viewModel.onStartClick {}
        assertFalse(viewModel.showDialog)
    }

    // ─────────────────────────────────────────────
    // saveProfileProcess — 성공
    // ─────────────────────────────────────────────

    @Test
    fun 프로필_저장_성공시_onSuccess_콜백이_호출된다() {
        viewModel.nickname = "테스트닉네임"
        viewModel.selectedImageUri = mockUri

        // ✅ ContentResolver Mock
        every { mockContentResolver.openInputStream(mockUri) } returns "fake_image".byteInputStream()

        coEvery { RetrofitClient.instance.getPresignedUrl(any(), any()) } returns
                Response.success(PresignedResponse(status = "SUCCESS", data = dummyPresignedData))
        coEvery { RetrofitClient.s3Instance.uploadImage(any(), any(), any()) } returns
                Response.success(null)
        coEvery { RetrofitClient.instance.updateProfile(any(), any()) } returns
                Response.success(null)

        var callbackCalled = false
        viewModel.onStartClick { callbackCalled = true }

        waitUntil { callbackCalled }
        assertTrue("onSuccess 콜백이 호출되지 않았습니다.", callbackCalled)
    }

    @Test
    fun 프로필_저장_성공후_isUploading이_false가_된다() {
        viewModel.nickname = "테스트닉네임"
        viewModel.selectedImageUri = mockUri

        every { mockContentResolver.openInputStream(mockUri) } returns "bytes".byteInputStream()
        coEvery { RetrofitClient.instance.getPresignedUrl(any(), any()) } returns
                Response.error(500, "error".toResponseBody())

        viewModel.onStartClick {}

        waitUntil { !viewModel.isUploading }
        assertFalse(viewModel.isUploading)
    }

    // ─────────────────────────────────────────────
    // saveProfileProcess — 실패
    // ─────────────────────────────────────────────

    @Test
    fun presignedUrl_요청_실패시_onSuccess_콜백이_호출되지_않는다() {
        viewModel.nickname = "테스트닉네임"
        viewModel.selectedImageUri = mockUri

        every { mockContentResolver.openInputStream(mockUri) } returns "bytes".byteInputStream()
        coEvery { RetrofitClient.instance.getPresignedUrl(any(), any()) } returns
                Response.error(500, "서버 에러".toResponseBody())

        var callbackCalled = false
        viewModel.onStartClick { callbackCalled = true }

        Thread.sleep(2000)
        assertFalse("실패 시 onSuccess가 호출되었습니다.", callbackCalled)
    }

    @Test
    fun s3_업로드_실패시_onSuccess_콜백이_호출되지_않는다() {
        viewModel.nickname = "테스트닉네임"
        viewModel.selectedImageUri = mockUri

        every { mockContentResolver.openInputStream(mockUri) } returns "bytes".byteInputStream()
        coEvery { RetrofitClient.instance.getPresignedUrl(any(), any()) } returns
                Response.success(PresignedResponse(status = "SUCCESS", data = dummyPresignedData))
        coEvery { RetrofitClient.s3Instance.uploadImage(any(), any(), any()) } returns
                Response.error(500, "s3 에러".toResponseBody())

        var callbackCalled = false
        viewModel.onStartClick { callbackCalled = true }

        Thread.sleep(2000)
        assertFalse("S3 실패 시 onSuccess가 호출되었습니다.", callbackCalled)
    }

    @Test
    fun updateProfile_실패시_onSuccess_콜백이_호출되지_않는다() {
        viewModel.nickname = "테스트닉네임"
        viewModel.selectedImageUri = mockUri

        every { mockContentResolver.openInputStream(mockUri) } returns "bytes".byteInputStream()
        coEvery { RetrofitClient.instance.getPresignedUrl(any(), any()) } returns
                Response.success(PresignedResponse(status = "SUCCESS", data = dummyPresignedData))
        coEvery { RetrofitClient.s3Instance.uploadImage(any(), any(), any()) } returns
                Response.success(null)
        coEvery { RetrofitClient.instance.updateProfile(any(), any()) } returns
                Response.error(500, "프로필 에러".toResponseBody())

        var callbackCalled = false
        viewModel.onStartClick { callbackCalled = true }

        Thread.sleep(2000)
        assertFalse("updateProfile 실패 시 onSuccess가 호출되었습니다.", callbackCalled)
    }

    @Test
    fun 네트워크_예외_발생시_onSuccess_콜백이_호출되지_않는다() {
        viewModel.nickname = "테스트닉네임"
        viewModel.selectedImageUri = mockUri

        every { mockContentResolver.openInputStream(mockUri) } returns "bytes".byteInputStream()
        coEvery { RetrofitClient.instance.getPresignedUrl(any(), any()) } throws
                RuntimeException("네트워크 오류")

        var callbackCalled = false
        viewModel.onStartClick { callbackCalled = true }

        Thread.sleep(2000)
        assertFalse("예외 발생 시 onSuccess가 호출되었습니다.", callbackCalled)
    }

    // ─────────────────────────────────────────────
    // API 호출 파라미터 검증
    // ─────────────────────────────────────────────

    @Test
    fun updateProfile_호출시_nickname이_정확하게_전달된다() {
        viewModel.nickname = "정확한닉네임"
        viewModel.selectedImageUri = mockUri

        every { mockContentResolver.openInputStream(mockUri) } returns "bytes".byteInputStream()
        coEvery { RetrofitClient.instance.getPresignedUrl(any(), any()) } returns
                Response.success(PresignedResponse(status = "SUCCESS", data = dummyPresignedData))
        coEvery { RetrofitClient.s3Instance.uploadImage(any(), any(), any()) } returns
                Response.success(null)
        coEvery { RetrofitClient.instance.updateProfile(any(), any()) } returns
                Response.success(null)

        viewModel.onStartClick {}
        Thread.sleep(2000)

        coVerify {
            RetrofitClient.instance.updateProfile(
                any(),
                match { it.nickname == "정확한닉네임" }
            )
        }
    }

    @Test
    fun updateProfile_호출시_Bearer_토큰이_포함된다() {
        viewModel.nickname = "테스트닉네임"
        viewModel.selectedImageUri = mockUri

        every { mockContentResolver.openInputStream(mockUri) } returns "bytes".byteInputStream()
        coEvery { RetrofitClient.instance.getPresignedUrl(any(), any()) } returns
                Response.success(PresignedResponse(status = "SUCCESS", data = dummyPresignedData))
        coEvery { RetrofitClient.s3Instance.uploadImage(any(), any(), any()) } returns
                Response.success(null)
        coEvery { RetrofitClient.instance.updateProfile(any(), any()) } returns
                Response.success(null)

        viewModel.onStartClick {}
        Thread.sleep(2000)

        coVerify {
            RetrofitClient.instance.updateProfile(
                token = "Bearer test_access_token",
                profile = any()
            )
        }
    }

    @Test
    fun updateProfile_호출시_FCM_토큰이_포함된다() {
        viewModel.nickname = "테스트닉네임"
        viewModel.selectedImageUri = mockUri

        every { mockContentResolver.openInputStream(mockUri) } returns "bytes".byteInputStream()
        coEvery { RetrofitClient.instance.getPresignedUrl(any(), any()) } returns
                Response.success(PresignedResponse(status = "SUCCESS", data = dummyPresignedData))
        coEvery { RetrofitClient.s3Instance.uploadImage(any(), any(), any()) } returns
                Response.success(null)
        coEvery { RetrofitClient.instance.updateProfile(any(), any()) } returns
                Response.success(null)

        viewModel.onStartClick {}
        Thread.sleep(2000)

        coVerify {
            RetrofitClient.instance.updateProfile(
                any(),
                match { it.fcmToken == "mock_fcm_token" }
            )
        }
    }
}
