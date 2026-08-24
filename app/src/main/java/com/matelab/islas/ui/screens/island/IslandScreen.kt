package com.matelab.islas.ui.screens.island

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matelab.islas.domain.model.MissionStatus
import com.matelab.islas.ui.art.IslandArt
import com.matelab.islas.ui.art.SeaBackdrop
import com.matelab.islas.ui.components.KuboSays
import com.matelab.islas.ui.components.MatePanel
import com.matelab.islas.ui.components.StarRow
import com.matelab.islas.ui.components.StatusChip
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.navigation.mateViewModel
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.paletteFor

/**
 * Sendero de misiones de una isla.
 * Cada nodo muestra su estado con icono, texto y estrellas, nunca solo color.
 */
@Composable
fun IslandScreen(
    worldId: String,
    onBack: () -> Unit,
    onOpenMission: (String) -> Unit
) {
    val viewModel: IslandViewModel = mateViewModel(key = worldId) { IslandViewModel(it, worldId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val feedback = rememberUiFeedback()
    val scroll = rememberScrollState()
    val world = state.world

    SeaBackdrop(Modifier.fillMaxSize(), showHorizon = false) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MateTheme.colors.card)
                        .clickable {
                            feedback.tap()
                            onBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Volver al mapa",
                        tint = MateTheme.colors.ink
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    world?.name ?: "Isla",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MateTheme.colors.ink
                )
            }

            Spacer(Modifier.height(12.dp))

            if (world != null) {
                val palette = paletteFor(world.theme)
                MatePanel(contentPadding = 14.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IslandArt(theme = world.theme, size = 108.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                world.subtitle,
                                style = MaterialTheme.typography.titleLarge,
                                color = palette.dark
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                world.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MateTheme.colors.inkSoft
                            )
                            Spacer(Modifier.height(10.dp))
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(palette.primary.copy(alpha = 0.20f))
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(state.percent / 100f)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(palette.primary)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Isla al ${state.percent} %",
                                style = MaterialTheme.typography.labelSmall,
                                color = MateTheme.colors.inkSoft
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                state.missions.forEachIndexed { index, row ->
                    MissionNode(
                        index = index + 1,
                        row = row,
                        color = palette.primary,
                        onClick = {
                            if (row.status == MissionStatus.BLOQUEADA) {
                                feedback.wrong()
                            } else {
                                feedback.tap()
                                onOpenMission(row.mission.id)
                            }
                        }
                    )
                    if (index < state.missions.lastIndex) {
                        Canvas(
                            Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                        ) {
                            drawLine(
                                color = palette.primary.copy(alpha = 0.5f),
                                start = Offset(size.width * 0.13f, 0f),
                                end = Offset(size.width * 0.13f, size.height),
                                strokeWidth = 7f,
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 14f))
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                KuboSays(
                    text = "Cada mision son unos cinco minutos. Puedes salir cuando quieras: " +
                        "tu progreso se guarda solo."
                )
            }
        }
    }
}

@Composable
private fun MissionNode(
    index: Int,
    row: MissionRow,
    color: Color,
    onClick: () -> Unit
) {
    val locked = row.status == MissionStatus.BLOQUEADA
    MatePanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentPadding = 14.dp,
        color = if (locked) MateTheme.colors.cardAlt else MateTheme.colors.card
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (locked) MateTheme.colors.locked else color),
                contentAlignment = Alignment.Center
            ) {
                if (locked) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color.White)
                } else {
                    Text(
                        index.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    row.mission.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (locked) MateTheme.colors.inkSoft else MateTheme.colors.ink
                )
                Text(
                    row.mission.goal,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MateTheme.colors.inkSoft
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusChip(row.status)
                    StarRow(row.stars, size = 18.dp)
                }
                if (locked && row.blockedBy.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Antes: ${row.blockedBy.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MateTheme.colors.inkSoft
                    )
                }
                if (!locked && row.timesPlayed > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Mejor resultado: ${row.bestPercent} %",
                        style = MaterialTheme.typography.labelSmall,
                        color = MateTheme.colors.inkSoft
                    )
                }
            }
        }
    }
}
