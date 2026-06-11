package com.example.lab_3.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lab_3.ui.screens.AnimeListScreen
import com.example.lab_3.ui.states.AnimeListUiState
import com.example.lab_3.domain.models.Anime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnimeListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun errorState_clickRetry_transitionsToSuccess() {
        val errorState = AnimeListUiState(
            errorMessage = "Network error"
        )
        val successState = AnimeListUiState(
            animeList = listOf(
                Anime(1, "Test Anime", "url", 12, 8.5, 2024, false)
            ),
            favouriteList = listOf(
                Anime(1, "Test Anime", "url", 12, 8.5, 2024, false)
            ),
            isLoading = false,
            errorMessage = null,
            hasSearched = false
        )

        val currentState = mutableStateOf(errorState)

        composeTestRule.setContent {
            AnimeListScreen(
                uiState = currentState.value,
                onSearchChange = {},
                onAnimeClick = {},
                onFavouriteClick = {},
                onRetry = {
                    currentState.value = successState
                }
            )
        }

        composeTestRule.onNodeWithText("Error: Network error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()

        composeTestRule.onNodeWithText("Retry").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Test Anime").assertIsDisplayed()
        composeTestRule.onNodeWithText("Error: Network error").assertDoesNotExist()
    }

    @Test
    fun emptySearchResult_displaysEmptyMessage() {
        val emptyState = AnimeListUiState(
            animeList = emptyList(),
            favouriteList = emptyList(),
            isLoading = false,
            errorMessage = null,
            hasSearched = true
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

        composeTestRule.onNodeWithText("No results found").assertIsDisplayed()
    }
}