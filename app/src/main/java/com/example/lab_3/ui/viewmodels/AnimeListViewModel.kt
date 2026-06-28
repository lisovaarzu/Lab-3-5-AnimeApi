package com.example.lab_3.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab_3.data.repository.AnimeRepository
import com.example.lab_3.domain.models.Anime
import com.example.lab_3.ui.states.AnimeListStatus
import com.example.lab_3.ui.states.AnimeListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AnimeListViewModel @Inject constructor(
    private val repository: AnimeRepository
) : ViewModel() {

    var uiState by mutableStateOf(AnimeListUiState())
        private set

    private var searchJob: Job? = null
    private var loadJob: Job? = null

    private fun loadFavourites() {
        viewModelScope.launch {
            try {
                val favourites = repository.getFavourites()
                val favouriteIds = favourites.map { it.id }.toSet()

                uiState = uiState.copy(
                    favouriteList = favourites,
                    animeList = uiState.animeList.map { anime ->
                        anime.copy(isFavourite = anime.id in favouriteIds)
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }

    init {
        loadAnimeList()
        loadFavourites()
    }

    fun onSearchQueryChange(newValue: String) {
        searchJob?.cancel()

        uiState = uiState.copy(
            searchQuery = newValue
        )

        if (newValue.isBlank()) {
            loadAnimeList()
            return
        }

        searchJob = viewModelScope.launch {
            try {
                if (newValue == uiState.searchQuery) {
                    uiState = uiState.copy(
                        status = AnimeListStatus.Loading
                    )
                }

                delay(500)

                if (newValue != uiState.searchQuery) {
                    return@launch
                }

                val results = repository.searchAnime(newValue)

                val favourites = try {
                    repository.getFavourites()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    uiState.favouriteList
                }

                val favouriteIds = favourites.map { it.id }.toSet()

                val resultsWithFavourites = results.map { anime ->
                    anime.copy(
                        isFavourite = anime.id in favouriteIds
                    )
                }.distinctBy { it.id }

                if (newValue == uiState.searchQuery) {
                    uiState = uiState.copy(
                        animeList = resultsWithFavourites,
                        favouriteList = favourites,
                        status = if (resultsWithFavourites.isEmpty()) {
                            AnimeListStatus.Empty
                        } else {
                            AnimeListStatus.Success
                        }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (newValue == uiState.searchQuery) {
                    uiState = uiState.copy(
                        status = AnimeListStatus.Error(
                            e.message ?: "Search error"
                        )
                    )
                }
            }
        }
    }

    fun onFavouriteClick(anime: Anime) {
        viewModelScope.launch {
            try {
                repository.setFavourite(
                    anime,
                    !anime.isFavourite
                )

                val updatedAnimeList = uiState.animeList.map { current ->
                    if (current.id == anime.id) {
                        current.copy(
                            isFavourite = !anime.isFavourite
                        )
                    } else {
                        current
                    }
                }

                val updatedFavouriteList = if (anime.isFavourite) {
                    uiState.favouriteList.filter {
                        it.id != anime.id
                    }
                } else {
                    (
                            uiState.favouriteList +
                                    anime.copy(isFavourite = true)
                            ).distinctBy { it.id }
                }

                uiState = uiState.copy(
                    animeList = updatedAnimeList,
                    favouriteList = updatedFavouriteList
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = uiState.copy(
                    status = AnimeListStatus.Error(
                        "Failed to save favorite"
                    )
                )
            }
        }
    }

    fun loadAnimeList() {
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            uiState = uiState.copy(
                animeList = emptyList(),
                status = AnimeListStatus.Loading
            )

            try {
                val list = repository.getAnimeList(page = 1)

                val favourites = try {
                    repository.getFavourites()
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    uiState.favouriteList
                }

                val favouriteIds = favourites.map { it.id }.toSet()

                val uniqueList = list.map { anime ->
                    anime.copy(
                        isFavourite = anime.id in favouriteIds
                    )
                }.distinctBy { it.id }

                uiState = uiState.copy(
                    animeList = uniqueList,
                    favouriteList = favourites,
                    status = if (uniqueList.isEmpty()) {
                        AnimeListStatus.Empty
                    } else {
                        AnimeListStatus.Success
                    }
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = uiState.copy(
                    status = AnimeListStatus.Error(
                        e.message ?: "Download error"
                    )
                )
            }
        }
    }

    fun onRetry() {
        if (uiState.searchQuery.isBlank()) {
            loadAnimeList()
        } else {
            onSearchQueryChange(uiState.searchQuery)
        }
    }
}