package com.example.lab_3.ui.states

import com.example.lab_3.domain.models.Anime

sealed interface AnimeListStatus {
    data object Loading : AnimeListStatus
    data object Empty : AnimeListStatus
    data object Success : AnimeListStatus
    data class Error(val message: String) : AnimeListStatus
}

data class AnimeListUiState(
    val searchQuery: String = "",
    val animeList: List<Anime> = emptyList(),
    val favouriteList: List<Anime> = emptyList(),
    val status: AnimeListStatus = AnimeListStatus.Loading
)