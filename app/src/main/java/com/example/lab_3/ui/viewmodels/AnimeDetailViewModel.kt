package com.example.lab_3.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab_3.data.repository.AnimeRepository
import com.example.lab_3.ui.states.AnimeDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnimeDetailViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {

    var uiState by mutableStateOf(AnimeDetailUiState(isLoading = true))
        private set

    private var loadJob: Job? = null

    fun load(animeId: Int) {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null,
                animeDetail = null
            )

            try {
                val detail = repository.getAnimeDetail(animeId)

                uiState = uiState.copy(
                    animeDetail = detail,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = uiState.copy(
                    animeDetail = null,
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load details"
                )
            }
        }
    }
}