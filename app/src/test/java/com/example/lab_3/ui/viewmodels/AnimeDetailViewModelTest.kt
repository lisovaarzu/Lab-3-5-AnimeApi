package com.example.lab_3.ui.viewmodels

import com.example.lab_3.data.repository.AnimeRepository
import com.example.lab_3.domain.models.AnimeDetail
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeDetailViewModelTest {
    private lateinit var repository: AnimeRepository
    private lateinit var viewModel: AnimeDetailViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = AnimeDetailViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun load_success_updatesUiState() = runTest {
        val detail = AnimeDetail(
            id = 1,
            title = "Test Anime",
            titleEnglish = "Test",
            imageUrl = "https://example.com/image.jpg",
            synopsis = "Test synopsis",
            episodes = 12,
            status = "Finished Airing",
            rating = 8.5,
            year = 2024,
            genres = listOf("Action", "Adventure"),
            studios = listOf("Studio A", "Studio B")
        )
        coEvery { repository.getAnimeDetail(1) } returns detail

        viewModel.load(1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.isLoading).isFalse()
        assertThat(viewModel.uiState.animeDetail).isEqualTo(detail)
        assertThat(viewModel.uiState.errorMessage).isNull()
    }

    @Test
    fun load_error_updatesErrorMessage() = runTest {
        coEvery { repository.getAnimeDetail(1) } throws Exception("Network error")

        viewModel.load(1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.isLoading).isFalse()
        assertThat(viewModel.uiState.errorMessage).isNotNull()
        assertThat(viewModel.uiState.errorMessage).contains("Network error")
        assertThat(viewModel.uiState.animeDetail).isNull()
    }

    @Test
    fun retry_afterError_loadsSuccessfully() = runTest {
        coEvery { repository.getAnimeDetail(1) } throws Exception("Network error")

        viewModel.load(1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.uiState.errorMessage).isNotNull()

        val detail = AnimeDetail(
            id = 1,
            title = "Test Anime",
            titleEnglish = null,
            imageUrl = "",
            synopsis = null,
            episodes = 12,
            status = "Finished",
            rating = null,
            year = null,
            genres = emptyList(),
            studios = emptyList()
        )
        coEvery { repository.getAnimeDetail(1) } returns detail

        viewModel.load(1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertThat(viewModel.uiState.errorMessage).isNull()
        assertThat(viewModel.uiState.animeDetail).isEqualTo(detail)
    }
}