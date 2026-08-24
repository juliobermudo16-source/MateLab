package com.matelab.islas.ui.screens.mission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matelab.islas.core.di.AppContainer
import com.matelab.islas.domain.model.Challenge
import com.matelab.islas.domain.model.ChallengeResult
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.MissionOutcome
import com.matelab.islas.domain.model.World
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MissionPhase { CARGANDO, INFORME, JUGANDO, RESULTADO }

data class MissionUiState(
    val phase: MissionPhase = MissionPhase.CARGANDO,
    val mission: Mission? = null,
    val world: World? = null,
    val challenges: List<Challenge> = emptyList(),
    val index: Int = 0,
    val answered: Boolean = false,
    val lastCorrect: Boolean = false,
    val hintVisible: Boolean = false,
    val hintsUsed: Int = 0,
    val correctCount: Int = 0,
    val outcome: MissionOutcome? = null
) {
    val current: Challenge? get() = challenges.getOrNull(index)
    val total: Int get() = challenges.size
    val isLast: Boolean get() = index >= challenges.lastIndex
}

/**
 * Motor de una sesion de mision.
 *
 * Lleva la cuenta de aciertos, pistas y tiempo, y al final delega en el
 * repositorio para guardar el progreso y calcular recompensas reales.
 */
class MissionViewModel(
    container: AppContainer,
    private val missionId: String
) : ViewModel() {

    private val catalog = container.catalogRepository
    private val progress = container.progressRepository

    private val _state = MutableStateFlow(MissionUiState())
    val state: StateFlow<MissionUiState> = _state.asStateFlow()

    private val results = mutableListOf<ChallengeResult>()
    private var challengeStartedAt = System.currentTimeMillis()

    init {
        viewModelScope.launch {
            val mission = catalog.mission(missionId)
            val challenges = catalog.challengesOf(missionId)
            val world = catalog.worlds().firstOrNull { it.id == mission?.worldId }
            _state.update {
                it.copy(
                    phase = if (mission == null || challenges.isEmpty()) {
                        MissionPhase.RESULTADO
                    } else {
                        MissionPhase.INFORME
                    },
                    mission = mission,
                    world = world,
                    challenges = challenges
                )
            }
        }
    }

    fun startPlaying() {
        challengeStartedAt = System.currentTimeMillis()
        _state.update { it.copy(phase = MissionPhase.JUGANDO) }
    }

    fun showHint() {
        _state.update {
            if (it.hintVisible) it else it.copy(hintVisible = true, hintsUsed = it.hintsUsed + 1)
        }
    }

    /** Registra la respuesta del mini-juego actual. */
    fun submit(correct: Boolean) {
        val current = _state.value.current ?: return
        if (_state.value.answered) return
        val elapsed = System.currentTimeMillis() - challengeStartedAt
        results += ChallengeResult(
            challengeId = current.id,
            correct = correct,
            usedHint = _state.value.hintVisible,
            elapsedMs = elapsed
        )
        _state.update {
            it.copy(
                answered = true,
                lastCorrect = correct,
                correctCount = it.correctCount + if (correct) 1 else 0
            )
        }
    }

    /** Pasa al siguiente reto o cierra la mision. */
    fun next() {
        val state = _state.value
        if (!state.answered) return
        if (state.isLast) {
            finish()
        } else {
            challengeStartedAt = System.currentTimeMillis()
            _state.update {
                it.copy(index = it.index + 1, answered = false, hintVisible = false)
            }
        }
    }

    /** Permite reintentar el mismo reto sin puntuar dos veces. */
    fun retry() {
        val state = _state.value
        if (!state.answered || state.lastCorrect) return
        _state.update { it.copy(answered = false) }
        results.removeAll { it.challengeId == state.current?.id }
        challengeStartedAt = System.currentTimeMillis()
    }

    private fun finish() {
        viewModelScope.launch {
            val outcome = progress.finishMission(missionId, results.toList())
            _state.update { it.copy(phase = MissionPhase.RESULTADO, outcome = outcome) }
        }
    }

    /** Reinicia la mision para volver a jugarla de cero. */
    fun replay() {
        results.clear()
        challengeStartedAt = System.currentTimeMillis()
        _state.update {
            it.copy(
                phase = MissionPhase.JUGANDO,
                index = 0,
                answered = false,
                hintVisible = false,
                hintsUsed = 0,
                correctCount = 0,
                outcome = null
            )
        }
    }
}
