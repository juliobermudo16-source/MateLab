package com.matelab.islas.ui.screens.onboarding

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.model.WorldTheme
import com.matelab.islas.ui.art.CrystalArt
import com.matelab.islas.ui.art.IslandArt
import com.matelab.islas.ui.art.Kubo
import com.matelab.islas.ui.art.KuboMood
import com.matelab.islas.ui.art.SeaBackdrop
import com.matelab.islas.ui.components.GhostButton
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.MatePanel
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import kotlinx.coroutines.launch

private data class OnboardPage(
    val title: String,
    val body: String,
    val art: @Composable () -> Unit
)

/**
 * Bienvenida en cuatro pantallas. Solo aparece la primera vez.
 */
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val feedback = rememberUiFeedback()
    val scope = rememberCoroutineScope()

    val pages = listOf(
        OnboardPage(
            title = "MateLab",
            body = "Bienvenido al archipielago. Soy Kubo y vamos a explorarlo resolviendo retos de matematicas.",
            art = { Kubo(mood = KuboMood.ANIMANDO, size = 170.dp) }
        ),
        OnboardPage(
            title = "Cuatro islas",
            body = "Formas, medidas, fracciones y numeros. Cada isla tiene sus propias misiones y su propio taller.",
            art = {
                Row(horizontalArrangement = Arrangement.spacedBy((-14).dp)) {
                    IslandArt(WorldTheme.FORMAS, size = 96.dp)
                    IslandArt(WorldTheme.MEDIDA, size = 96.dp)
                    IslandArt(WorldTheme.FRACCION, size = 96.dp)
                }
            }
        ),
        OnboardPage(
            title = "Explora y colecciona",
            body = "Cada mision da experiencia, estrellas y Cristales de Ingenio. Con experiencia emergen islas nuevas.",
            art = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CrystalArt(3, com.matelab.islas.domain.model.Rarity.RARO, true, size = 82.dp)
                    CrystalArt(7, com.matelab.islas.domain.model.Rarity.LEGENDARIO, true, size = 96.dp)
                    CrystalArt(12, com.matelab.islas.domain.model.Rarity.COMUN, true, size = 82.dp)
                }
            }
        ),
        OnboardPage(
            title = "Todo aqui dentro",
            body = "MateLab funciona sin internet y no pide ningun dato personal. Tu progreso se guarda solo en este movil.",
            art = { Kubo(mood = KuboMood.FELIZ, size = 150.dp) }
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    SeaBackdrop(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { index ->
                val page = pages[index]
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 26.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(Modifier.height(190.dp), contentAlignment = Alignment.Center) {
                        page.art()
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        page.title,
                        style = MaterialTheme.typography.displayMedium,
                        color = MateTheme.colors.ink,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    MatePanel(contentPadding = 16.dp) {
                        Text(
                            page.body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MateTheme.colors.inkSoft,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { i ->
                    val active = pagerState.currentPage == i
                    Box(
                        Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = if (active) 26.dp else 10.dp, height = 10.dp)
                            .clip(if (active) RoundedCornerShape(50) else CircleShape)
                            .background(if (active) Teal else MateTheme.colors.outline)
                    )
                }
            }

            Column(Modifier.padding(horizontal = 24.dp, vertical = 18.dp)) {
                val last = pagerState.currentPage == pages.lastIndex
                MateButton(
                    text = if (last) "Crear mi perfil" else "Siguiente",
                    onClick = {
                        feedback.tap()
                        if (last) {
                            onFinished()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                    color = if (last) Sun else Teal,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!last) {
                    Spacer(Modifier.height(8.dp))
                    GhostButton(
                        text = "Saltar",
                        onClick = {
                            feedback.tap()
                            onFinished()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.width(0.dp))
            }
        }
    }
}
