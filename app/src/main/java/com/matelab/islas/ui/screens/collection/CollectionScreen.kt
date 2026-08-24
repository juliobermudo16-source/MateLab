package com.matelab.islas.ui.screens.collection

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matelab.islas.core.di.AppContainer
import com.matelab.islas.domain.engine.CollectibleEngine
import com.matelab.islas.domain.model.Collectible
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.Rarity
import com.matelab.islas.ui.art.CrystalArt
import com.matelab.islas.ui.art.SeaBackdrop
import com.matelab.islas.ui.components.KuboSays
import com.matelab.islas.ui.components.MatePanel
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.navigation.mateViewModel
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sun
import com.matelab.islas.ui.theme.Teal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CollectionUiState(
    val collectibles: List<Collectible> = emptyList(),
    val unlocked: Set<String> = emptySet(),
    val missions: List<Mission> = emptyList()
) {
    val percent: Int get() = CollectibleEngine.completionPercent(collectibles.size, unlocked.size)
}

class CollectionViewModel(container: AppContainer) : ViewModel() {

    private val catalog = container.catalogRepository
    private val progress = container.progressRepository

    private val _state = MutableStateFlow(CollectionUiState())
    val state: StateFlow<CollectionUiState> = _state

    init {
        viewModelScope.launch {
            val all = catalog.collectibles().sortedBy { it.artSeed }
            val missions = catalog.missions()
            _state.update { it.copy(collectibles = all, missions = missions) }
        }
        viewModelScope.launch {
            progress.observeUnlockedCollectibleIds().collect { ids ->
                _state.update { it.copy(unlocked = ids) }
            }
        }
    }
}

/**
 * Vitrina de Cristales de Ingenio.
 * Cada cristal se consigue con progreso real y guarda un dato matematico.
 */
@Composable
fun CollectionScreen(onBack: () -> Unit) {
    val viewModel: CollectionViewModel = mateViewModel { CollectionViewModel(it) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val feedback = rememberUiFeedback()
    val scroll = rememberScrollState()
    var selected by remember { mutableStateOf<Collectible?>(null) }

    SeaBackdrop(Modifier.fillMaxSize(), showHorizon = false) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp)
        ) {
            Header(title = "Cristales de Ingenio", onBack = onBack)

            Spacer(Modifier.height(12.dp))

            MatePanel(contentPadding = 14.dp) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${state.unlocked.size} de ${state.collectibles.size}",
                            style = MaterialTheme.typography.displayMedium,
                            color = MateTheme.colors.ink
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "cristales encontrados",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MateTheme.colors.inkSoft
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Teal.copy(alpha = 0.2f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(state.percent / 100f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Teal)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            state.collectibles.chunked(3).forEach { row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { crystal ->
                        val unlocked = crystal.id in state.unlocked
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(MateTheme.colors.card)
                                .clickable {
                                    feedback.tap()
                                    selected = crystal
                                }
                                .padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CrystalArt(
                                artSeed = crystal.artSeed,
                                rarity = crystal.rarity,
                                unlocked = unlocked,
                                size = 68.dp
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                if (unlocked) crystal.name else "Por descubrir",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (unlocked) MateTheme.colors.ink else MateTheme.colors.inkSoft,
                                textAlign = TextAlign.Center
                            )
                            if (unlocked && crystal.rarity != Rarity.COMUN) {
                                Text(
                                    crystal.rarity.name.lowercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Sun
                                )
                            }
                        }
                    }
                    if (row.size < 3) repeat(3 - row.size) { Box(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(10.dp))
            KuboSays("Cada cristal guarda un dato curioso. Tocalo para leerlo.")
        }
    }

    selected?.let { crystal ->
        val unlocked = crystal.id in state.unlocked
        AlertDialog(
            onDismissRequest = { selected = null },
            confirmButton = {
                TextButton(onClick = { selected = null }) { Text("Cerrar") }
            },
            icon = {
                CrystalArt(crystal.artSeed, crystal.rarity, unlocked, size = 80.dp)
            },
            title = {
                Text(if (unlocked) crystal.name else "Cristal por descubrir")
            },
            text = {
                Text(
                    if (unlocked) crystal.fact
                    else CollectibleEngine.hintFor(crystal, state.missions)
                )
            }
        )
    }
}

/** Cabecera comun de las pantallas secundarias. */
@Composable
fun Header(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MateTheme.colors.card)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Volver",
                tint = MateTheme.colors.ink
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            color = MateTheme.colors.ink
        )
    }
}
