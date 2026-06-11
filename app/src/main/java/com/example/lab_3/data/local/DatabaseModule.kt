package com.example.lab_3.data.local

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAnimeDatabase(
        @ApplicationContext context: Context
    ): AnimeDatabase =
        Room.databaseBuilder(
            context,
            AnimeDatabase::class.java,
            "anime.db",
        ).build()

    @Provides
    @Singleton
    fun provideAnimeDao(
        database: AnimeDatabase
    ): AnimeDao = database.AnimeDao()


}