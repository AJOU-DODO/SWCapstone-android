package com.example.swcapstone_android.data.remote

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swcapstone_android.data.TokenManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TokenAuthenticatorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var tokenManager: TokenManager
    private lateinit var authenticator: TokenAuthenticator
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        tokenManager = TokenManager(context)

        mockWebServer = MockWebServer()
        mockWebServer.start()

        val testBaseUrl = mockWebServer.url("/").toString()
        authenticator = TokenAuthenticator(context, tokenManager, baseUrl = testBaseUrl)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        runTest { tokenManager.clearTokens() }
    }

    @Test
    fun 토큰_재발급_성공_시_새로운_토큰을_DataStore에_저장하고_헤더에_새_토큰을_끼워_재요청을_반환한다() = runTest {
        tokenManager.saveTokens("old_access", "valid_refresh")

        val mockSuccessResponseBody = """
            {
                "success": true,
                "data": {
                    "accessToken": "new_activated_access_token",
                    "refreshToken": "new_activated_refresh_token"
                }
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockSuccessResponseBody)
        )

        val dummyRequest = Request.Builder()
            .url(mockWebServer.url("/dummy-api")) // 가짜 서버 주소 적용
            .header("Authorization", "Bearer old_access")
            .build()

        val dummyResponse = Response.Builder()
            .request(dummyRequest)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        val authenticatedRequest = authenticator.authenticate(null, dummyResponse)

        assertNotNull(authenticatedRequest)

        val authHeader = authenticatedRequest?.header("Authorization")
        assertEquals("Bearer new_activated_access_token", authHeader)

        assertEquals("new_activated_access_token", tokenManager.accessToken.first())
        assertEquals("new_activated_refresh_token", tokenManager.refreshToken.first())
    }

    @Test
    fun RefreshToken이_만료되어_재발급_API가_실패하면_null을_반환하여_무한루프를_차단한다() = runTest {
        tokenManager.saveTokens("old_access", "expired_refresh")

        // 백엔드가 토큰 재발급 거부(400 Bad Request or 401) 응답을 준다고 가정
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(400)
                .setBody("""{"success":false, "message":"만료된 토큰입니다"}""")
        )

        val dummyRequest = Request.Builder()
            .url(mockWebServer.url("/dummy-api"))
            .header("Authorization", "Bearer old_access")
            .build()

        val dummyResponse = Response.Builder()
            .request(dummyRequest)
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        // When: Authenticator 발동
        val authenticatedRequest = authenticator.authenticate(null, dummyResponse)

        // Then: 재발급에 실패했으므로 아무런 재요청을 하지 않고 통신을 중단(null 반환)해야 함
        assertNull(authenticatedRequest)
    }
}