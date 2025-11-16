package com.example.animedev.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.animedev.data.repository.FakeAnimeRepositoryImpl
import com.example.animedev.data.repository.FakeUserRepositoryImpl
import com.example.animedev.domain.usecase.GetHomeContentUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getHomeContentUseCase: GetHomeContentUseCase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    init {
        loadHomeContent()
    }

    fun loadHomeContent() {
        _uiState.value = HomeUiState.Loading
        scope.launch {
            val result = getHomeContentUseCase()
            result.fold(
                onSuccess = { homeContent ->
                    _uiState.value = HomeUiState.Success(homeContent)
                },
                onFailure = { throwable ->
                    _uiState.value = HomeUiState.Error(
                        throwable.message ?: "Ha ocurrido un error inesperado"
                    )
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    val animeRepository = FakeAnimeRepositoryImpl()
                    val userRepository = FakeUserRepositoryImpl()
                    val useCase = GetHomeContentUseCase(animeRepository, userRepository)
                    return HomeViewModel(useCase) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
