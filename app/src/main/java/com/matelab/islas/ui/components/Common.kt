package com.matelab.islas.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.engine.ProgressEngine
import com.matelab.islas.domain.model.MissionStatus
import com.matelab.islas.ui.art.Kubo
import com.matelab.islas.ui.art.KuboMood
import com.matelab.islas.ui.art.starPath
import com.matelab.islas.ui.theme.Deep
import com.matelab.islas.ui.theme.LocalReducedMotion
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal

/**
 * Boton principal con relieve: se hunde al pulsarlo, como en un juego.
 */
@Composable
fun MateButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Teal,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduced = LocalReducedMotion.current
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduced) 0.96f else 1f,
        animationSpec = tween(90),
        label = "press"
    )
    val base = if (enabled) color else MateTheme.colors.locked
    val shadow = if (enabled) darken(color) else MateTheme.colors.locked

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(shadow)
            .padding(bottom = 5.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(listOf(lighten(base), base))
                )
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(horizontal = 22.dp, vertical = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leading != null) {
                    leading()
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Boton secundario, mas discreto. */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MateTheme.colors.inkSoft
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(BorderStroke(2.dp, color.copy(alpha = 0.45f)), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

/** Tarjeta base con borde suave; sustituye a las Card planas de Material. */
@Composable
fun MatePanel(
    modifier: Modifier = Modifier,
    color: Color = MateTheme.colors.card,
    contentPadding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, MateTheme.colors.outline),
        shadowElevation = 0.dp
    ) {
        Box(Modifier.padding(contentPadding)) { content() }
    }
}

/** Fila de estrellas conseguidas, dibujadas a mano. */
@Composable
fun StarRow(
    stars: Int,
    modifier: Modifier = Modifier,
    max: Int = 3,
    size: Dp = 22.dp
) {
    Row(
        modifier = modifier.semantics { contentDescription = "$stars de $max estrellas" },
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(max) { index ->
            val filled = index < stars
            Canvas(Modifier.size(size)) {
                val side = this.size.width
                val path = starPath(Offset(side / 2f, side / 2f), side * 0.46f, side * 0.20f)
                drawPath(path, if (filled) Sun else Color(0x33607A80))
            }
        }
    }
}

/**
 * Barra de experiencia con nivel. El porcentaje sale del motor de progreso.
 */
@Composable
fun XpBar(xp: Int, modifier: Modifier = Modifier, showLabels: Boolean = true) {
    val level = ProgressEngine.levelFor(xp)
    val progress = ProgressEngine.levelProgress(xp)
    val missing = ProgressEngine.xpToNextLevel(xp)
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(700),
        label = "xp"
    )

    Column(modifier) {
        if (showLabels) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Nivel $level",
                    style = MaterialTheme.typography.labelMedium,
                    color = MateTheme.colors.ink
                )
                Text(
                    if (missing > 0) "faltan $missing XP" else "nivel maximo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MateTheme.colors.inkSoft
                )
            }
            Spacer(Modifier.height(4.dp))
        }
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(12.dp)
        ) {
            val r = CornerRadius(size.height / 2f, size.height / 2f)
            drawRoundRect(MateTheme_outlineFallback, size = size, cornerRadius = r)
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(Teal, Sun)),
                size = Size(size.width * animated.coerceIn(0f, 1f), size.height),
                cornerRadius = r
            )
        }
    }
}

private val MateTheme_outlineFallback = Color(0x3312B3A6)

/**
 * Distintivo de estado de una mision.
 * Nunca se apoya solo en el color: siempre lleva simbolo y texto.
 */
@Composable
fun StatusChip(status: MissionStatus, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        MissionStatus.BLOQUEADA -> "Bloqueada" to MateTheme.colors.locked
        MissionStatus.DISPONIBLE -> "Disponible" to Teal
        MissionStatus.EMPEZADA -> "Empezada" to Sun
        MissionStatus.COMPLETADA -> "Completada" to MateTheme.colors.success
        MissionStatus.DOMINADA -> "Dominada" to Sun
    }
    val icon = when (status) {
        MissionStatus.BLOQUEADA -> Icons.Rounded.Lock
        MissionStatus.DISPONIBLE -> Icons.Rounded.PlayArrow
        MissionStatus.EMPEZADA -> Icons.Rounded.Bolt
        MissionStatus.COMPLETADA -> Icons.Rounded.Check
        MissionStatus.DOMINADA -> Icons.Rounded.Star
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MateTheme.colors.ink)
    }
}

/** Titulo de seccion con una pequena barra de color al lado. */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Teal,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(width = 6.dp, height = 22.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            style = MaterialTheme.typography.headlineSmall,
            color = MateTheme.colors.ink,
            modifier = Modifier.weight(1f)
        )
        trailing?.invoke()
    }
}

/** Kubo diciendo algo. Frases cortas, nunca parrafos. */
@Composable
fun KuboSays(
    text: String,
    modifier: Modifier = Modifier,
    mood: KuboMood = KuboMood.FELIZ,
    kuboSize: Dp = 74.dp
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Kubo(mood = mood, size = kuboSize)
        Spacer(Modifier.width(8.dp))
        Surface(
            color = MateTheme.colors.card,
            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp),
            border = BorderStroke(1.5.dp, MateTheme.colors.outline),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = MateTheme.colors.ink,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
            )
        }
    }
}

/** Contador con icono textual, por ejemplo cristales o estrellas. */
@Composable
fun CounterPill(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(value, style = MaterialTheme.typography.labelMedium, color = MateTheme.colors.ink)
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MateTheme.colors.inkSoft)
    }
}

internal fun darken(color: Color, factor: Float = 0.72f): Color = Color(
    red = color.red * factor,
    green = color.green * factor,
    blue = color.blue * factor,
    alpha = color.alpha
)

internal fun lighten(color: Color, factor: Float = 0.14f): Color = Color(
    red = (color.red + (1f - color.red) * factor).coerceIn(0f, 1f),
    green = (color.green + (1f - color.green) * factor).coerceIn(0f, 1f),
    blue = (color.blue + (1f - color.blue) * factor).coerceIn(0f, 1f),
    alpha = color.alpha
)

internal val DeepInk = Deep
