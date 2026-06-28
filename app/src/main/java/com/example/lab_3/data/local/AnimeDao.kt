package com.example.lab_3.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AnimeDao {

    @Query("SELECT * FROM favourites_anime ORDER BY title")
    suspend fun  getFavourites(): List<FavouriteAnimeEntity>

    @Query("SELECT id FROM favourites_anime ORDER BY id")
    suspend fun  getFavouritesIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(anime: FavouriteAnimeEntity)

    @Query("DELETE FROM favourites_anime WHERE id = :id")
    suspend fun deleteById(id: Int)
}