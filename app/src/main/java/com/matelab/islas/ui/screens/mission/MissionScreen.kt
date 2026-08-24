package com.matelab.islas.ui.screens.mission

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matelab.islas.domain.model.Challenge
import com.matelab.islas.ui.art.IslandArt
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
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import com.matelab.islas.ui.theme.paletteFor

/**
 * Pantalla de mision: informe de Kubo, retos encadenados y parte final.
 */
@Composable
fun MissionScreen(
    missionId: String,
    onExit: () -> Unit
) {
    val viewModel: MissionViewModel = mateViewModel(key = missionId) { MissionViewModel(it, missionId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state.phase) {
        MissionPhase.CARGANDO -> LoadingBody()
        MissionPhase.INFORME -> BriefingBody(state, onStart = viewModel::startPlaying, onExit = onExit)
        MissionPhase.JUGANDO -> PlayingBody(
            state = state,
            onHint = viewModel::showHint,
            onSubmit = viewModel::submit,
            onNext = viewModel::next,
            onRetry = viewModel::retry,
            onExit = onExit
        )
        MissionPhase.RESULTADO -> ResultBody(
            state = state,
            onReplay = viewModel::replay,
            onExit = onExit
        )
    }
}

@Composable
private fun LoadingBody() {
    SeaBackdrop(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Kubo(mood = KuboMood.PENSANDO, size = 120.dp)
        }
    }
}

// ---------------------------------------------------------------- INFORME

@Composable
private fun BriefingBody(
    state: MissionUiState,
    onStart: () -> Unit,
    onExit: () -> Unit
) {
    val feedback = rememberUiFeedback()
    val mission = state.mission
    val palette = state.world?.theme?.let { paletteFor(it) }

    SeaBackdrop(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            state.world?.let { IslandArt(theme = it.theme, size = 150.dp) }
            Spacer(Modifier.height(10.dp))
            Text(
                mission?.name ?: "Mision",
                style = MaterialTheme.typography.displayMedium,
                color = MateTheme.colors.ink
            )
            Spacer(Modifier.height(6.dp))
            Text(
                mission?.goal ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MateTheme.colors.inkSoft
            )
            Spacer(Modifier.height(18.dp))
            KuboSays(text = mission?.briefing ?: "", mood = KuboMood.ANIMANDO)
            Spacer(Modifier.height(18.dp))
            MatePanel(contentPadding = 12.dp) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Info("${state.total}", "retos")
                    Info(mission?.difficulty?.name?.lowercase() ?: "-", "nivel")
                    Info("~${state.total + 2}", "minutos")
                }
            }
            Spacer(Modifier.height(20.dp))
            MateButton(
                text = "Empezar",
                onClick = {
                    feedback.tap()
                    onStart()
                },
                color = palette?.primary ?: Teal,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            GhostButton(text = "Ahora no", onClick = onExit, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Info(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = MateTheme.colors.ink)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MateTheme.colors.inkSoft)
    }
}

// ---------------------------------------------------------------- JUGANDO

@Composable
private fun PlayingBody(
    state: MissionUiState,
    onHint: () -> Unit,
    onSubmit: (Boolean) -> Unit,
    onNext: () -> Unit,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    val feedback = rememberUiFeedback()
    val scroll = rememberScrollState()
    val challenge = state.current ?: return
    val palette = state.world?.theme?.let { paletteFor(it) }

    LaunchedEffect(state.answered, state.index) {
        if (state.answered) {
            if (state.lastCorrect) feedback.correct() else feedback.wrong()
        }
    }

    SeaBackdrop(Modifier.fillMaxSize(), showHorizon = false) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp)
                .padding(top = 14.dp, bottom = 26.dp)
        ) {

            // Cabecera con progreso por puntos
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MateTheme.colors.card)
                        .clickable {
                            feedback.tap()
                            onExit()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Salir", tint = MateTheme.colors.ink)
                }
                Spacer(Modifier.width(10.dp))
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    repeat(state.total) { i ->
                        Box(
                            Modifier
                                .weight(1f)
                                .height(9.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    when {
                                        i < state.index -> palette?.primary ?: Teal
                                        i == state.index -> Sun
                                        else -> MateTheme.colors.outline
                                    }
                                )
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (state.hintVisible) Sun.copy(alpha = 0.3f) else MateTheme.colors.card)
                        .clickable {
                            feedback.tap()
                            onHint()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Lightbulb, contentDescription = "Pista", tint = Sun)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Enunciado
            MatePanel(contentPadding = 14.dp) {
                Column {
                    Text(
                        "${activityLabel(challenge).uppercase()}  -  RETO ${state.index + 1} DE ${state.total}",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette?.dark ?: Teal
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        challenge.prompt,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MateTheme.colors.ink
                    )
                }
            }

            AnimatedVisibility(visible = state.hintVisible, enter = fadeIn(), exit = fadeOut()) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Sun.copy(alpha = 0.18f))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = Sun)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                challenge.hint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MateTheme.colors.ink
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            ChallengeGame(
                challenge = challenge,
                enabled = !state.answered,
                onSubmit = onSubmit,
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = state.answered,
                enter = fadeIn() + slideInVertically { it / 3 }
            ) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    FeedbackPanel(
                        correct = state.lastCorrect,
                        challenge = challenge,
                        isLast = state.isLast,
                        onNext = {
                            feedback.tap()
                            onNext()
                        },
                        onRetry = {
                            feedback.tap()
                            onRetry()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Feedback educativo: nunca se limita a "correcto" o "incorrecto".
 * Siempre explica el porque y ofrece reintentar sin castigo.
 */
@Composable
private fun FeedbackPanel(
    correct: Boolean,
    challenge: Challenge,
    isLast: Boolean,
    onNext: () -> Unit,
    onRetry: () -> Unit
) {
    val accent = if (correct) MateTheme.colors.success else MateTheme.colors.warning
    MatePanel(contentPadding = 16.dp, color = accent.copy(alpha = 0.10f)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Kubo(
                    mood = if (correct) KuboMood.CELEBRANDO else KuboMood.PENSANDO,
                    size = 66.dp
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        if (correct) "Bien resuelto" else "Casi lo tienes",
                        style = MaterialTheme.typography.headlineSmall,
                        color = accent
                    )
                    Text(
                        if (correct) "Kubo apunta el resultado en su cuaderno."
                        else "Mira la explicacion y vuelve a intentarlo.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MateTheme.colors.inkSoft
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                challenge.explanation,
                style = MaterialTheme.typography.bodyLarge,
                color = MateTheme.colors.ink
            )
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth()) {
                if (!correct) {
                    GhostButton(
                        text = "Reintentar",
                        onClick = onRetry,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(10.dp))
                }
                MateButton(
                    text = if (isLast) "Terminar mision" else "Siguiente reto",
                    onClick = onNext,
                    color = accent,
                    modifier = Modifier.weight(1.4f)
                )
            }
        }
    }
}

// -------------------------------------------------------------- RESULTADO

@Composable
private fun ResultBody(
    state: MissionUiState,
    onReplay: () -> Unit,
    onExit: () -> Unit
) {
    val outcome = state.outcome
    val feedback = rememberUiFeedback()
    val scroll = rememberScrollState()

    LaunchedEffect(outcome?.missionId, outcome?.stars) {
        if (outcome != null) {
            if (outcome.stars >= 2) feedback.star() else feedback.tap()
            if (outcome.newCollectibles.isNotEmpty() || outcome.newBadges.isNotEmpty()) feedback.unlock()
            if (outcome.leveledUpTo != null) feedback.levelUp()
        }
    }

    SeaBackdrop(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(10.dp))
            Kubo(
                mood = if ((outcome?.stars ?: 0) >= 2) KuboMood.CELEBRANDO else KuboMood.ANIMANDO,
                size = 130.dp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Mision terminada",
                style = MaterialTheme.typography.displayMedium,
                color = MateTheme.colors.ink
            )
            Spacer(Modifier.height(12.dp))

            AnimatedStars(stars = outcome?.stars ?: 0)

            Spacer(Modifier.height(16.dp))

            MatePanel(contentPadding = 16.dp) {
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Info("${outcome?.correct ?: 0}/${outcome?.total ?: 0}", "aciertos")
                        Info("+${outcome?.xpEarned ?: 0}", "XP")
                        Info("${outcome?.hintsUsed ?: 0}", "pistas")
                    }
                }
            }

            outcome?.leveledUpTo?.let { level ->
                Spacer(Modifier.height(12.dp))
                RewardBanner(
                    title = "Nivel $level alcanzado",
                    subtitle = "Kubo mejora los motores de la nave",
                    color = Sun
                )
            }

            if (outcome != null && outcome.newCollectibles.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                outcome.newCollectibles.forEach { crystal ->
                    RewardBanner(
                        title = "Nuevo cristal: ${crystal.name}",
                        subtitle = crystal.fact,
                        color = Teal
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (outcome != null && outcome.newBadges.isNotEmpty()) {
                outcome.newBadges.forEach { badge ->
                    RewardBanner(
                        title = "Insignia: ${badge.name}",
                        subtitle = badge.description,
                        color = MateTheme.colors.success
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (outcome != null && outcome.unlockedWorlds.isNotEmpty()) {
                outcome.unlockedWorlds.forEach { world ->
                    RewardBanner(
                        title = "Isla nueva: ${world.name}",
                        subtitle = world.subtitle,
                        color = MateTheme.colors.warning
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(18.dp))

            MateButton(
                text = "Volver al mapa",
                onClick = {
                    feedback.tap()
                    onExit()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            GhostButton(
                text = "Repetir mision",
                onClick = {
                    feedback.tap()
                    onReplay()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AnimatedStars(stars: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(3) { index ->
            val filled = index < stars
            Box(
                Modifier
                    .size(if (filled) 66.dp else 54.dp)
                    .clip(CircleShape)
                    .background(if (filled) Sun.copy(alpha = 0.25f) else MateTheme.colors.outline.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                com.matelab.islas.ui.components.StarRow(
                    stars = if (filled) 1 else 0,
                    max = 1,
                    size = if (filled) 40.dp else 30.dp
                )
            }
        }
    }
}

@Composable
private fun RewardBanner(title: String, subtitle: String, color: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(14.dp)
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MateTheme.colors.ink)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MateTheme.colors.inkSoft)
        }
    }
}
