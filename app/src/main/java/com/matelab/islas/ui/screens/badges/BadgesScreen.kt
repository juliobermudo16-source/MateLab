package com.matelab.islas.ui.screens.badges

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matelab.islas.core.di.AppContainer
import com.matelab.islas.domain.engine.BadgeContext
import com.matelab.islas.domain.engine.BadgeEngine
import com.matelab.islas.domain.engine.ProgressEngine
import com.matelab.islas.domain.model.Badge
import com.matelab.islas.ui.art.BadgeArt
import com.matelab.islas.ui.art.SeaBackdrop
import com.matelab.islas.ui.components.KuboSays
import com.matelab.islas.ui.components.MatePanel
import com.matelab.islas.ui.screens.collection.Header
import com.matelab.islas.ui.navigation.mateViewModel
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Sun
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BadgesUiState(
    val badges: List<Badge> = emptyList(),
    val unlocked: Set<String> = emptySet(),
    val context: BadgeContext = BadgeContext()
)

class BadgesViewModel(container: AppContainer) : ViewModel() {

    private val catalog = container.catalogRepository
    private val progress = container.progressRepository
    private val player = container.playerRepository

    private val _state = MutableStateFlow(BadgesUiState())
    val state: StateFlow<BadgesUiState> = _state

    init {
        viewModelScope.launch {
            _state.update { it.copy(badges = catalog.badges().sortedBy { b -> b.artSeed }) }
            refreshContext()
        }
        viewModelScope.launch {
            progress.observeUnlockedBadgeIds().collect { ids ->
                _state.update { it.copy(unlocked = ids) }
                refreshContext()
            }
        }
    }

    /** El contexto se recalcula con datos reales de la base, no con cache. */
    private suspend fun refreshContext() {
        val stats = progress.stats()
        val profile = player.profile()
        val worlds = catalog.worlds()
        val progressMap = progress.progressMap()
        val completedByWorld = mutableMapOf<String, Int>()
        val totalByWorld = mutableMapOf<String, Int>()
        worlds.forEach { world ->
            val missions = catalog.missionsOf(world.id)
            totalByWorld[world.id] = missions.size
            completedByWorld[world.id] = missions.count {
                ProgressEngine.isCompleted(progressMap[it.id])
            }
        }
        _state.update {
            it.copy(
                context = BadgeContext(
                    missionsCompleted = stats.missionsCompleted,
                    perfectMissions = progressMap.values.count { p -> p.stars >= 3 },
                    totalStars = stats.totalStars,
                    xp = profile.xp,
                    streakDays = profile.streakDays,
                    collectionSize = stats.crystalsUnlocked,
                    completedByWorld = completedByWorld,
                    totalByWorld = totalByWorld,
                    accuracyByWorld = stats.perWorldAccuracy
                )
            )
        }
    }
}

/** Vitrina de insignias con el avance real de cada una. */
@Composable
fun BadgesScreen(onBack: () -> Unit) {
    val viewModel: BadgesViewModel = mateViewModel { BadgesViewModel(it) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    SeaBackdrop(Modifier.fillMaxSize(), showHorizon = false) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp)
        ) {
            Header(title = "Insignias", onBack = onBack)
            Spacer(Modifier.height(10.dp))
            KuboSays("Las insignias se ganan haciendo cosas, no por tiempo jugado.")
            Spacer(Modifier.height(14.dp))

            Text(
                "${state.unlocked.size} de ${state.badges.size} conseguidas",
                style = MaterialTheme.typography.titleLarge,
                color = MateTheme.colors.inkSoft
            )
            Spacer(Modifier.height(10.dp))

            state.badges.forEach { badge ->
                val unlocked = badge.id in state.unlocked
                val progressValue = BadgeEngine.progressOf(badge, state.context)
                MatePanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    contentPadding = 12.dp,
                    color = if (unlocked) MateTheme.colors.card else MateTheme.colors.cardAlt
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BadgeArt(artSeed = badge.artSeed, unlocked = unlocked, size = 66.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                badge.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (unlocked) MateTheme.colors.ink else MateTheme.colors.inkSoft
                            )
                            Text(
                                badge.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MateTheme.colors.inkSoft
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Sun.copy(alpha = 0.22f))
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth(progressValue)
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Sun)
                                    )
                                }
                                Text(
                                    if (unlocked) "Conseguida"
                                    else BadgeEngine.progressLabel(badge, state.context),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MateTheme.colors.inkSoft
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
