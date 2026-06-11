package com.example.lab_3.data.models

import com.example.lab_3.domain.models.AnimeDetail
import kotlin.Int

data class AnimeDetailResponse(
    val data: AnimeDetailDto
)

data class AnimeDetailDto(
    val mal_id: Int,
    val title: String,
    val title_english: String?,
    val images: Images?,
    val synopsis: String?,
    val episodes: Int?,
    val status: String,
    val score: Double?,
    val year: Int?,
    val genres: List<Genre>?,
    val studios: List<Studio>?
){
    fun  toDomainOrNull(): AnimeDetail? {
        val safeTitle = title.takeIf { it.isNotBlank() } ?: return null

        return AnimeDetail(
            id = mal_id,
            title = safeTitle,
            titleEnglish = title_english,
            imageUrl =
                images?.jpg?.large_image_url
                    ?: images?.jpg?.image_url
                    ?: images?.webp?.image_url
                    ?: "",
            synopsis = synopsis,
            episodes = episodes,
            status = status,
            rating = score,
            year = year,
            genres = genres?.map {it.name} ?: emptyList(),
            studios = studios?.map { it.name} ?: emptyList()
        )
    }
}
