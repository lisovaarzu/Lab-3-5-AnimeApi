package com.example.lab_3.ui.states

import com.example.lab_3.domain.models.Anime

data class AnimeListUiState(
    val searchQuery: String = "",
    val animeList: List<Anime> = emptyList(),
    val favouriteList: List<Anime> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
    val isEmpty: Boolean = false
)