package com.example.swcapstone_android.ui

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    // 🌟 MainActivity를 테스트 시스템에 바인딩
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun 앱_구동_중_NEST_LIKE_알림_인텐트가_유입되면_onNewIntent_라우팅_메커니즘이_정상_트리거된다() {
        // Given: 앱이 켜진 직후 (현재 스플래시나 로딩 중인 상태 상관없이)
        // FCM 푸시 알림을 클릭했을 때의 시스템 가짜 Intent Payload 설계
        val context = ApplicationProvider.getApplicationContext<Context>()
        val newIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("SELECTED_NEST_ID", "888")
            putExtra("NOTIFICATION_TYPE", "NEST_LIKE") // 좋아요 알림 타입 주입
        }

        // When: 액티비티가 살아있는 도중에 새로운 알림 인텐트를 강제로 주입! (onNewIntent 트리거)
        composeTestRule.activityRule.scenario.onActivity { activity ->
            // MainActivity 내부에 열어둔 테스트용 우회 메서드 호출
            activity.triggerNewIntentForTest(newIntent)
        }

        // 💡 [핵심 보정] 알림 화면(AlarmScreen) 내부 혹은 상단바에 '반드시' 뜨는 글자로 세팅해줘.
        // 네가 만든 알림창 디자인에 "알림" 대신 "알림 상세", "소식" 등이 뜨면 그 단어로 교체해야 해!
        val targetScreenText = "알림"

        // Then: LaunchedEffect가 발동하여 알림 화면 데스티네이션 노드가 렌더링될 때까지 최대 5초 대기
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText(targetScreenText)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}