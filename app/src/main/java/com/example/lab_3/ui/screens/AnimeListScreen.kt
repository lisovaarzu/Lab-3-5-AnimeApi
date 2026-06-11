package com.example.lab_3.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lab_3.domain.models.Anime
import com.example.lab_3.ui.states.AnimeListUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeListScreen(
    uiState: AnimeListUiState,
    onSearchChange: (String) -> Unit,
    onAnimeClick: (Int) -> Unit,
    onFavouriteClick: (Anime) -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Anime List") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search by title") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Error: ${uiState.errorMessage}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }
                uiState.hasSearched && uiState.animeList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No results found")
                    }
                }

                !uiState.hasSearched && uiState.favouriteList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No favourites yet")
                    }
                }

                else -> {
                    val list = if (uiState.hasSearched) {
                        uiState.animeList
                    } else {
                        uiState.favouriteList
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(list, key = { it.id }) { anime ->
                            AnimeCard(
                                anime = anime,
                                onAnimeClick = { onAnimeClick(anime.id) },
                                onFavouriteClick = { onFavouriteClick(anime) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimeCard(
    anime: Anime,
    onAnimeClick: () -> Unit,
    onFavouriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAnimeClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = anime.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )

                anime.episodes?.let { episodes ->
                    Text(
                        text = "Episodes: $episodes",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                anime.rating?.let { rating ->
                    Text(
                        text = "Rating: ${String.format("%.1f", rating)}/10",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                anime.year?.let { year ->
                    Text(
                        text = "Year: $year",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onFavouriteClick) {
                Text(
                    text = if (anime.isFavourite) "★" else "☆",
                    fontSize = 24.sp
                )
            }

            AsyncImage(
                model = anime.imageUrl,
                contentDescription = anime.title,
                modifier = Modifier.size(width = 70.dp, height = 100.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}