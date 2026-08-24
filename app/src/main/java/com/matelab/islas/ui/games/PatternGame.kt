package com.matelab.islas.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.engine.PatternEngine
import com.matelab.islas.domain.model.PatternPayload
import com.matelab.islas.domain.model.PatternToken
import com.matelab.islas.ui.art.TokenShape
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.ShapePalette
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal

/**
 * Patrones y secuencias.
 *
 * La pieza elegida se coloca de verdad en el hueco de la fila para que el
 * nino vea si el ritmo del patron encaja antes de comprobar.
 */
@Composable
fun PatternGame(
    payload: PatternPayload,
    enabled: Boolean,
    onSubmit: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = rememberUiFeedback()
    var chosen by remember { mutableIntStateOf(-1) }
    val scroll = rememberScrollState()

    Column(modifier.fillMaxWidth()) {

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(MateTheme.colors.cardAlt)
                .padding(vertical = 16.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scroll)
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                payload.sequence.forEachIndexed { index, token ->
                    if (index == payload.holeIndex) {
                        HoleSlot(
                            token = payload.options.getOrNull(chosen),
                            highlighted = chosen >= 0
                        )
                    } else {
                        TokenView(token)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "Elige la pieza que falta",
            style = MaterialTheme.typography.titleMedium,
            color = MateTheme.colors.inkSoft
        )

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            payload.options.forEachIndexed { index, option ->
                val selected = chosen == index
                val scale by animateFloatAsState(if (selected) 1.06f else 1f, tween(140), label = "opt")
                Box(
                    Modifier
                        .weight(1f)
                        .scale(scale)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (selected) Teal.copy(alpha = 0.22f) else MateTheme.colors.card)
                        .border(
                            width = if (selected) 3.dp else 1.5.dp,
                            color = if (selected) Teal else MateTheme.colors.outline,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .pointerInput(enabled, index) {
                            if (!enabled) return@pointerInput
                            detectTapGestures {
                                chosen = index
                                feedback.tap()
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TokenView(option, small = true)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        MateButton(
            text = "Comprobar",
            onClick = { onSubmit(PatternEngine.isCorrect(chosen, payload.answerIndex)) },
            enabled = enabled && chosen >= 0,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TokenView(token: PatternToken, small: Boolean = false) {
    val size = if (small) 40.dp else 52.dp
    if (token.shape != null) {
        TokenShape(
            shape = token.shape,
            color = ShapePalette[token.colorIndex % ShapePalette.size],
            size = size,
            rotationDegrees = token.rotation
        )
    } else {
        Box(
            Modifier
                .size(size)
                .clip(RoundedCornerShape(14.dp))
                .background(Sun.copy(alpha = 0.28f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                token.label,
                style = MaterialTheme.typography.headlineSmall,
                color = MateTheme.colors.ink
            )
        }
    }
}

@Composable
private fun HoleSlot(token: PatternToken?, highlighted: Boolean) {
    Box(
        Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (highlighted) Teal.copy(alpha = 0.15f) else MateTheme.colors.card)
            .border(
                width = 3.dp,
                color = if (highlighted) Teal else Sun,
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (token == null) {
            Text("?", style = MaterialTheme.typography.displayMedium, color = Sun)
        } else {
            TokenView(token, small = true)
        }
    }
}
