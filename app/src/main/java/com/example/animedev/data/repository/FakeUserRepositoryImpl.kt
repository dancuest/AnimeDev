package com.example.animedev.data.repository

import com.example.animedev.domain.model.Genre
import com.example.animedev.domain.repository.UserRepository
import kotlinx.coroutines.delay

class FakeUserRepositoryImpl : UserRepository {

    override suspend fun getPreferredGenres(): List<Genre> {
        // Simular una llamada de red
        delay(500)
        return listOf(
            Genre(id = "1", name = "Shonen"),
            Genre(id = "2", name = "Seinen"),
            Genre(id = "3", name = "Aventura"),
        )
    }
}
