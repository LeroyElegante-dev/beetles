package com.example.beetles.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.beetles.data.CurrentPlayerStore
import com.example.beetles.data.GameSettingsStore
import com.example.beetles.data.GoldRateRepository
import com.example.beetles.data.GoldRateStore
import com.example.beetles.db.BeetlesDatabase
import com.example.beetles.db.ScoreEntity
import com.example.beetles.model.GameSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameUiState(
    val score: Int = 0,
    val hits: Int = 0,
    val misses: Int = 0,
    val playing: Boolean = false,
    val secondsLeft: Int = 0,
    val settings: GameSettings = GameSettings.DEFAULT,
    val goldPoints: Int = 80,
    val currentPlayerName: String? = null
) {
    val accuracyPercent: Int
        get() {
            val shots = hits + misses
            if (shots == 0) return 0
            return hits * 100 / shots
        }
}

sealed interface GameEvent {
    data object NeedPlayer : GameEvent
    data object BonusActivated : GameEvent
    data class RoundFinished(val score: Int, val accuracy: Int) : GameEvent
    data object ScoreSaved : GameEvent
}

class GameViewModel(
    private val settingsStore: GameSettingsStore,
    private val currentPlayerStore: CurrentPlayerStore,
    private val goldRateStore: GoldRateStore,
    private val goldRateRepository: GoldRateRepository,
    private val database: BeetlesDatabase
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var timerJob: Job? = null

    init {
        _state.update {
            it.copy(secondsLeft = settingsStore.load().roundDurationSeconds)
        }
        refreshCurrentPlayer()
        refreshGoldRate()
    }

    fun hasPlayer(): Boolean = currentPlayerStore.hasPlayer()

    fun refreshCurrentPlayer() {
        viewModelScope.launch {
            val id = currentPlayerStore.getPlayerId()
            val name = if (id == CurrentPlayerStore.NO_PLAYER) {
                null
            } else {
                database.playerDao().getById(id)?.fullName
            }
            _state.update { it.copy(currentPlayerName = name) }
        }
    }

    fun refreshGoldRate() {
        viewModelScope.launch {
            goldRateRepository.refreshOrCached()
        }
    }

    fun startRound() {
        if (!currentPlayerStore.hasPlayer()) {
            _events.tryEmit(GameEvent.NeedPlayer)
            return
        }
        val settings = settingsStore.load()
        timerJob?.cancel()
        _state.value = GameUiState(
            score = 0,
            hits = 0,
            misses = 0,
            playing = true,
            secondsLeft = settings.roundDurationSeconds,
            settings = settings,
            goldPoints = goldRateStore.pointsForHit(),
            currentPlayerName = _state.value.currentPlayerName
        )
        startTimer(settings.roundDurationSeconds)
    }

    fun onHit(points: Int) {
        if (!_state.value.playing) return
        _state.update {
            it.copy(
                score = it.score + points,
                hits = it.hits + 1
            )
        }
    }

    fun onMiss() {
        if (!_state.value.playing) return
        _state.update {
            it.copy(
                score = (it.score - MISS_PENALTY).coerceAtLeast(0),
                misses = it.misses + 1
            )
        }
    }

    fun onBonusCollected() {
        if (!_state.value.playing) return
        _events.tryEmit(GameEvent.BonusActivated)
    }

    fun abortRound() {
        if (!_state.value.playing) return
        finishRound(save = false)
    }

    private fun startTimer(totalSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var left = totalSeconds
            while (left > 0) {
                _state.update { it.copy(secondsLeft = left) }
                delay(1000L)
                if (!_state.value.playing) return@launch
                left--
            }
            if (_state.value.playing) {
                finishRound(save = true)
            }
        }
    }

    private fun finishRound(save: Boolean) {
        timerJob?.cancel()
        timerJob = null
        val finished = _state.value
        _state.update {
            it.copy(
                playing = false,
                secondsLeft = it.settings.roundDurationSeconds
            )
        }
        if (save) {
            _events.tryEmit(
                GameEvent.RoundFinished(
                    score = finished.score,
                    accuracy = finished.accuracyPercent
                )
            )
            saveScore(finished)
        }
    }

    private fun saveScore(finished: GameUiState) {
        val playerId = currentPlayerStore.getPlayerId()
        if (playerId == CurrentPlayerStore.NO_PLAYER) return
        viewModelScope.launch {
            val player = database.playerDao().getById(playerId) ?: return@launch
            database.scoreDao().insert(
                ScoreEntity(
                    playerId = player.id,
                    playerName = player.fullName,
                    score = finished.score,
                    difficulty = player.difficulty,
                    hits = finished.hits,
                    misses = finished.misses,
                    accuracy = finished.accuracyPercent,
                    createdAt = System.currentTimeMillis()
                )
            )
            _events.tryEmit(GameEvent.ScoreSaved)
        }
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    private companion object {
        const val MISS_PENALTY = 5
    }
}
