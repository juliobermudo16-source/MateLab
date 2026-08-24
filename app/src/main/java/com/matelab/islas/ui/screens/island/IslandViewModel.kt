package com.matelab.islas.ui.screens.island

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matelab.islas.core.di.AppContainer
import com.matelab.islas.domain.engine.ProgressEngine
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.MissionStatus
import com.matelab.islas.domain.model.World
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MissionRow(
    val mission: Mission,
    val status: MissionStatus,
    val stars: Int,
    val bestPercent: Int,
    val timesPlayed: Int,
    val blockedBy: List<String>
)

data class IslandUiState(
    val loading: Boolean = true,
    val world: World? = null,
    val missions: List<MissionRow> = emptyList(),
    val percent: Int = 0,
    val xp: Int = 0
)

/** Detalle de una isla: su sendero de misiones y el estado real de cada una. */
class IslandViewModel(
    container: AppContainer,
    private val worldId: String
) : ViewModel() {

    private val catalog = container.catalogRepository
    private val player = container.playerRepository
    private val progress = container.progressRepository

    val state: StateFlow<IslandUiState> = combine(
        catalog.observeWorlds(),
        catalog.observeMissions(),
        progress.observeMissionProgress(),
        player.observeProfile()
    ) { worlds, missions, progressMap, profile ->
        val world = worlds.firstOrNull { it.id == worldId }
            ?: return@combine IslandUiState(loading = false)
        val own = missions.filter { it.worldId == worldId }.sortedBy { it.order }
        val completed = progressMap.filterValues { ProgressEngine.isCompleted(it) }.keys
        val worldOpen = ProgressEngine.isWorldUnlocked(world, profile.xp)

        val rows = own.map { mission ->
            val p = progressMap[mission.id]
            MissionRow(
                mission = mission,
                status = ProgressEngine.statusFor(mission, p, worldOpen, completed),
                stars = p?.stars ?: 0,
                bestPercent = p?.bestPercent ?: 0,
                timesPlayed = p?.timesPlayed ?: 0,
                blockedBy = mission.requires.filter { it !in completed }
                    .mapNotNull { req -> missions.firstOrNull { it.id == req }?.name }
            )
        }

        IslandUiState(
            loading = false,
            world = world,
            missions = rows,
            percent = ProgressEngine.worldPercent(own.map { it.id }, progressMap),
            xp = profile.xp
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IslandUiState())
}
