package com.example.lab_3.data.local

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(
    entities = [FavouriteAnimeEntity::class],
    version = 1,
)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun AnimeDao(): AnimeDao
}