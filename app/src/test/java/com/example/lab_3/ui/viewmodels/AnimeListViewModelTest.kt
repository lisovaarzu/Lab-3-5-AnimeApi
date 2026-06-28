package com.example.lab_3.ui.viewmodels

import com.example.lab_3.data.repository.AnimeRepository
import com.example.lab_3.domain.models.Anime
import com.example.lab_3.ui.states.AnimeListStatus
import com.google.common.truth.Truth.assertThat
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnimeListViewModelTest {

    private lateinit var repository: AnimeRepository
    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        repository = mockk()

        coEvery { repository.getFavourites() } returns emptyList()
        coEvery { repository.getAnimeList(page = 1) } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoadingAndListEmpty() = runTest(testDispatcher) {
        val viewModel = AnimeListViewModel(repository)

        assertThat(viewModel.uiState.status)
            .isEqualTo(AnimeListStatus.Loading)
        assertThat(viewModel.uiState.animeList).isEmpty()
    }

    @Test
    fun loadAnimeList_success_updatesUiState() = runTest(testDispatcher) {
        val viewModel = AnimeListViewModel(repository)
        advanceUntilIdle()

        val fakeAnimeList = listOf(
            Anime(1, "Anime 1", "url1", 12, 8.5, 2024, false),
            Anime(2, "Anime 2", "url2", 24, 9.0, 2023, false)
        )

        coEvery {
            repository.getAnimeList(page = 1)
        } returns fakeAnimeList

        viewModel.loadAnimeList()
        advanceUntilIdle()

        assertThat(viewModel.uiState.status)
            .isEqualTo(AnimeListStatus.Success)
        assertThat(viewModel.uiState.animeList).hasSize(2)
        assertThat(viewModel.uiState.animeList[0].title)
            .isEqualTo("Anime 1")
    }

    @Test
    fun loadAnimeList_error_updatesErrorState() = runTest(testDispatcher) {
        val viewModel = AnimeListViewModel(repository)
        advanceUntilIdle()

        coEvery {
            repository.getAnimeList(page = 1)
        } throws Exception("Network error")

        viewModel.loadAnimeList()
        advanceUntilIdle()

        val status = viewModel.uiState.status

        assertThat(status)
            .isInstanceOf(AnimeListStatus.Error::class.java)
        assertThat((status as AnimeListStatus.Error).message)
            .contains("Network error")
    }

    @Test
    fun retry_afterError_callsCorrectApi() = runTest(testDispatcher) {
        val viewModel = AnimeListViewModel(repository)
        advanceUntilIdle()

        clearMocks(
            repository,
            answers = false,
            recordedCalls = true
        )

        coEvery {
            repository.getAnimeList(page = 1)
        } throws Exception("Network error")

        viewModel.loadAnimeList()
        advanceUntilIdle()

        val errorStatus = viewModel.uiState.status

        assertThat(errorStatus)
            .isInstanceOf(AnimeListStatus.Error::class.java)
        assertThat((errorStatus as AnimeListStatus.Error).message)
            .contains("Network error")

        val fakeAnimeList = listOf(
            Anime(1, "Anime 1", null, 12, 8.5, 2024, false)
        )

        coEvery {
            repository.getAnimeList(page = 1)
        } returns fakeAnimeList

        viewModel.onRetry()
        advanceUntilIdle()

        coVerify(exactly = 2) {
            repository.getAnimeList(page = 1)
        }

        assertThat(viewModel.uiState.status)
            .isEqualTo(AnimeListStatus.Success)
        assertThat(viewModel.uiState.animeList).hasSize(1)
        assertThat(viewModel.uiState.animeList[0].title)
            .isEqualTo("Anime 1")
    }

    @Test
    fun searchAnime_emptyResult_setsEmptyState() = runTest(testDispatcher) {
        val viewModel = AnimeListViewModel(repository)
        advanceUntilIdle()

        coEvery {
            repository.searchAnime("xyz", 1)
        } returns emptyList()

        viewModel.onSearchQueryChange("xyz")
        advanceTimeBy(500)
        advanceUntilIdle()

        assertThat(viewModel.uiState.status)
            .isEqualTo(AnimeListStatus.Empty)
        assertThat(viewModel.uiState.animeList).isEmpty()
    }

    @Test
    fun search_rapidInput_cancelsPreviousRequests() =
        runTest(testDispatcher) {
            val viewModel = AnimeListViewModel(repository)
            advanceUntilIdle()

            coEvery {
                repository.searchAnime("a", 1)
            } coAnswers {
                delay(1000)
                listOf(
                    Anime(
                        1,
                        "Anime A",
                        null,
                        null,
                        null,
                        null,
                        false
                    )
                )
            }

            coEvery {
                repository.searchAnime("ab", 1)
            } coAnswers {
                delay(500)
                listOf(
                    Anime(
                        2,
                        "Anime AB",
                        null,
                        null,
                        null,
                        null,
                        false
                    )
                )
            }

            coEvery {
                repository.searchAnime("abc", 1)
            } returns listOf(
                Anime(
                    3,
                    "Anime ABC",
                    null,
                    null,
                    null,
                    null,
                    false
                )
            )

            viewModel.onSearchQueryChange("a")
            advanceTimeBy(100)

            viewModel.onSearchQueryChange("ab")
            advanceTimeBy(100)

            viewModel.onSearchQueryChange("abc")
            advanceTimeBy(500)
            advanceUntilIdle()

            coVerify(exactly = 1) {
                repository.searchAnime("abc", 1)
            }
            coVerify(exactly = 0) {
                repository.searchAnime("a", 1)
            }
            coVerify(exactly = 0) {
                repository.searchAnime("ab", 1)
            }

            assertThat(viewModel.uiState.status)
                .isEqualTo(AnimeListStatus.Success)
            assertThat(viewModel.uiState.animeList).hasSize(1)
            assertThat(viewModel.uiState.animeList[0].title)
                .isEqualTo("Anime ABC")
        }
}