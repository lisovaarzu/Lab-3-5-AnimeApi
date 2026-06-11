package com.example.lab_3.data.network

import com.example.lab_3.data.models.AnimeDetailResponse
import com.example.lab_3.data.models.AnimeResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JikanApi {
    @GET("anime")
    suspend fun getAnimeList(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<AnimeResponse>

    @GET("anime/{id}")
    suspend fun getAnimeDetail(
        @Path("id") id: Int
    ): Response<AnimeDetailResponse>

    @GET("anime")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<AnimeResponse>
}