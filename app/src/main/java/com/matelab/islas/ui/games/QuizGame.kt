package com.matelab.islas.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.model.QuizPayload
import com.matelab.islas.ui.art.SceneArt
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Teal

/**
 * Reto de eleccion con ilustracion.
 *
 * Se usa como apoyo puntual: la mayor parte de MateLab son mini-juegos de
 * manipulacion, no preguntas de test.
 */
@Composable
fun QuizGame(
    payload: QuizPayload,
    enabled: Boolean,
    onSubmit: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = rememberUiFeedback()
    var chosen by remember { mutableIntStateOf(-1) }
    val letters = listOf("A", "B", "C", "D", "E")

    Column(modifier.fillMaxWidth()) {

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(MateTheme.colors.cardAlt)
                .padding(10.dp)
        ) {
            SceneArt(key = payload.art, height = 140.dp)
        }

        Spacer(Modifier.height(14.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            payload.options.forEachIndexed { index, option ->
                val selected = chosen == index
                val scale by animateFloatAsState(if (selected) 1.02f else 1f, tween(120), label = "quiz")
                Row(
                    Modifier
                        .fillMaxWidth()
                        .scale(scale)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (selected) Teal.copy(alpha = 0.18f) else MateTheme.colors.card)
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
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (selected) Teal else MateTheme.colors.outline),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            letters.getOrElse(index) { "?" },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else MateTheme.colors.inkSoft
                        )
                    }
                    Spacer(Modifier.size(12.dp))
                    Text(
                        option,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MateTheme.colors.ink
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        MateButton(
            text = "Comprobar",
            onClick = { onSubmit(chosen == payload.answerIndex) },
            enabled = enabled && chosen >= 0,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
