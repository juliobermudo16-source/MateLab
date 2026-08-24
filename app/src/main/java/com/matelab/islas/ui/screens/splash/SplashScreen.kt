package com.matelab.islas.ui.screens.splash

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.matelab.islas.ui.art.Kubo
import com.matelab.islas.ui.art.KuboMood
import com.matelab.islas.ui.art.SeaBackdrop
import com.matelab.islas.ui.theme.MateTheme

/**
 * Pantalla de arranque propia mientras se lee el perfil de la base de datos.
 */
@Composable
fun SplashScreen() {
    val transition = rememberInfiniteTransition(label = "splash")
    val pulse by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )

    SeaBackdrop(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Kubo(mood = KuboMood.FELIZ, size = 150.dp)
                Spacer(Modifier.height(14.dp))
                Text(
                    "MateLab",
                    style = MaterialTheme.typography.displayLarge,
                    color = MateTheme.colors.ink
                )
                Text(
                    "Islas del Ingenio",
                    style = MaterialTheme.typography.titleLarge,
                    color = MateTheme.colors.inkSoft
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    "Preparando la expedicion",
                    style = MaterialTheme.typography.labelMedium,
                    color = MateTheme.colors.inkSoft,
                    modifier = Modifier.alpha(pulse)
                )
            }
        }
    }
}
