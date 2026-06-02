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
        // 1. 계측 테스트용 Context를 가져와서 TokenManager 주입
        context = ApplicationProvider.getApplicationContext()
        tokenManager = TokenManager(context)
    }

    @After
    fun tearDown() = runTest {
        // 2. 다른 테스트에 영향을 주지 않도록 매 테스트가 끝나면 저장소 데이터를 깨끗하게 청소
        tokenManager.clearTokens()
    }

    @Test
    fun 토큰이_없는_초기_상태에서_가져오기를_수행하면_모두_null을_반환한다() = runTest {
        // When: 저장된 토큰이 없을 때 Flow의 첫 번째 값을 수집
        val access = tokenManager.accessToken.first()
        val refresh = tokenManager.refreshToken.first()

        // Then: 둘 다 null이어야 함
        assertNull(access)
        assertNull(refresh)
    }

    @Test
    fun saveTokens를_통해_토큰을_저장하면_저장된_값이_정확하게_조회된다() = runTest {
        // Given: 테스트용 더미 토큰 데이터 준비
        val dummyAccess = "sample_access_token_123"
        val dummyRefresh = "sample_refresh_token_456"

        // When: 토큰 저장 실행
        tokenManager.saveTokens(dummyAccess, dummyRefresh)

        // Then: 저장된 데이터와 Flow에서 꺼내온 데이터가 일치하는지 단언(Assertion)
        val savedAccess = tokenManager.accessToken.first()
        val savedRefresh = tokenManager.refreshToken.first()

        assertEquals(dummyAccess, savedAccess)
        assertEquals(dummyRefresh, savedRefresh)
    }

    @Test
    fun clearTokens를_호출하면_기존에_저장되어_있던_모든_토큰이_삭제되어_null이_된다() = runTest {
        // Given: 먼저 토큰을 안전하게 저장해 둠
        tokenManager.saveTokens("temp_access", "temp_refresh")

        // 토큰이 잘 들어갔는지 중간 확인
        assertEquals("temp_access", tokenManager.accessToken.first())

        // When: 토큰 삭제 메서드 실행
        tokenManager.clearTokens()

        // Then: 삭제 후 꺼냈을 때 무조건 null로 밀려있어야 함
        val clearedAccess = tokenManager.accessToken.first()
        val clearedRefresh = tokenManager.refreshToken.first()

        assertNull(clearedAccess)
        assertNull(clearedRefresh)
    }
}