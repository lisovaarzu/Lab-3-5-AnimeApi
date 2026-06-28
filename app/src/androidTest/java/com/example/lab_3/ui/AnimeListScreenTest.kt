package com.example.lab_3.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lab_3.data.repository.AnimeRepository
import com.example.lab_3.domain.models.Anime
import com.example.lab_3.domain.models.AnimeDetail
import com.example.lab_3.ui.screens.AnimeDetailScreen
import com.example.lab_3.ui.screens.AnimeListScreen
import com.example.lab_3.ui.states.AnimeDetailUiState
import com.example.lab_3.ui.states.AnimeListStatus
import com.example.lab_3.ui.states.AnimeListUiState
import com.example.lab_3.ui.viewmodels.AnimeListViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnimeListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun errorState_clickRetry_requestsDataAndShowsSuccess() {
        val anime = Anime(
            id = 1,
            title = "Test Anime",
            imageUrl = null,
            episodes = 12,
            rating = 8.5,
            year = 2024,
            isFavourite = false
        )

        val repository = RetryAnimeRepository(anime)
        val viewModel = AnimeListViewModel(repository)

        composeTestRule.setContent {
            AnimeListScreen(
                uiState = viewModel.uiState,
                onSearchChange = viewModel::onSearchQueryChange,
                onAnimeClick = {},
                onFavouriteClick = viewModel::onFavouriteClick,
                onRetry = viewModel::onRetry
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.uiState.status is AnimeListStatus.Error
        }

        composeTestRule
            .onNodeWithText("Error: Network error")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Retry")
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.uiState.status == AnimeListStatus.Success
        }

        composeTestRule
            .onNodeWithText("Test Anime")
            .assertIsDisplayed()

        assert(repository.listRequestCount == 2)
    }

    @Test
    fun animeClick_navigatesFromListToDetails() {
        val anime = Anime(
            id = 1,
            title = "Test Anime",
            imageUrl = null,
            episodes = 12,
            rating = 8.5,
            year = 2024,
            isFavourite = false
        )

        val animeDetail = AnimeDetail(
            id = 1,
            title = "Test Anime",
            titleEnglish = null,
            imageUrl = null,
            synopsis = "Test synopsis",
            episodes = 12,
            status = "Finished Airing",
            rating = 8.5,
            year = 2024,
            genres = emptyList(),
            studios = emptyList()
        )

        composeTestRule.setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "list"
            ) {
                composable("list") {
                    AnimeListScreen(
                        uiState = AnimeListUiState(
                            animeList = listOf(anime),
                            status = AnimeListStatus.Success
                        ),
                        onSearchChange = {},
                        onAnimeClick = { animeId ->
                            navController.navigate("details/$animeId")
                        },
                        onFavouriteClick = {},
                        onRetry = {}
                    )
                }

                composable(
                    route = "details/{animeId}",
                    arguments = listOf(
                        navArgument("animeId") {
                            type = NavType.IntType
                        }
                    )
                ) {
                    AnimeDetailScreen(
                        uiState = AnimeDetailUiState(
                            animeDetail = animeDetail
                        ),
                        isFavourite = false,
                        onBack = {
                            navController.popBackStack()
                        },
                        onRetry = {},
                        onFavouriteClick = {}
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithText("Test Anime")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Anime Details")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Episodes: 12")
            .assertIsDisplayed()
    }

    @Test
    fun emptySearchResult_displaysEmptyMessage() {
        val emptyState = AnimeListUiState(
            searchQuery = "xyz",
            animeList = emptyList(),
            favouriteList = emptyList(),
            status = AnimeListStatus.Empty
        )

        composeTestRule.setContent {
            AnimeListScreen(
                uiState = emptyState,
                onSearchChange = {},
                onAnimeClick = {},
                onFavouriteClick = {},
                onRetry = {}
            )
        }

        composeTestRule
            .onNodeWithText("No results found")
            .assertIsDisplayed()
    }

    private class RetryAnimeRepository(
        private val anime: Anime
    ) : AnimeRepository {

        var listRequestCount: Int = 0
            private set

        override suspend fun getAnimeList(page: Int): List<Anime> {
            listRequestCount++

            if (listRequestCount == 1) {
                throw Exception("Network error")
            }

            return listOf(anime)
        }

        override suspend fun searchAnime(
            query: String,
            page: Int
        ): List<Anime> {
            return emptyList()
        }

        override suspend fun getAnimeDetail(id: Int): AnimeDetail {
            return AnimeDetail(
                id = anime.id,
                title = anime.title,
                titleEnglish = null,
                imageUrl = anime.imageUrl,
                synopsis = null,
                episodes = anime.episodes,
                status = "Finished Airing",
                rating = anime.rating,
                year = anime.year,
                genres = emptyList(),
                studios = emptyList()
            )
        }

        override suspend fun getFavourites(): List<Anime> {
            return emptyList()
        }

        override suspend fun setFavourite(
            anime: Anime,
            isFavourite: Boolean
        ) {
        }
    }
}