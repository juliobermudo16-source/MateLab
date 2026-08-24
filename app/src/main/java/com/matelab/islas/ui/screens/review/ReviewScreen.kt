package com.matelab.islas.ui.screens.review

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matelab.islas.core.di.AppContainer
import com.matelab.islas.domain.engine.ReviewEngine
import com.matelab.islas.domain.model.Challenge
import com.matelab.islas.domain.model.ChallengeResult
import com.matelab.islas.ui.art.Kubo
import com.matelab.islas.ui.art.KuboMood
import com.matelab.islas.ui.art.SeaBackdrop
import com.matelab.islas.ui.components.GhostButton
import com.matelab.islas.ui.components.KuboSays
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.MatePanel
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.games.ChallengeGame
import com.matelab.islas.ui.games.activityLabel
import com.matelab.islas.ui.navigation.mateViewModel
import com.matelab.islas.ui.screens.collection.Header
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sun
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ReviewPhase { LISTA, JUGANDO, FIN }

data class ReviewUiState(
    val phase: ReviewPhase = ReviewPhase.LISTA,
    val pending: Int = 0,
    val session: List<Challenge> = emptyList(),
    val index: Int = 0,
    val answered: Boolean = false,
    val lastCorrect: Boolean = false,
    val correct: Int = 0,
    val cleared: Boolean = false
) {
    val current: Challenge? get() = session.getOrNull(index)
}

/**
 * Taller de repaso: recupera los retos que se fallaron y los vuelve a proponer.
 */
class ReviewViewModel(container: AppContainer) : ViewModel() {

    private val catalog = container.catalogRepository
    private val progress = container.progressRepository

    private val _state = MutableStateFlow(ReviewUiState())
    val state: StateFlow<ReviewUiState> = _state

    private val results = mutableListOf<ChallengeResult>()
    private var startedAt = System.currentTimeMillis()

    init {
        viewModelScope.launch {
            progress.observePendingReviewCount().collect { count ->
                _state.update { it.copy(pending = count) }
            }
        }
    }

    fun start() {
        viewModelScope.launch {
            val items = progress.reviewItems()
            val challenges = catalog.challengesByIds(
                items.filter { !it.resolved }.map { it.challengeId }
            )
            val session = ReviewEngine.buildSession(items, challenges)
            results.clear()
            startedAt = System.currentTimeMillis()
            _state.update {
                it.copy(
                    phase = if (session.isEmpty()) ReviewPhase.LISTA else ReviewPhase.JUGANDO,
                    session = session,
                    index = 0,
                    answered = false,
                    correct = 0,
                    cleared = false
                )
            }
        }
    }

    fun submit(correct: Boolean) {
        val current = _state.value.current ?: return
        if (_state.value.answered) return
        results += ChallengeResult(
            challengeId = current.id,
            correct = correct,
            usedHint = false,
            elapsedMs = System.currentTimeMillis() - startedAt
        )
        _state.update {
            it.copy(
                answered = true,
                lastCorrect = correct,
                correct = it.correct + if (correct) 1 else 0
            )
        }
    }

    fun next() {
        val state = _state.value
        if (!state.answered) return
        if (state.index >= state.session.lastIndex) {
            viewModelScope.launch {
                val cleared = progress.finishReview(results.toList())
                _state.update { it.copy(phase = ReviewPhase.FIN, cleared = cleared) }
            }
        } else {
            startedAt = System.currentTimeMillis()
            _state.update { it.copy(index = it.index + 1, answered = false) }
        }
    }

    fun backToList() {
        _state.update { it.copy(phase = ReviewPhase.LISTA, session = emptyList()) }
    }
}

@Composable
fun ReviewScreen(onBack: () -> Unit) {
    val viewModel: ReviewViewModel = mateViewModel { ReviewViewModel(it) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val feedback = rememberUiFeedback()
    val scroll = rememberScrollState()

    LaunchedEffect(state.answered) {
        if (state.answered) {
            if (state.lastCorrect) feedback.correct() else feedback.wrong()
        }
    }

    SeaBackdrop(Modifier.fillMaxSize(), showHorizon = false) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp)
        ) {
            Header(title = "Taller de repaso", onBack = onBack)
            Spacer(Modifier.height(12.dp))

            when (state.phase) {
                ReviewPhase.LISTA -> {
                    Kubo(
                        mood = if (state.pending == 0) KuboMood.FELIZ else KuboMood.PENSANDO,
                        size = 120.dp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(10.dp))
                    MatePanel(contentPadding = 16.dp) {
                        Column {
                            Text(
                                ReviewEngine.statusMessage(state.pending),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MateTheme.colors.ink
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "En el taller se reparan los retos que se te resistieron. " +
                                    "No hay nota ni castigo: solo otra oportunidad.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MateTheme.colors.inkSoft
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    MateButton(
                        text = "Empezar repaso",
                        onClick = {
                            feedback.tap()
                            viewModel.start()
                        },
                        enabled = state.pending > 0,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                ReviewPhase.JUGANDO -> {
                    val challenge = state.current
                    if (challenge != null) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            repeat(state.session.size) { i ->
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(9.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            when {
                                                i < state.index -> MateTheme.colors.success
                                                i == state.index -> Sun
                                                else -> MateTheme.colors.outline
                                            }
                                        )
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        MatePanel(contentPadding = 14.dp) {
                            Column {
                                Text(
                                    activityLabel(challenge).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MateTheme.colors.inkSoft
                                )
                                Text(
                                    challenge.prompt,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MateTheme.colors.ink
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        ChallengeGame(
                            challenge = challenge,
                            enabled = !state.answered,
                            onSubmit = viewModel::submit,
                            modifier = Modifier.fillMaxWidth()
                        )
                        AnimatedVisibility(visible = state.answered, enter = fadeIn()) {
                            Column {
                                Spacer(Modifier.height(14.dp))
                                MatePanel(
                                    contentPadding = 14.dp,
                                    color = (if (state.lastCorrect) MateTheme.colors.success
                                    else MateTheme.colors.warning).copy(alpha = 0.12f)
                                ) {
                                    Column {
                                        Text(
                                            if (state.lastCorrect) "Reparado" else "Sigue en el taller",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = if (state.lastCorrect) MateTheme.colors.success
                                            else MateTheme.colors.warning
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            challenge.explanation,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MateTheme.colors.ink
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        MateButton(
                                            text = if (state.index >= state.session.lastIndex) {
                                                "Terminar repaso"
                                            } else {
                                                "Siguiente"
                                            },
                                            onClick = {
                                                feedback.tap()
                                                viewModel.next()
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                ReviewPhase.FIN -> {
                    Kubo(
                        mood = if (state.cleared) KuboMood.CELEBRANDO else KuboMood.ANIMANDO,
                        size = 130.dp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (state.cleared) "Taller reluciente" else "Buen trabajo",
                        style = MaterialTheme.typography.displayMedium,
                        color = MateTheme.colors.ink
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Has acertado ${state.correct} de ${state.session.size} retos del repaso.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MateTheme.colors.inkSoft
                    )
                    Spacer(Modifier.height(16.dp))
                    MateButton(
                        text = "Volver al taller",
                        onClick = {
                            feedback.tap()
                            viewModel.backToList()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    GhostButton(
                        text = "Ir al mapa",
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (state.phase == ReviewPhase.LISTA) {
                Spacer(Modifier.height(16.dp))
                KuboSays("Un reto sale del taller cuando lo aciertas.")
            }
        }
    }
}
