package com.example.animedev.domain.repository

import com.example.animedev.domain.model.Anime

interface AnimeRepository {
    suspend fun getHeroRecommendation(): Anime
    suspend fun getAnimesByGenre(genreId: String): List<Anime>
}
