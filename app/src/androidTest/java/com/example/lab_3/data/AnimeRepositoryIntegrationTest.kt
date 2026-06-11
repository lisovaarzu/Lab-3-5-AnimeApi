package com.example.lab_3.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.lab_3.data.local.AnimeDao
import com.example.lab_3.data.local.AnimeDatabase
import com.example.lab_3.data.network.JikanApi
import com.example.lab_3.data.repository.AnimeRepositoryImpl
import com.example.lab_3.domain.models.Anime
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AnimeRepositoryIntegrationTest {

    private lateinit var database: AnimeDatabase
    private lateinit var dao: AnimeDao
    private lateinit var repository: AnimeRepositoryImpl
    private lateinit var mockApi: JikanApi

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, AnimeDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.AnimeDao()
        mockApi = mockk(relaxed = true)
        repository = AnimeRepositoryImpl(api = mockApi, animeDao = dao)
    }

    @After
    fun tearDown() {
        database.close()
    }
    @Test
    fun setFavourite_savesToRoom() = runTest {
        val anime = Anime(1, "Test Anime", "url", 12, 8.5, 2024, false)

        repository.setFavourite(anime, isFavourite = true)
        val favourites = repository.getFavourites()

        assertThat(favourites.size).isEqualTo(1)
        assertThat(favourites[0].id).isEqualTo(1)
    }

    @Test
    fun addFavouriteTwice_noDuplicate() = runTest {
        val anime = Anime(1, "Test Anime", "url", 12, 8.5, 2024, false)

        repository.setFavourite(anime, isFavourite = true)
        repository.setFavourite(anime, isFavourite = true)
        val favourites = repository.getFavourites()

        assertThat(favourites.size).isEqualTo(1)
    }

    @Test
    fun removeFavourite_deletesFromRoom() = runTest {
        val anime = Anime(1, "Test Anime", "url", 12, 8.5, 2024, false)

        repository.setFavourite(anime, isFavourite = true)
        repository.setFavourite(anime, isFavourite = false)
        val favourites = repository.getFavourites()

        assertThat(favourites.size).isEqualTo(0)
    }

    @Test
    fun getAnimeList_success_returnsData() = runTest {
        val animeDto = com.example.lab_3.data.models.AnimeDto(
            mal_id = 1,
            title = "Test Anime",
            images = com.example.lab_3.data.models.Images(
                jpg = com.example.lab_3.data.models.JpgImage("https://example.com/image.jpg", null, null),
                webp = null
            ),
            episodes = 12,
            score = 8.5,
            year = 2024
        )

        val animeResponse = com.example.lab_3.data.models.AnimeResponse(
            data = listOf(animeDto),
            pagination = com.example.lab_3.data.models.Pagination(1, false, 1, null)
        )

        val response = Response.success(animeResponse)
        coEvery { mockApi.getAnimeList(page = 1, limit = 10) } returns response

        val result = repository.getAnimeList(page = 1)

        assertThat(result).isNotEmpty()
        assertThat(result.size).isEqualTo(1)
        assertThat(result[0].id).isEqualTo(1)
        assertThat(result[0].title).isEqualTo("Test Anime")
        assertThat(result[0].episodes).isEqualTo(12)
        assertThat(result[0].rating).isEqualTo(8.5)
    }

    @Test
    fun getAnimeList_httpError_throwsException() = runTest {
        val response = Response.error<com.example.lab_3.data.models.AnimeResponse>(500, okhttp3.ResponseBody.create(null, "Error"))
        coEvery { mockApi.getAnimeList(page = 1, limit = 10) } returns response

        var exceptionThrown = false
        try {
            repository.getAnimeList(page = 1)
        } catch (e: Exception) {
            exceptionThrown = true
            assertThat(e.message).contains("HTTP 500")
        }
        assertThat(exceptionThrown).isTrue()
    }

    @Test
    fun searchAnime_success_returnsData() = runTest {
        val animeDto = com.example.lab_3.data.models.AnimeDto(
            mal_id = 2,
            title = "Naruto",
            images = com.example.lab_3.data.models.Images(
                jpg = com.example.lab_3.data.models.JpgImage("https://example.com/naruto.jpg", null, null),
                webp = null
            ),
            episodes = 220,
            score = 8.0,
            year = 2002
        )

        val animeResponse = com.example.lab_3.data.models.AnimeResponse(
            data = listOf(animeDto),
            pagination = com.example.lab_3.data.models.Pagination(1, false, 1, null)
        )

        val response = Response.success(animeResponse)
        coEvery { mockApi.searchAnime(query = "Naruto", page = 1, limit = 10) } returns response

        val result = repository.searchAnime(query = "Naruto", page = 1)

        assertThat(result).isNotEmpty()
        assertThat(result.size).isEqualTo(1)
        assertThat(result[0].id).isEqualTo(2)
        assertThat(result[0].title).isEqualTo("Naruto")
    }

    @Test
    fun searchAnime_emptyQuery_returnsEmptyList() = runTest {
        val result = repository.searchAnime(query = "", page = 1)

        assertThat(result).isEmpty()
        coEvery { mockApi.searchAnime(any(), any(), any()) } returns mockk(relaxed = true)
    }

    @Test
    fun searchAnime_httpError_throwsException() = runTest {
        val response = Response.error<com.example.lab_3.data.models.AnimeResponse>(404, okhttp3.ResponseBody.create(null, "Not Found"))
        coEvery { mockApi.searchAnime(query = "NotFound", page = 1, limit = 10) } returns response

        var exceptionThrown = false
        try {
            repository.searchAnime(query = "NotFound", page = 1)
        } catch (e: Exception) {
            exceptionThrown = true
            assertThat(e.message).contains("HTTP 404")
        }
        assertThat(exceptionThrown).isTrue()
    }
}