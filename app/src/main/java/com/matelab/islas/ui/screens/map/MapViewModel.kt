package com.matelab.islas.ui.screens.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matelab.islas.core.di.AppContainer
import com.matelab.islas.domain.engine.ProgressEngine
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.MissionProgress
import com.matelab.islas.domain.model.MissionStatus
import com.matelab.islas.domain.model.Profile
import com.matelab.islas.domain.model.World
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/** Estado de una isla en el mapa. */
data class WorldCard(
    val world: World,
    val unlocked: Boolean,
    val percent: Int,
    val missionsDone: Int,
    val missionsTotal: Int,
    val xpMissing: Int
)

/** Mision destacada como "siguiente paso". */
data class NextMissionCard(
    val mission: Mission,
    val world: World,
    val status: MissionStatus,
    val stars: Int
)

data class MapUiState(
    val loading: Boolean = true,
    val profile: Profile = Profile(),
    val worlds: List<WorldCard> = emptyList(),
    val next: NextMissionCard? = null,
    val pendingReview: Int = 0,
    val totalStars: Int = 0
)

/**
 * Estado del mapa del archipielago: que islas hay, cuales estan abiertas y
 * cual es la siguiente mision recomendada.
 */
class MapViewModel(container: AppContainer) : ViewModel() {

    private val catalog = container.catalogRepository
    private val player = container.playerRepository
    private val progress = container.progressRepository

    val state: StateFlow<MapUiState> = combine(
        catalog.observeWorlds(),
        catalog.observeMissions(),
        progress.observeMissionProgress(),
        player.observeProfile(),
        progress.observePendingReviewCount()
    ) { worlds, missions, progressMap, profile, pending ->
        build(worlds, missions, progressMap, profile, pending)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MapUiState()
    )

    init {
        viewModelScope.launch {
            val today = System.currentTimeMillis() / TimeUnit.DAYS.toMillis(1)
            player.registerVisit(today)
        }
    }

    private fun build(
        worlds: List<World>,
        missions: List<Mission>,
        progressMap: Map<String, MissionProgress>,
        profile: Profile,
        pending: Int
    ): MapUiState {
        val completedIds = progressMap.filterValues { ProgressEngine.isCompleted(it) }.keys

        val cards = worlds.map { world ->
            val own = missions.filter { it.worldId == world.id }
            WorldCard(
                world = world,
                unlocked = ProgressEngine.isWorldUnlocked(world, profile.xp),
                percent = ProgressEngine.worldPercent(own.map { it.id }, progressMap),
                missionsDone = own.count { it.id in completedIds },
                missionsTotal = own.size,
                xpMissing = (world.xpToUnlock - profile.xp).coerceAtLeast(0)
            )
        }

        val next = missions
            .sortedWith(compareBy({ worlds.firstOrNull { w -> w.id == it.worldId }?.order ?: 99 }, { it.order }))
            .firstOrNull { mission ->
                val world = worlds.firstOrNull { it.id == mission.worldId } ?: return@firstOrNull false
                val worldOpen = ProgressEngine.isWorldUnlocked(world, profile.xp)
                val status = ProgressEngine.statusFor(
                    mission, progressMap[mission.id], worldOpen, completedIds
                )
                status == MissionStatus.DISPONIBLE || status == MissionStatus.EMPEZADA ||
                    status == MissionStatus.COMPLETADA
            }
            ?.let { mission ->
                val world = worlds.first { it.id == mission.worldId }
                NextMissionCard(
                    mission = mission,
                    world = world,
                    status = ProgressEngine.statusFor(
                        mission, progressMap[mission.id],
                        ProgressEngine.isWorldUnlocked(world, profile.xp), completedIds
                    ),
                    stars = progressMap[mission.id]?.stars ?: 0
                )
            }

        return MapUiState(
            loading = false,
            profile = profile,
            worlds = cards,
            next = next,
            pendingReview = pending,
            totalStars = progressMap.values.sumOf { it.stars }
        )
    }
}
