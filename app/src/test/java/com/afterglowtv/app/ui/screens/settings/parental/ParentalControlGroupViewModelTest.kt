package com.afterglowtv.app.ui.screens.settings.parental

import androidx.lifecycle.SavedStateHandle
import com.afterglowtv.data.preferences.PreferencesRepository
import com.afterglowtv.domain.model.Category
import com.afterglowtv.domain.model.ContentType
import com.afterglowtv.domain.repository.CategoryRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ParentalControlGroupViewModelTest {

    private val categoryRepository: CategoryRepository = mock()
    private val preferencesRepository: PreferencesRepository = mock()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(preferencesRepository.getHiddenCategoryIds(any(), any())).thenReturn(flowOf(emptySet()))
        whenever(preferencesRepository.hasParentalPin).thenReturn(flowOf(false))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `adult tagged category starts unlocked and can be toggled by the user`() = runTest {
        val adultCategory = Category(
            id = 42L,
            name = "Adult",
            type = ContentType.LIVE,
            isAdult = true,
            isUserProtected = false
        )
        whenever(categoryRepository.getCategories(7L)).thenReturn(flowOf(listOf(adultCategory)))
        val viewModel = createViewModel()

        val initial = viewModel.uiState.first { !it.isLoading }.categories.single()
        assertThat(initial.isProtected).isFalse()
        assertThat(initial.isInitiallyProtected).isFalse()

        viewModel.toggleCategoryProtection(adultCategory)
        advanceUntilIdle()

        val toggled = viewModel.uiState.value.categories.single()
        assertThat(toggled.isProtected).isTrue()
        assertThat(viewModel.uiState.value.hasPendingProtectionChanges).isTrue()
        assertThat(viewModel.uiState.value.pendingProtectionChangeCount).isEqualTo(1)
    }

    private fun createViewModel(): ParentalControlGroupViewModel =
        ParentalControlGroupViewModel(
            categoryRepository = categoryRepository,
            preferencesRepository = preferencesRepository,
            savedStateHandle = SavedStateHandle(mapOf("providerId" to 7L))
        )
}
