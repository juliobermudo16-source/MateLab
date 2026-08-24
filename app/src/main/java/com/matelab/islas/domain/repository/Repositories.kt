package com.matelab.islas.domain.repository

import com.matelab.islas.domain.model.Badge
import com.matelab.islas.domain.model.Challenge
import com.matelab.islas.domain.model.ChallengeResult
import com.matelab.islas.domain.model.Collectible
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.MissionOutcome
import com.matelab.islas.domain.model.MissionProgress
import com.matelab.islas.domain.model.Profile
import com.matelab.islas.domain.model.ReviewItem
import com.matelab.islas.domain.model.Settings
import com.matelab.islas.domain.model.Stats
import com.matelab.islas.domain.model.World
import kotlinx.coroutines.flow.Flow

/** Acceso de solo lectura al contenido educativo guardado en Room. */
interface CatalogRepository {
    fun observeWorlds(): Flow<List<World>>
    fun observeMissions(): Flow<List<Mission>>
    suspend fun worlds(): List<World>
    suspend fun missions(): List<Mission>
    suspend fun missionsOf(worldId: String): List<Mission>
    suspend fun mission(missionId: String): Mission?
    suspend fun challengesOf(missionId: String): List<Challenge>
    suspend fun challengesByIds(ids: List<String>): List<Challenge>
    suspend fun badges(): List<Badge>
    suspend fun collectibles(): List<Collectible>
}

/** Perfil local y ajustes. */
interface PlayerRepository {
    fun observeProfile(): Flow<Profile>
    fun observeSettings(): Flow<Settings>
    suspend fun profile(): Profile
    suspend fun saveAlias(alias: String)
    suspend fun saveAvatar(avatarId: Int)
    suspend fun completeOnboarding()
    suspend fun completeProfile(alias: String, avatarId: Int)
    suspend fun updateSettings(settings: Settings)
    suspend fun registerVisit(today: Long)
}

/** Progreso, recompensas, estadisticas y repaso. */
interface ProgressRepository {
    fun observeMissionProgress(): Flow<Map<String, MissionProgress>>
    fun observeUnlockedBadgeIds(): Flow<Set<String>>
    fun observeUnlockedCollectibleIds(): Flow<Set<String>>
    fun observePendingReviewCount(): Flow<Int>

    suspend fun progressMap(): Map<String, MissionProgress>
    suspend fun unlockedBadgeIds(): Set<String>
    suspend fun unlockedCollectibleIds(): Set<String>
    suspend fun reviewItems(): List<ReviewItem>

    /**
     * Cierra una mision: guarda intentos, estrellas, XP y comprueba que
     * insignias y cristales se han ganado de verdad.
     */
    suspend fun finishMission(missionId: String, results: List<ChallengeResult>): MissionOutcome

    /** Registra una sesion de repaso. Devuelve true si se completo sin fallos. */
    suspend fun finishReview(results: List<ChallengeResult>): Boolean

    suspend fun stats(): Stats

    /** Borra todo el progreso y deja el catalogo intacto. */
    suspend fun resetProgress()
}
