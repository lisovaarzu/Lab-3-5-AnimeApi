package com.example.lab_3.domain.models

data class Anime(
    val id: Int,
    val title: String,
    val imageUrl: String? = null,
    val episodes: Int?,
    val rating: Double?,
    val year: Int?,
    val isFavourite: Boolean = false
)
