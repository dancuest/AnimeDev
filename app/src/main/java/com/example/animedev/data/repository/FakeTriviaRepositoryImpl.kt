package com.example.animedev.data.repository

import com.example.animedev.data.FakeDataSource
import com.example.animedev.domain.model.Anime
import com.example.animedev.domain.model.DurationType
import com.example.animedev.domain.model.EmissionStatus
import com.example.animedev.domain.model.Trivias.TriviaDifficulty
import com.example.animedev.domain.model.Trivias.TriviaQuestion
import com.example.animedev.domain.model.Trivias.TriviaSummary
import com.example.animedev.domain.repository.TriviaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.math.ceil
import kotlin.math.max

object FakeTriviaRepositoryImpl : TriviaRepository {

    private const val DEFAULT_QUESTION_COUNT = 3

    private data class TriviaStats(
        val lastScore: Int? = null,
        val totalQuestions: Int = DEFAULT_QUESTION_COUNT,
        val lastDifficulty: TriviaDifficulty? = null,
        val bestScore: Int = 0
    )

    private val statsFlow = MutableStateFlow<Map<Long, TriviaStats>>(emptyMap())

    private val questionBank: Map<Long, Map<TriviaDifficulty, List<TriviaQuestion>>> =
        FakeDataSource.animeCatalog.associate { anime ->
            anime.id to buildQuestionSet(anime)
        }

    override fun getTriviaSummaries(): Flow<List<TriviaSummary>> = statsFlow.map { stats ->
        FakeDataSource.animeCatalog.map { anime ->
            val animeStats = stats[anime.id]
            TriviaSummary(
                anime = anime,
                lastScore = animeStats?.lastScore,
                totalQuestions = animeStats?.totalQuestions ?: DEFAULT_QUESTION_COUNT,
                lastDifficulty = animeStats?.lastDifficulty,
                bestScore = animeStats?.bestScore ?: 0
            )
        }
    }

    override suspend fun getQuestions(
        animeId: Long,
        difficulty: TriviaDifficulty
    ): List<TriviaQuestion> {
        delay(400)
        val questionsByDifficulty = questionBank[animeId]
            ?: error("No hay trivias disponibles para el anime con id $animeId")
        return questionsByDifficulty[difficulty]
            ?: error("No hay preguntas para la dificultad $difficulty")
    }

    override suspend fun recordResult(
        animeId: Long,
        difficulty: TriviaDifficulty,
        score: Int,
        totalQuestions: Int
    ) {
        val current = statsFlow.value[animeId]
        val updated = TriviaStats(
            lastScore = score,
            totalQuestions = totalQuestions,
            lastDifficulty = difficulty,
            bestScore = max(current?.bestScore ?: 0, score)
        )
        statsFlow.value = statsFlow.value + (animeId to updated)
    }

    private fun buildQuestionSet(anime: Anime): Map<TriviaDifficulty, List<TriviaQuestion>> =
        mapOf(
            TriviaDifficulty.EASY to listOf(
                buildDurationQuestion(anime),
                buildStatusQuestion(anime),
                buildGenreQuestion(anime)
            ),
            TriviaDifficulty.MEDIUM to listOf(
                buildReleaseYearQuestion(anime),
                buildEpisodesQuestion(anime),
                buildOriginalTitleQuestion(anime)
            ),
            TriviaDifficulty.HARD to listOf(
                buildStatementQuestion(anime),
                buildMissingGenreQuestion(anime),
                buildBingeTimeQuestion(anime)
            )
        )

    private fun buildDurationQuestion(anime: Anime): TriviaQuestion {
        val options = DurationType.entries.map { it.toReadableText() }
        val correctIndex = options.indexOf(anime.durationType.toReadableText())
        return TriviaQuestion(
            id = "${anime.id}_duration",
            animeId = anime.id,
            difficulty = TriviaDifficulty.EASY,
            question = "¿Qué duración aproximada tienen los episodios de ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "La serie se considera ${anime.durationType.toReadableText()} por la extensión de cada capítulo."
        )
    }

    private fun buildStatusQuestion(anime: Anime): TriviaQuestion {
        val options = EmissionStatus.entries.map { it.toReadableText() }
        val correctIndex = options.indexOf(anime.emissionStatus.toReadableText())
        return TriviaQuestion(
            id = "${anime.id}_status",
            animeId = anime.id,
            difficulty = TriviaDifficulty.EASY,
            question = "¿Cuál es el estado de emisión actual de ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "Actualmente la serie se encuentra ${anime.emissionStatus.toReadableText().lowercase()}"
        )
    }

    private fun buildGenreQuestion(anime: Anime): TriviaQuestion {
        val correctGenre = anime.genres.first()
        val wrongGenres = FakeDataSource.genres.filter { it.id != correctGenre.id }
            .map { it.name }
        val options = (listOf(correctGenre.name) + wrongGenres.take(3)).shuffled()
        val correctIndex = options.indexOf(correctGenre.name)
        return TriviaQuestion(
            id = "${anime.id}_genre",
            animeId = anime.id,
            difficulty = TriviaDifficulty.EASY,
            question = "¿Qué género representa mejor a ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "${anime.title} destaca por su componente ${correctGenre.name.lowercase()}"
        )
    }

    private fun buildReleaseYearQuestion(anime: Anime): TriviaQuestion {
        val baseYear = anime.releaseYear ?: 2015
        val options = listOf(
            baseYear,
            baseYear + 1,
            baseYear - 2,
            baseYear + 3
        ).map { it.coerceAtLeast(1990) }.distinct().take(4).map { it.toString() }
        val correctIndex = options.indexOf(baseYear.toString())
        return TriviaQuestion(
            id = "${anime.id}_release",
            animeId = anime.id,
            difficulty = TriviaDifficulty.MEDIUM,
            question = "¿En qué año se estrenó ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "El estreno original ocurrió en $baseYear, marcando su llegada a la TV japonesa"
        )
    }

    private fun buildEpisodesQuestion(anime: Anime): TriviaQuestion {
        val total = anime.totalEpisodes ?: 12
        val options = listOf(
            total,
            total + 10,
            max(1, total - 8),
            total + 4
        ).map { it.coerceAtLeast(1) }.distinct().take(4).map { "$it episodios" }
        val correctIndex = options.indexOf("$total episodios")
        return TriviaQuestion(
            id = "${anime.id}_episodes",
            animeId = anime.id,
            difficulty = TriviaDifficulty.MEDIUM,
            question = "¿Cuántos episodios tiene ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "Hasta la fecha cuenta con $total episodios publicados"
        )
    }

    private fun buildOriginalTitleQuestion(anime: Anime): TriviaQuestion {
        val originalTitle = anime.originalTitle ?: anime.title
        val otherTitles = FakeDataSource.animeCatalog
            .filter { it.id != anime.id }
            .mapNotNull { it.originalTitle }
            .take(3)
        val options = (listOf(originalTitle) + otherTitles).shuffled()
        val correctIndex = options.indexOf(originalTitle)
        return TriviaQuestion(
            id = "${anime.id}_title",
            animeId = anime.id,
            difficulty = TriviaDifficulty.MEDIUM,
            question = "¿Cuál es el título original de ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "En Japón se conoce como $originalTitle"
        )
    }

    private fun buildStatementQuestion(anime: Anime): TriviaQuestion {
        val baseYear = anime.releaseYear ?: 2015
        val mainGenre = anime.genres.firstOrNull()?.name ?: "acción"
        val statements = listOf(
            "${anime.title} mezcla el género $mainGenre con elementos históricos y se estrenó en $baseYear",
            "${anime.title} finalizó en 2010 y es recordado como una comedia romántica",
            "${anime.title} se caracteriza por episodios de menos de 10 minutos estrenados en 2022",
            "${anime.title} nunca se transmitió en TV y sólo existe como película"
        )
        val correctIndex = 0
        return TriviaQuestion(
            id = "${anime.id}_statement",
            animeId = anime.id,
            difficulty = TriviaDifficulty.HARD,
            question = "Selecciona la afirmación correcta sobre ${anime.title}",
            options = statements,
            correctAnswerIndex = correctIndex,
            feedback = "Su estreno en $baseYear consolidó a ${anime.title} dentro del género $mainGenre"
        )
    }

    private fun buildMissingGenreQuestion(anime: Anime): TriviaQuestion {
        val availableGenres = anime.genres.map { it.name }
        val extraGenre = FakeDataSource.genres.firstOrNull { it.name !in availableGenres }?.name ?: "Comedia"
        val options = (availableGenres + extraGenre).shuffled()
        val correctIndex = options.indexOf(extraGenre)
        return TriviaQuestion(
            id = "${anime.id}_missing_genre",
            animeId = anime.id,
            difficulty = TriviaDifficulty.HARD,
            question = "¿Cuál de estos géneros NO está asociado a ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "El género $extraGenre no forma parte de la mezcla principal de la serie"
        )
    }

    private fun buildBingeTimeQuestion(anime: Anime): TriviaQuestion {
        val totalEpisodes = anime.totalEpisodes ?: 12
        val minutesPerEpisode = anime.durationType.toAverageMinutes()
        val totalHours = ceil(totalEpisodes * minutesPerEpisode / 60.0).toInt()
        val options = listOf(
            totalHours,
            totalHours + 4,
            max(1, totalHours - 3),
            totalHours + 2
        ).distinct().map { "$it horas" }
        val correctIndex = options.indexOf("$totalHours horas")
        return TriviaQuestion(
            id = "${anime.id}_binge",
            animeId = anime.id,
            difficulty = TriviaDifficulty.HARD,
            question = "Si vieras todos los episodios seguidos, ¿cuántas horas aproximadas invertirías?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "Son alrededor de $totalHours horas de contenido contando los ${anime.totalEpisodes ?: ""} episodios"
        )
    }

    private fun DurationType.toReadableText(): String = when (this) {
        DurationType.SHORT -> "Corto (≤15 min)"
        DurationType.MEDIUM -> "Medio (16-25 min)"
        DurationType.LONG -> "Largo (30+ min)"
    }

    private fun DurationType.toAverageMinutes(): Int = when (this) {
        DurationType.SHORT -> 12
        DurationType.MEDIUM -> 23
        DurationType.LONG -> 35
    }

    private fun EmissionStatus.toReadableText(): String = when (this) {
        EmissionStatus.ON_AIR -> "En emisión"
        EmissionStatus.FINISHED -> "Finalizado"
        EmissionStatus.ON_BREAK -> "En pausa"
    }
}