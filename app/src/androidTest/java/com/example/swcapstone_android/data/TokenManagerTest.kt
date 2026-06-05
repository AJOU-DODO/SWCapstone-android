package com.example.swcapstone_android.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TokenManagerTest {

    private lateinit var context: Context
    private lateinit var tokenManager: TokenManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        tokenManager = TokenManager(context)
    }

    @After
    fun tearDown() = runTest {
        tokenManager.clearTokens()
    }

    @Test
    fun 토큰이_없는_초기_상태에서_가져오기를_수행하면_모두_null을_반환한다() = runTest {
        val access = tokenManager.accessToken.first()
        val refresh = tokenManager.refreshToken.first()

        // Then: 둘 다 null이어야 함
        assertNull(access)
        assertNull(refresh)
    }

    @Test
    fun saveTokens를_통해_토큰을_저장하면_저장된_값이_정확하게_조회된다() = runTest {
        val dummyAccess = "sample_access_token_123"
        val dummyRefresh = "sample_refresh_token_456"

        tokenManager.saveTokens(dummyAccess, dummyRefresh)

        val savedAccess = tokenManager.accessToken.first()
        val savedRefresh = tokenManager.refreshToken.first()

        assertEquals(dummyAccess, savedAccess)
        assertEquals(dummyRefresh, savedRefresh)
    }

    @Test
    fun clearTokens를_호출하면_기존에_저장되어_있던_모든_토큰이_삭제되어_null이_된다() = runTest {
        tokenManager.saveTokens("temp_access", "temp_refresh")

        assertEquals("temp_access", tokenManager.accessToken.first())

        tokenManager.clearTokens()

        val clearedAccess = tokenManager.accessToken.first()
        val clearedRefresh = tokenManager.refreshToken.first()

        assertNull(clearedAccess)
        assertNull(clearedRefresh)
    }
}