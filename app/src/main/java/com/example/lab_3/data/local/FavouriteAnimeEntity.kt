package com.example.lab_3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lab_3.domain.models.Anime

@Entity(tableName = "favourites_anime")
data class FavouriteAnimeEntity(

    @PrimaryKey
    val id: Int,
    val title: String,
    val imageUrl: String? = null,
    val episodes: Int?,
    val rating: Double?,
    val year: Int?,
)

fun FavouriteAnimeEntity.toDomain(): Anime = Anime(
    id,
    title,
    imageUrl,
    episodes,
    rating,
    year,
    isFavourite = true,
)

fun Anime.toFavouriteEntity(): FavouriteAnimeEntity = FavouriteAnimeEntity(
    id,
    title,
    imageUrl,
    episodes,
    rating,
    year,
)