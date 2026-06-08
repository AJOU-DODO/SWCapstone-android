package com.example.swcapstone_android.ui

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swcapstone_android.data.etc.UrlProvider
import com.example.swcapstone_android.ui.write.WriteViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WriteViewModelTest {

    private lateinit var application: Application
    private lateinit var viewModel: WriteViewModel

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        viewModel = WriteViewModel(application)
    }

    // ─────────────────────────────────────────────
    // 초기 상태 검증
    // ─────────────────────────────────────────────

    @Test
    fun 초기_jsCommand는_null이다() {
        assertNull(viewModel.jsCommand)
    }

    @Test
    fun 초기_isLoading은_true이다() {
        assertTrue(viewModel.isLoading)
    }

    @Test
    fun 초기_showPublishConfirm은_false이다() {
        assertFalse(viewModel.showPublishConfirm)
    }

    @Test
    fun 초기_publishRadius는_0이다() {
        assertEquals(0, viewModel.publishRadius)
    }

    @Test
    fun 초기_writeUrl이_nest_editor_URL이다() {
        assertEquals("${UrlProvider.baseUrl}/nest-editor", viewModel.writeUrl)
    }

    // ─────────────────────────────────────────────
    // updateLoading 검증
    // ─────────────────────────────────────────────

    @Test
    fun updateLoading_false_호출시_isLoading이_false가_된다() {
        viewModel.updateLoading(false)
        assertFalse(viewModel.isLoading)
    }

    @Test
    fun updateLoading_true_호출시_isLoading이_true가_된다() {
        viewModel.updateLoading(false)
        viewModel.updateLoading(true)
        assertTrue(viewModel.isLoading)
    }

    // ─────────────────────────────────────────────
    // clearJsCommand 검증
    // ─────────────────────────────────────────────

    @Test
    fun clearJsCommand_호출시_jsCommand가_null이_된다() {
        // jsCommand에 값이 있는 상태 만들기
        viewModel.sendApproveToWeb()
        assertNotNull(viewModel.jsCommand)

        viewModel.clearJsCommand()
        assertNull(viewModel.jsCommand)
    }

    // ─────────────────────────────────────────────
    // requestPublication 검증
    // ─────────────────────────────────────────────

    @Test
    fun requestPublication_호출시_publishRadius가_설정된다() {
        viewModel.requestPublication(3000)
        assertEquals(3000, viewModel.publishRadius)
    }

    @Test
    fun requestPublication_호출시_showPublishConfirm이_true가_된다() {
        viewModel.requestPublication(3000)
        assertTrue(viewModel.showPublishConfirm)
    }

    @Test
    fun requestPublication_radius_0으로_호출해도_showPublishConfirm이_true가_된다() {
        viewModel.requestPublication(0)
        assertTrue(viewModel.showPublishConfirm)
        assertEquals(0, viewModel.publishRadius)
    }

    // ─────────────────────────────────────────────
    // dismissConfirm 검증
    // ─────────────────────────────────────────────

    @Test
    fun dismissConfirm_호출시_showPublishConfirm이_false가_된다() {
        viewModel.requestPublication(3000)
        viewModel.dismissConfirm()
        assertFalse(viewModel.showPublishConfirm)
    }

    @Test
    fun dismissConfirm_호출해도_publishRadius는_유지된다() {
        viewModel.requestPublication(5000)
        viewModel.dismissConfirm()
        assertEquals(5000, viewModel.publishRadius)
    }

    // ─────────────────────────────────────────────
    // sendApproveToWeb 검증
    // ─────────────────────────────────────────────

    @Test
    fun sendApproveToWeb_호출시_jsCommand가_getApprove_스크립트가_된다() {
        viewModel.sendApproveToWeb()
        assertEquals("window.getApprove()", viewModel.jsCommand)
    }

    @Test
    fun sendApproveToWeb_호출시_showPublishConfirm이_false가_된다() {
        viewModel.requestPublication(3000)
        viewModel.sendApproveToWeb()
        assertFalse(viewModel.showPublishConfirm)
    }

    // ─────────────────────────────────────────────
    // 시나리오 테스트 (상태 흐름 검증)
    // ─────────────────────────────────────────────

    @Test
    fun 발행_요청_후_승인_전체_흐름이_정상동작한다() {
        // 1. 발행 요청
        viewModel.requestPublication(2000)
        assertTrue(viewModel.showPublishConfirm)
        assertEquals(2000, viewModel.publishRadius)

        // 2. 웹에 승인 전송
        viewModel.sendApproveToWeb()
        assertFalse(viewModel.showPublishConfirm)
        assertEquals("window.getApprove()", viewModel.jsCommand)

        // 3. JS 커맨드 소비
        viewModel.clearJsCommand()
        assertNull(viewModel.jsCommand)
    }

    @Test
    fun 발행_요청_후_취소_흐름이_정상동작한다() {
        viewModel.requestPublication(2000)
        assertTrue(viewModel.showPublishConfirm)

        viewModel.dismissConfirm()
        assertFalse(viewModel.showPublishConfirm)
        assertNull(viewModel.jsCommand)  // 취소 시 JS 커맨드 없음
    }
}