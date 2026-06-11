package com.example.lab_3.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.ui.layout.ContentScale
import com.example.lab_3.ui.states.AnimeDetailUiState
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailScreen(
    uiState: AnimeDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anime Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
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
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
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
                uiState.animeDetail != null -> {
                    val anime = uiState.animeDetail

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        ) {
                            AsyncImage(
                                model = anime.imageUrl,
                                contentDescription = anime.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(
                                                androidx.compose.ui.graphics.Color.Transparent,
                                                androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f)
                                            )
                                        )
                                    )
                            )
                        }

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = anime.title,
                                style = MaterialTheme.typography.headlineMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            anime.episodes?.let { episodes ->
                                Text(
                                    text = "Episodes: $episodes",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            anime.rating?.let{ rating ->
                                Text(
                                    text = "Rating: ${String.format("%.1f", rating)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            anime.year?.let { year ->
                                Text(
                                    text = "Year: $year",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (anime.genres.isNotEmpty()) {
                                Text(
                                    text = "Genres: ${anime.genres.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            if (anime.studios.isNotEmpty()) {
                                Text(
                                    text = "Studios: ${anime.studios.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            if (!anime.synopsis.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Synopsis",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = anime.synopsis,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}