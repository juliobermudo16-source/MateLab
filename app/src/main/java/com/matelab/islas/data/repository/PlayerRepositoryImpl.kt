package com.matelab.islas.data.repository

import com.matelab.islas.data.local.dao.PlayerDao
import com.matelab.islas.data.local.dao.RewardDao
import com.matelab.islas.data.local.entity.ProfileEntity
import com.matelab.islas.data.local.toDomain
import com.matelab.islas.data.local.toEntity
import com.matelab.islas.domain.engine.ProgressEngine
import com.matelab.islas.domain.model.Profile
import com.matelab.islas.domain.model.Settings
import com.matelab.islas.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class PlayerRepositoryImpl(
    private val playerDao: PlayerDao,
    private val rewardDao: RewardDao
) : PlayerRepository {

    override fun observeProfile(): Flow<Profile> =
        combine(
            playerDao.observeProfile(),
            rewardDao.observeCollectibleCount()
        ) { entity, crystals ->
            (entity ?: ProfileEntity()).toDomain(crystals)
        }

    override fun observeSettings(): Flow<Settings> =
        playerDao.observeSettings().map { it?.toDomain() ?: Settings() }

    override suspend fun profile(): Profile {
        val entity = playerDao.profile() ?: ProfileEntity().also { playerDao.upsertProfile(it) }
        return entity.toDomain(rewardDao.collectibleCount())
    }

    override suspend fun saveAlias(alias: String) {
        val current = playerDao.profile() ?: ProfileEntity()
        playerDao.upsertProfile(current.copy(alias = sanitizeAlias(alias)))
    }

    override suspend fun saveAvatar(avatarId: Int) {
        val current = playerDao.profile() ?: ProfileEntity()
        playerDao.upsertProfile(current.copy(avatarId = avatarId.coerceIn(0, AVATAR_COUNT - 1)))
    }

    override suspend fun completeOnboarding() {
        val current = playerDao.profile() ?: ProfileEntity()
        playerDao.upsertProfile(current.copy(onboardingDone = true))
    }

    override suspend fun completeProfile(alias: String, avatarId: Int) {
        val current = playerDao.profile() ?: ProfileEntity()
        playerDao.upsertProfile(
            current.copy(
                alias = sanitizeAlias(alias),
                avatarId = avatarId.coerceIn(0, AVATAR_COUNT - 1),
                profileDone = true,
                onboardingDone = true
            )
        )
    }

    override suspend fun updateSettings(settings: Settings) {
        playerDao.upsertSettings(settings.toEntity())
    }

    override suspend fun registerVisit(today: Long) {
        val current = playerDao.profile() ?: ProfileEntity()
        val streak = ProgressEngine.nextStreak(current.streakDays, current.lastPlayedDay, today)
        playerDao.upsertProfile(current.copy(streakDays = streak, lastPlayedDay = today))
    }

    companion object {
        const val AVATAR_COUNT = 8
        const val MAX_ALIAS_LENGTH = 14

        /**
         * El apodo se recorta y se limpia: nada de saltos de linea ni textos
         * kilometricos que rompan la interfaz.
         */
        fun sanitizeAlias(raw: String): String =
            raw.replace(Regex("\\s+"), " ").trim().take(MAX_ALIAS_LENGTH)
    }
}
