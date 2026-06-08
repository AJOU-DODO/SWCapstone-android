package com.example.swcapstone_android.ui

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.swcapstone_android.ui.setting.SettingViewModel
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingViewModelTest {

    private lateinit var application: Application
    private lateinit var viewModel: SettingViewModel

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()

        // ✅ 테스트 간 간섭 방지: 매번 SharedPreferences 초기화
        application.getSharedPreferences("dodo_settings", Context.MODE_PRIVATE)
            .edit().clear().commit()

        viewModel = SettingViewModel(application)
    }

    @After
    fun tearDown() {
        // 테스트 후 정리
        application.getSharedPreferences("dodo_settings", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    // ─────────────────────────────────────────────
    // 초기 상태 검증
    // ─────────────────────────────────────────────

    @Test
    fun 초기_isCategoryEnabled는_true이다() {
        assertTrue(viewModel.isCategoryEnabled)
    }

    @Test
    fun 초기_searchRadius는_2000이다() {
        assertEquals(2000, viewModel.searchRadius)
    }

    // ─────────────────────────────────────────────
    // toggleCategory 검증
    // ─────────────────────────────────────────────

    @Test
    fun toggleCategory_false_호출시_isCategoryEnabled가_false가_된다() {
        viewModel.toggleCategory(false)
        assertFalse(viewModel.isCategoryEnabled)
    }

    @Test
    fun toggleCategory_true_호출시_isCategoryEnabled가_true가_된다() {
        viewModel.toggleCategory(false)
        viewModel.toggleCategory(true)
        assertTrue(viewModel.isCategoryEnabled)
    }

    @Test
    fun toggleCategory_호출시_SharedPreferences에_저장된다() {
        viewModel.toggleCategory(false)

        val saved = application
            .getSharedPreferences("dodo_settings", Context.MODE_PRIVATE)
            .getBoolean("category_enabled", true)

        assertFalse(saved)
    }

    @Test
    fun toggleCategory_저장값이_새_ViewModel_인스턴스에_반영된다() {
        viewModel.toggleCategory(false)

        // 앱 재시작 시뮬레이션: 새 인스턴스 생성
        val newViewModel = SettingViewModel(application)
        assertFalse(newViewModel.isCategoryEnabled)
    }

    // ─────────────────────────────────────────────
    // updateRadius 검증
    // ─────────────────────────────────────────────

    @Test
    fun updateRadius_호출시_searchRadius가_업데이트된다() {
        viewModel.updateRadius(5000)
        assertEquals(5000, viewModel.searchRadius)
    }

    @Test
    fun updateRadius_호출시_SharedPreferences에_저장된다() {
        viewModel.updateRadius(1000)

        val saved = application
            .getSharedPreferences("dodo_settings", Context.MODE_PRIVATE)
            .getInt("search_radius", 2000)

        assertEquals(1000, saved)
    }

    @Test
    fun updateRadius_저장값이_새_ViewModel_인스턴스에_반영된다() {
        viewModel.updateRadius(3000)

        val newViewModel = SettingViewModel(application)
        assertEquals(3000, newViewModel.searchRadius)
    }

    @Test
    fun updateRadius_여러번_호출시_마지막_값이_유지된다() {
        viewModel.updateRadius(1000)
        viewModel.updateRadius(3000)
        viewModel.updateRadius(5000)

        assertEquals(5000, viewModel.searchRadius)

        val saved = application
            .getSharedPreferences("dodo_settings", Context.MODE_PRIVATE)
            .getInt("search_radius", 2000)
        assertEquals(5000, saved)
    }
}