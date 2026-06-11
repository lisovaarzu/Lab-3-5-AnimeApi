package com.example.lab_3.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab_3.ui.screens.AnimeDetailScreen
import com.example.lab_3.ui.screens.AnimeListScreen
import com.example.lab_3.ui.viewmodels.AnimeDetailViewModel
import com.example.lab_3.ui.viewmodels.AnimeListViewModel

object AnimeRoutes {
    const val LIST_ROUTE = "anime_list"
    const val DETAILS_ROUTE_PATTERN = "anime_detail/{animeId}"
    const val ANIME_ID_ARG = "animeId"
    fun details(animeId: Int): String = "anime_detail/$animeId"
}

@Composable
fun NavGraph(){
    val navController = rememberNavController()

    val animeListViewModel: AnimeListViewModel = hiltViewModel()
    NavHost(
        navController = navController,
        startDestination = AnimeRoutes.LIST_ROUTE
    ) {
        composable(AnimeRoutes.LIST_ROUTE) {
            AnimeListScreen(
                uiState = animeListViewModel.uiState,
                onSearchChange = animeListViewModel::onSearchQueryChange,
                onAnimeClick = { animeId ->
                    navController.navigate(AnimeRoutes.details(animeId))
                },
                onFavouriteClick = animeListViewModel::onFavouriteClick,
                onRetry = { animeListViewModel.onRetry() }
            )
        }

        composable(
            route = AnimeRoutes.DETAILS_ROUTE_PATTERN,
            arguments = listOf(
                navArgument(AnimeRoutes.ANIME_ID_ARG) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val animeDetailViewModel: AnimeDetailViewModel = hiltViewModel()
            val animeId = backStackEntry.arguments?.getInt(AnimeRoutes.ANIME_ID_ARG)
                ?: return@composable

            LaunchedEffect(animeId) {
                animeDetailViewModel.load(animeId)
            }

            AnimeDetailScreen(
                uiState = animeDetailViewModel.uiState,
                onBack = { navController.popBackStack() },
                onRetry = { animeDetailViewModel.load(animeId) }
            )
        }
    }
}