package com.example.animedev.feature.animeinfo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev.data.repository.FakeAnimeRepositoryImpl
import com.example.animedev.domain.usecase.GetAnimeDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimeDetailViewModel(
    private val animeId: Long,
    private val getAnimeDetailUseCase: GetAnimeDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnimeDetailUiState>(AnimeDetailUiState.Loading)
    val uiState: StateFlow<AnimeDetailUiState> = _uiState.asStateFlow()

    init {
        loadAnimeDetail()
    }

    fun loadAnimeDetail() {
        viewModelScope.launch {
            _uiState.value = AnimeDetailUiState.Loading
            val result = getAnimeDetailUseCase(animeId)
            result.fold(
                onSuccess = { detail ->
                    _uiState.value = AnimeDetailUiState.Success(detail)
                },
                onFailure = { throwable ->
                    _uiState.value = AnimeDetailUiState.Error(
                        throwable.message ?: "No pudimos cargar la información del anime"
                    )
                }
            )
        }
    }

    companion object {
        fun provideFactory(animeId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = FakeAnimeRepositoryImpl()
                    val useCase = GetAnimeDetailUseCase(repository)
                    return AnimeDetailViewModel(animeId, useCase) as T
                }
            }
    }
}