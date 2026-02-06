package com.kappa.app.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.core.network.ApiService
import com.kappa.app.core.network.model.toDomain
import com.kappa.app.domain.home.MiniGame
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameHubState(
    val games: List<MiniGame> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class GameHubViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(GameHubState())
    val state: StateFlow<GameHubState> = _state.asStateFlow()

    init {
        loadGames()
    }

    private fun loadGames() {
        viewModelScope.launch {
            val response = runCatching { apiService.getPopularMiniGames() }.getOrNull()
            if (response == null || !response.success || response.data == null) {
                _state.value = _state.value.copy(error = response?.error ?: "Failed to load games")
            } else {
                _state.value = GameHubState(games = response.data.map { it.toDomain() })
            }
        }
    }
}
