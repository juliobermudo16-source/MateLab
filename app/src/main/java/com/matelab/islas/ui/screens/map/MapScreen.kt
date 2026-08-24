package com.matelab.islas.ui.screens.map

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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matelab.islas.domain.model.MissionStatus
import com.matelab.islas.ui.art.AvatarArt
import com.matelab.islas.ui.art.IslandArt
import com.matelab.islas.ui.art.Kubo
import com.matelab.islas.ui.art.KuboMood
import com.matelab.islas.ui.art.SeaBackdrop
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.MatePanel
import com.matelab.islas.ui.components.StarRow
import com.matelab.islas.ui.components.StatusChip
import com.matelab.islas.ui.components.XpBar
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.navigation.mateViewModel
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import com.matelab.islas.ui.theme.paletteFor

/**
 * Mapa del archipielago: centro de la experiencia.
 *
 * No es una lista de botones: es un mar con islas que emergen segun avanza
 * el nino, con Kubo proponiendo la siguiente mision.
 */
@Composable
fun MapScreen(
    onOpenIsland: (String) -> Unit,
    onOpenMission: (String) -> Unit,
    onOpenCollection: () -> Unit,
    onOpenBadges: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val viewModel: MapViewModel = mateViewModel { MapViewModel(it) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val feedback = rememberUiFeedback()
    val scroll = rememberScrollState()

    SeaBackdrop(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp)
                .padding(top = 18.dp, bottom = 28.dp)
        ) {

            // ------------------------------------------------------------ HUD
            MatePanel(contentPadding = 14.dp) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarArt(avatarId = state.profile.avatarId, size = 52.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                state.profile.alias.ifBlank { "Explorador" },
                                style = MaterialTheme.typography.headlineSmall,
                                color = MateTheme.colors.ink
                            )
                            Text(
                                "Racha de ${state.profile.streakDays} " +
                                    if (state.profile.streakDays == 1) "dia" else "dias",
                                style = MaterialTheme.typography.labelSmall,
                                color = MateTheme.colors.inkSoft
                            )
                        }
                        IconChip(Icons.Rounded.Settings, "Ajustes") {
                            feedback.tap()
                            onOpenSettings()
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    XpBar(xp = state.profile.xp)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatPill(Icons.Rounded.Diamond, "${state.profile.crystals}", "cristales", Teal)
                        StatPill(Icons.Rounded.AutoAwesome, "${state.totalStars}", "estrellas", Sun)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ------------------------------------------------- siguiente paso
            val next = state.next
            if (next != null) {
                MatePanel(contentPadding = 14.dp) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Kubo(mood = KuboMood.ANIMANDO, size = 78.dp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    next.world.name.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = paletteFor(next.world.theme).dark
                                )
                                Text(
                                    next.mission.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MateTheme.colors.ink
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    next.mission.goal,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MateTheme.colors.inkSoft
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StarRow(next.stars)
                                    Spacer(Modifier.width(8.dp))
                                    StatusChip(next.status)
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        MateButton(
                            text = if (next.status == MissionStatus.DISPONIBLE) "Empezar mision" else "Continuar",
                            onClick = {
                                feedback.tap()
                                onOpenMission(next.mission.id)
                            },
                            color = paletteFor(next.world.theme).primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            // ----------------------------------------------------- las islas
            Text(
                "Archipielago",
                style = MaterialTheme.typography.headlineMedium,
                color = MateTheme.colors.ink
            )
            Text(
                "Toca una isla para ver sus misiones",
                style = MaterialTheme.typography.bodyMedium,
                color = MateTheme.colors.inkSoft
            )
            Spacer(Modifier.height(8.dp))

            state.worlds.forEachIndexed { index, card ->
                IslandRow(
                    card = card,
                    alignRight = index % 2 == 1,
                    showTrail = index < state.worlds.lastIndex,
                    onClick = {
                        if (card.unlocked) {
                            feedback.tap()
                            onOpenIsland(card.world.id)
                        } else {
                            feedback.wrong()
                        }
                    }
                )
            }

            Spacer(Modifier.height(18.dp))

            // ------------------------------------------------- accesos rapidos
            Text(
                "Cabina de Kubo",
                style = MaterialTheme.typography.headlineMedium,
                color = MateTheme.colors.ink
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickAccess(
                    icon = Icons.Rounded.Diamond,
                    title = "Cristales",
                    subtitle = "${state.profile.crystals}",
                    color = Teal,
                    modifier = Modifier.weight(1f),
                    onClick = { feedback.tap(); onOpenCollection() }
                )
                QuickAccess(
                    icon = Icons.Rounded.EmojiEvents,
                    title = "Insignias",
                    subtitle = "Logros",
                    color = Sun,
                    modifier = Modifier.weight(1f),
                    onClick = { feedback.tap(); onOpenBadges() }
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickAccess(
                    icon = Icons.Rounded.Insights,
                    title = "Progreso",
                    subtitle = "Tus datos",
                    color = MateTheme.colors.success,
                    modifier = Modifier.weight(1f),
                    onClick = { feedback.tap(); onOpenStats() }
                )
                QuickAccess(
                    icon = Icons.Rounded.Build,
                    title = "Taller",
                    subtitle = if (state.pendingReview > 0) "${state.pendingReview} por repasar" else "Todo listo",
                    color = MateTheme.colors.warning,
                    modifier = Modifier.weight(1f),
                    onClick = { feedback.tap(); onOpenReview() }
                )
            }
        }
    }
}

@Composable
private fun IslandRow(
    card: WorldCard,
    alignRight: Boolean,
    showTrail: Boolean,
    onClick: () -> Unit
) {
    val palette = paletteFor(card.world.theme)
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (alignRight) Spacer(Modifier.width(12.dp))
            if (!alignRight) {
                IslandArt(theme = card.world.theme, size = 116.dp, locked = !card.unlocked)
                Spacer(Modifier.width(10.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    card.world.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (card.unlocked) MateTheme.colors.ink else MateTheme.colors.inkSoft
                )
                Text(
                    card.world.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MateTheme.colors.inkSoft
                )
                Spacer(Modifier.height(8.dp))
                if (card.unlocked) {
                    ProgressLine(percent = card.percent, color = palette.primary)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${card.missionsDone} de ${card.missionsTotal} misiones - ${card.percent} %",
                        style = MaterialTheme.typography.labelSmall,
                        color = MateTheme.colors.inkSoft
                    )
                } else {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MateTheme.colors.locked.copy(alpha = 0.25f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "Emerge con ${card.xpMissing} XP mas",
                            style = MaterialTheme.typography.labelSmall,
                            color = MateTheme.colors.ink
                        )
                    }
                }
            }
            if (alignRight) {
                Spacer(Modifier.width(10.dp))
                IslandArt(theme = card.world.theme, size = 116.dp, locked = !card.unlocked)
            }
        }
        if (showTrail) {
            TrailDots(alignRight)
        }
    }
}

@Composable
private fun TrailDots(alignRight: Boolean) {
    val color = MateTheme.colors.sea.copy(alpha = 0.55f)
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(34.dp)
    ) {
        val startX = if (alignRight) size.width * 0.78f else size.width * 0.22f
        val endX = if (alignRight) size.width * 0.24f else size.width * 0.76f
        drawLine(
            color = color,
            start = Offset(startX, 0f),
            end = Offset(endX, size.height),
            strokeWidth = 7f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 16f))
        )
    }
}

@Composable
private fun ProgressLine(percent: Int, color: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.20f))
    ) {
        Box(
            Modifier
                .fillMaxWidth(percent.coerceIn(0, 100) / 100f)
                .height(10.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

@Composable
private fun StatPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(value, style = MaterialTheme.typography.labelMedium, color = MateTheme.colors.ink)
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MateTheme.colors.inkSoft)
    }
}

@Composable
private fun IconChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(MateTheme.colors.cardAlt)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = MateTheme.colors.inkSoft)
    }
}

@Composable
private fun QuickAccess(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(MateTheme.colors.card)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, color = MateTheme.colors.ink)
        Text(
            subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MateTheme.colors.inkSoft,
            textAlign = TextAlign.Start
        )
    }
}
