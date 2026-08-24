package com.matelab.islas.ui.screens.stats

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matelab.islas.core.di.AppContainer
import com.matelab.islas.domain.model.Stats
import com.matelab.islas.domain.model.World
import com.matelab.islas.ui.art.IslandBadge
import com.matelab.islas.ui.art.SeaBackdrop
import com.matelab.islas.ui.components.KuboSays
import com.matelab.islas.ui.components.MatePanel
import com.matelab.islas.ui.screens.collection.Header
import com.matelab.islas.ui.navigation.mateViewModel
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import com.matelab.islas.ui.theme.paletteFor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatsUiState(
    val stats: Stats = Stats(),
    val worlds: List<World> = emptyList(),
    val loading: Boolean = true
)

class StatsViewModel(container: AppContainer) : ViewModel() {

    private val catalog = container.catalogRepository
    private val progress = container.progressRepository

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state

    init {
        viewModelScope.launch {
            val worlds = catalog.worlds()
            _state.update { it.copy(worlds = worlds) }
        }
        viewModelScope.launch {
            // Los datos se recalculan cada vez que cambia el progreso.
            progress.observeMissionProgress().collect {
                _state.update { current -> current.copy(stats = progress.stats(), loading = false) }
            }
        }
    }
}

/**
 * Estadisticas calculadas a partir de la tabla de intentos.
 * Ningun numero de esta pantalla esta escrito a mano.
 */
@Composable
fun StatsScreen(onBack: () -> Unit) {
    val viewModel: StatsViewModel = mateViewModel { StatsViewModel(it) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()
    val stats = state.stats

    SeaBackdrop(Modifier.fillMaxSize(), showHorizon = false) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp)
        ) {
            Header(title = "Tu progreso", onBack = onBack)
            Spacer(Modifier.height(12.dp))

            if (stats.totalAttempts == 0) {
                KuboSays("Todavia no hay datos. Juega una mision y aqui apareceran tus resultados.")
            } else {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BigStat("${stats.accuracyPercent} %", "aciertos", Teal, Modifier.weight(1f))
                    BigStat("${stats.missionsCompleted}", "misiones", Sun, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BigStat("${stats.totalStars}", "estrellas", MateTheme.colors.success, Modifier.weight(1f))
                    BigStat("${stats.crystalsUnlocked}", "cristales", MateTheme.colors.warning, Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))

                MatePanel(contentPadding = 14.dp) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "Retos de los ultimos 7 dias",
                            style = MaterialTheme.typography.titleLarge,
                            color = MateTheme.colors.ink
                        )
                        Spacer(Modifier.height(12.dp))
                        WeekChart(values = stats.last7Days)
                    }
                }

                Spacer(Modifier.height(16.dp))

                MatePanel(contentPadding = 14.dp) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            "Aciertos por isla",
                            style = MaterialTheme.typography.titleLarge,
                            color = MateTheme.colors.ink
                        )
                        Spacer(Modifier.height(10.dp))
                        state.worlds.forEach { world ->
                            val accuracy = stats.perWorldAccuracy[world.id] ?: 0
                            val attempts = stats.perWorldAttempts[world.id] ?: 0
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IslandBadge(theme = world.theme, size = 38.dp)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        world.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MateTheme.colors.ink
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(9.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(paletteFor(world.theme).primary.copy(alpha = 0.20f))
                                    ) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth(accuracy / 100f)
                                                .height(9.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(paletteFor(world.theme).primary)
                                        )
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "$accuracy %",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MateTheme.colors.ink
                                    )
                                    Text(
                                        "$attempts retos",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MateTheme.colors.inkSoft
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                KuboSays("Fallar forma parte de aprender. Lo importante es volver a intentarlo.")
            }
        }
    }
}

@Composable
private fun BigStat(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.displayMedium, color = MateTheme.colors.ink)
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MateTheme.colors.inkSoft,
            textAlign = TextAlign.Center
        )
    }
}

/** Grafico de barras dibujado con Canvas a partir de datos reales. */
@Composable
private fun WeekChart(values: List<Int>) {
    // Etiquetas relativas: la ultima barra es siempre hoy.
    val days = listOf("-6", "-5", "-4", "-3", "-2", "ayer", "hoy")
    val maxValue = (values.maxOrNull() ?: 0).coerceAtLeast(1)
    val barColor = Teal
    val trackColor = MateTheme.colors.outline

    Column(Modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            if (values.isEmpty()) return@Canvas
            val slot = size.width / values.size
            val barWidth = slot * 0.52f
            values.forEachIndexed { index, value ->
                val h = (value.toFloat() / maxValue) * size.height * 0.9f
                val x = slot * index + (slot - barWidth) / 2f
                drawRoundRect(
                    trackColor.copy(alpha = 0.35f),
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
                if (value > 0) {
                    drawRoundRect(
                        barColor,
                        topLeft = Offset(x, size.height - h),
                        size = Size(barWidth, h),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            values.indices.forEach { index ->
                Text(
                    days.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MateTheme.colors.inkSoft
                )
            }
        }
    }
}
