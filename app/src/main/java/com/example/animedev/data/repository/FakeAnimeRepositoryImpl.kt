package com.example.animedev.data.repository

import com.example.animedev.data.FakeDataSource
import com.example.animedev.domain.model.Anime
import com.example.animedev.domain.repository.AnimeRepository
import kotlinx.coroutines.delay

class FakeAnimeRepositoryImpl : AnimeRepository {

    override suspend fun getHeroRecommendation(): Anime {
        delay(600)
        return FakeDataSource.heroAnime
    }

    override suspend fun getAnimesByGenre(genreId: String): List<Anime> {
        delay(400)
        return FakeDataSource.animeCatalog.filter { anime ->
            anime.genres.any { it.id == genreId }
        }
    }
}
