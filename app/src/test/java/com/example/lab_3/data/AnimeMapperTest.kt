package com.example.lab_3.data

import com.example.lab_3.data.models.AnimeDto
import com.example.lab_3.data.models.Images
import com.example.lab_3.data.models.JpgImage
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AnimeMapperTest {

    @Test
    fun toDomainOrNull_validData_returnsAnime() {
        val dto = AnimeDto(
            mal_id = 1,
            title = "Cowboy Bebop",
            images = Images(
                jpg = JpgImage("https://example.com/image.jpg", null, null),
                webp = null
            ),
            episodes = 26,
            score = 8.8,
            year = 1998
        )

        val result = dto.toDomainOrNull()

        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(1)
        assertThat(result?.title).isEqualTo("Cowboy Bebop")
        assertThat(result?.episodes).isEqualTo(26)
        assertThat(result?.rating).isEqualTo(8.8)
    }

    @Test
    fun toDomainOrNull_blankTitle_returnsNull() {
        val dto = AnimeDto(
            mal_id = 1,
            title = "",
            images = Images(
                jpg = JpgImage("https://example.com/image.jpg", null, null),
                webp = null
            ),
            episodes = 26,
            score = 8.8,
            year = 1998
        )

        val result = dto.toDomainOrNull()

        assertThat(result).isNull()
    }
}