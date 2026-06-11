package com.example.lab_3.ui.states

import com.example.lab_3.domain.models.AnimeDetail

data class AnimeDetailUiState(
    val isLoading: Boolean = false,
    val animeDetail: AnimeDetail? = null,
    val errorMessage: String? = null
)