package com.example.lab_3.data.repository

import com.example.lab_3.data.local.AnimeDao
import com.example.lab_3.data.local.toDomain
import com.example.lab_3.data.local.toFavouriteEntity
import com.example.lab_3.data.network.JikanApi
import com.example.lab_3.domain.models.Anime
import com.example.lab_3.domain.models.AnimeDetail
import kotlinx.coroutines.withContext
import com.example.lab_3.data.network.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

interface AnimeRepository {
    suspend fun getAnimeList(page: Int = 1): List<Anime>
    suspend fun searchAnime(query: String, page: Int = 1): List<Anime>
    suspend fun getAnimeDetail(id: Int): AnimeDetail
    suspend fun getFavourites(): List<Anime>
    suspend fun setFavourite(anime: Anime, isFavourite: Boolean)
}

class AnimeRepositoryImpl @Inject constructor(
    private val api: JikanApi,
    private val animeDao: AnimeDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AnimeRepository {

    override suspend fun getFavourites(): List<Anime> = withContext(ioDispatcher){
        animeDao.getFavourites().map {it.toDomain()}
    }

    override suspend fun setFavourite(anime: Anime, isFavourite: Boolean) = withContext(ioDispatcher) {
        if (isFavourite){
            animeDao.upsert(anime.toFavouriteEntity())
        } else {
            animeDao.deleteById(anime.id)
        }
    }

    override suspend fun getAnimeList(page: Int): List<Anime> {
        repeat(2) { attempt ->
            try {
                val response = api.getAnimeList(page = page)

                if (!response.isSuccessful) {
                    if (response.code() == 429 && attempt == 0) {
                        kotlinx.coroutines.delay(1000)
                        return@repeat
                    }
                    throw Exception("HTTP ${response.code()}")
                }
                val body = response.body() ?: throw Exception("Empty body")
                return body.data.mapNotNull { it.toDomainOrNull() }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                if (attempt == 1) throw e
            }
        }
        return emptyList()
    }

    override suspend fun searchAnime(query: String, page: Int): List<Anime> {
        if (query.isBlank()) return emptyList()

        repeat(2) { attempt ->
            try {
                val response = api.searchAnime(query = query, page = page)

                if (!response.isSuccessful) {
                    if (response.code() == 429 && attempt == 0) {
                        kotlinx.coroutines.delay(1000)
                        return@repeat
                    }
                    throw Exception("HTTP ${response.code()}")
                }
                val body = response.body() ?: throw Exception("Empty body")
                return body.data.mapNotNull { it.toDomainOrNull() }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                if (attempt == 1) throw e
            }
        }
        return emptyList()
    }

    override suspend fun getAnimeDetail(id: Int): AnimeDetail {
        repeat(2) { attempt ->
            try {
                val response = api.getAnimeDetail(id)

                if (!response.isSuccessful) {
                    if (response.code() == 429 && attempt == 0) {
                        kotlinx.coroutines.delay(1000)
                        return@repeat
                    }
                    throw Exception("HTTP ${response.code()}")
                }

                return response.body()?.data?.toDomainOrNull()
                    ?: throw Exception("Empty body")
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                if (attempt == 1) throw e
            }
        }
        error("unreachable")
    }
}