package com.matelab.islas.data.repository

import com.matelab.islas.data.local.dao.CatalogDao
import com.matelab.islas.data.local.dao.PlayerDao
import com.matelab.islas.data.local.dao.ProgressDao
import com.matelab.islas.data.local.dao.RewardDao
import com.matelab.islas.data.local.entity.AttemptEntity
import com.matelab.islas.data.local.entity.BadgeUnlockEntity
import com.matelab.islas.data.local.entity.CollectibleUnlockEntity
import com.matelab.islas.data.local.entity.MissionProgressEntity
import com.matelab.islas.data.local.entity.ProfileEntity
import com.matelab.islas.data.local.entity.ReviewItemEntity
import com.matelab.islas.data.local.toDomain
import com.matelab.islas.domain.engine.BadgeContext
import com.matelab.islas.domain.engine.BadgeEngine
import com.matelab.islas.domain.engine.CollectibleEngine
import com.matelab.islas.domain.engine.ProgressEngine
import com.matelab.islas.domain.engine.ReviewEngine
import com.matelab.islas.domain.model.Badge
import com.matelab.islas.domain.model.ChallengeResult
import com.matelab.islas.domain.model.Collectible
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.MissionOutcome
import com.matelab.islas.domain.model.MissionProgress
import com.matelab.islas.domain.model.ReviewItem
import com.matelab.islas.domain.model.Stats
import com.matelab.islas.domain.model.World
import com.matelab.islas.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class ProgressRepositoryImpl(
    private val progressDao: ProgressDao,
    private val catalogDao: CatalogDao,
    private val rewardDao: RewardDao,
    private val playerDao: PlayerDao,
    private val now: () -> Long = { System.currentTimeMillis() }
) : ProgressRepository {

    override fun observeMissionProgress(): Flow<Map<String, MissionProgress>> =
        progressDao.observeProgress().map { list ->
            list.associate { it.missionId to it.toDomain() }
        }

    override fun observeUnlockedBadgeIds(): Flow<Set<String>> =
        rewardDao.observeBadgeUnlocks().map { list -> list.map { it.badgeId }.toSet() }

    override fun observeUnlockedCollectibleIds(): Flow<Set<String>> =
        rewardDao.observeCollectibleUnlocks().map { list -> list.map { it.collectibleId }.toSet() }

    override fun observePendingReviewCount(): Flow<Int> = progressDao.observePendingReviewCount()

    override suspend fun progressMap(): Map<String, MissionProgress> =
        progressDao.allProgress().associate { it.missionId to it.toDomain() }

    override suspend fun unlockedBadgeIds(): Set<String> =
        rewardDao.badgeUnlocks().map { it.badgeId }.toSet()

    override suspend fun unlockedCollectibleIds(): Set<String> =
        rewardDao.collectibleUnlocks().map { it.collectibleId }.toSet()

    override suspend fun reviewItems(): List<ReviewItem> =
        progressDao.reviewItems().map { it.toDomain() }

    // ------------------------------------------------------------- misiones

    override suspend fun finishMission(
        missionId: String,
        results: List<ChallengeResult>
    ): MissionOutcome {
        val timestamp = now()
        val mission = catalogDao.mission(missionId)?.toDomain()
            ?: return MissionOutcome(missionId, 0, 0, 0, 0L, 0, 0)
        val worldId = mission.worldId

        recordAttempts(results, missionId, worldId, timestamp)

        val correct = results.count { it.correct }
        val total = results.size
        val hintsUsed = results.count { it.usedHint }
        val elapsed = results.sumOf { it.elapsedMs }
        val stars = ProgressEngine.starsFor(correct, total, hintsUsed)
        val percent = if (total == 0) 0 else correct * 100 / total
        val xpEarned = ProgressEngine.xpFor(correct, stars, mission.difficulty)

        val previous = progressDao.progressOf(missionId)?.toDomain()
        val merged = ProgressEngine.mergeProgress(previous, missionId, stars, percent, timestamp)
        progressDao.upsertProgress(
            MissionProgressEntity(
                missionId = merged.missionId,
                status = merged.status.name,
                stars = merged.stars,
                bestPercent = merged.bestPercent,
                timesPlayed = merged.timesPlayed,
                noHintRun = (progressDao.progressOf(missionId)?.noHintRun ?: false) ||
                    (hintsUsed == 0 && stars >= 1),
                lastPlayedAt = timestamp
            )
        )

        // ------------------------------------------------------------- XP
        val profileEntity = playerDao.profile() ?: ProfileEntity()
        val xpBefore = profileEntity.xp
        val xpAfter = xpBefore + xpEarned
        playerDao.upsertProfile(profileEntity.copy(xp = xpAfter))

        val levelBefore = ProgressEngine.levelFor(xpBefore)
        val levelAfter = ProgressEngine.levelFor(xpAfter)

        val allWorlds = catalogDao.worlds().map { it.toDomain() }
        val newWorlds = allWorlds.filter {
            !ProgressEngine.isWorldUnlocked(it, xpBefore) && ProgressEngine.isWorldUnlocked(it, xpAfter)
        }

        // -------------------------------------------------------- recompensas
        val newCollectibles = mutableListOf<Collectible>()
        val newBadges = mutableListOf<Badge>()

        val allCollectibles = catalogDao.collectibles().map { it.toDomain() }
        val allBadges = catalogDao.badges().map { it.toDomain() }

        CollectibleEngine.rewardFor(mission, stars, unlockedCollectibleIds())?.let { rewardId ->
            rewardDao.unlockCollectible(CollectibleUnlockEntity(rewardId, timestamp))
            allCollectibles.firstOrNull { it.id == rewardId }?.let { newCollectibles += it }
        }

        // Primera pasada de insignias con el estado ya actualizado.
        newBadges += unlockPendingBadges(allBadges, timestamp)

        // Hitos de coleccion (dependen de nivel, estrellas e insignias).
        val worldsCompleted = completedWorldIds(allWorlds)
        val milestones = CollectibleEngine.milestoneUnlocks(
            all = allCollectibles,
            alreadyUnlocked = unlockedCollectibleIds(),
            worldsCompleted = worldsCompleted,
            level = levelAfter,
            totalStars = progressDao.totalStars(),
            badgesUnlocked = rewardDao.badgeCount()
        )
        milestones.forEach { rewardDao.unlockCollectible(CollectibleUnlockEntity(it.id, timestamp)) }
        newCollectibles += milestones

        // Segunda pasada: algunos logros dependen del tamano de la coleccion.
        newBadges += unlockPendingBadges(allBadges, timestamp)

        return MissionOutcome(
            missionId = missionId,
            correct = correct,
            total = total,
            hintsUsed = hintsUsed,
            elapsedMs = elapsed,
            stars = stars,
            xpEarned = xpEarned,
            newBadges = newBadges.distinctBy { it.id },
            newCollectibles = newCollectibles.distinctBy { it.id },
            leveledUpTo = if (levelAfter > levelBefore) levelAfter else null,
            unlockedWorlds = newWorlds
        )
    }

    override suspend fun finishReview(results: List<ChallengeResult>): Boolean {
        val timestamp = now()
        results.forEach { result ->
            val challenge = catalogDao.challengesByIds(listOf(result.challengeId)).firstOrNull()
            val missionId = challenge?.missionId ?: return@forEach
            val worldId = catalogDao.mission(missionId)?.worldId ?: return@forEach
            progressDao.insertAttempt(
                AttemptEntity(
                    challengeId = result.challengeId,
                    missionId = missionId,
                    worldId = worldId,
                    correct = result.correct,
                    usedHint = result.usedHint,
                    elapsedMs = result.elapsedMs,
                    timestamp = timestamp
                )
            )
            updateReviewItem(result, missionId, worldId, timestamp)
        }

        val cleared = ReviewEngine.isSessionCleared(results.count { it.correct }, results.size)
        if (cleared) {
            val profile = playerDao.profile() ?: ProfileEntity()
            playerDao.upsertProfile(
                profile.copy(reviewSessionsCleared = profile.reviewSessionsCleared + 1)
            )
            unlockPendingBadges(catalogDao.badges().map { it.toDomain() }, timestamp)
        }
        return cleared
    }

    // ------------------------------------------------------------ estadisticas

    override suspend fun stats(): Stats {
        val totalAttempts = progressDao.attemptCount()
        val correct = progressDao.correctCount()
        val rows = progressDao.accuracyByWorld()
        val since = now() - TimeUnit.DAYS.toMillis(6) - startOfDayOffset()
        val timestamps = progressDao.attemptTimestampsSince(since)

        val perDay = IntArray(7)
        val dayMs = TimeUnit.DAYS.toMillis(1)
        val todayIndex = now() / dayMs
        timestamps.forEach { ts ->
            val diff = (todayIndex - ts / dayMs).toInt()
            if (diff in 0..6) perDay[6 - diff]++
        }

        return Stats(
            totalAttempts = totalAttempts,
            correctAttempts = correct,
            missionsCompleted = progressDao.completedMissions(),
            totalStars = progressDao.totalStars(),
            badgesUnlocked = rewardDao.badgeCount(),
            crystalsUnlocked = rewardDao.collectibleCount(),
            perWorldAccuracy = rows.associate {
                it.worldId to if (it.total == 0) 0 else it.correct * 100 / it.total
            },
            perWorldAttempts = rows.associate { it.worldId to it.total },
            last7Days = perDay.toList()
        )
    }

    override suspend fun resetProgress() {
        progressDao.clearProgress()
        progressDao.clearAttempts()
        progressDao.clearReview()
        rewardDao.clearBadges()
        rewardDao.clearCollectibles()
        val profile = playerDao.profile() ?: ProfileEntity()
        playerDao.upsertProfile(
            profile.copy(xp = 0, streakDays = 1, lastPlayedDay = 0L, reviewSessionsCleared = 0)
        )
    }

    // ------------------------------------------------------------------ interno

    private suspend fun recordAttempts(
        results: List<ChallengeResult>,
        missionId: String,
        worldId: String,
        timestamp: Long
    ) {
        results.forEach { result ->
            progressDao.insertAttempt(
                AttemptEntity(
                    challengeId = result.challengeId,
                    missionId = missionId,
                    worldId = worldId,
                    correct = result.correct,
                    usedHint = result.usedHint,
                    elapsedMs = result.elapsedMs,
                    timestamp = timestamp
                )
            )
            updateReviewItem(result, missionId, worldId, timestamp)
        }
    }

    private suspend fun updateReviewItem(
        result: ChallengeResult,
        missionId: String,
        worldId: String,
        timestamp: Long
    ) {
        val existing = progressDao.reviewItem(result.challengeId)
        when {
            existing == null && !result.correct -> progressDao.upsertReview(
                ReviewItemEntity(result.challengeId, missionId, worldId, 1, timestamp, false)
            )
            existing != null && result.correct -> progressDao.upsertReview(
                existing.copy(resolved = true)
            )
            existing != null && !result.correct -> progressDao.upsertReview(
                existing.copy(
                    wrongCount = existing.wrongCount + 1,
                    lastWrongAt = timestamp,
                    resolved = false
                )
            )
        }
    }

    private suspend fun completedWorldIds(worlds: List<World>): Set<String> {
        val progress = progressMap()
        return worlds.filter { world ->
            val missions = catalogDao.missionsOf(world.id).map { it.toDomain() }
            missions.isNotEmpty() && missions.all { ProgressEngine.isCompleted(progress[it.id]) }
        }.map { it.id }.toSet()
    }

    private suspend fun buildBadgeContext(): BadgeContext {
        val worlds = catalogDao.worlds().map { it.toDomain() }
        val progress = progressMap()
        val completedByWorld = mutableMapOf<String, Int>()
        val totalByWorld = mutableMapOf<String, Int>()
        worlds.forEach { world ->
            val missions: List<Mission> = catalogDao.missionsOf(world.id).map { it.toDomain() }
            totalByWorld[world.id] = missions.size
            completedByWorld[world.id] = missions.count { ProgressEngine.isCompleted(progress[it.id]) }
        }
        val profile = playerDao.profile() ?: ProfileEntity()
        val accuracy = progressDao.accuracyByWorld().associate {
            it.worldId to if (it.total == 0) 0 else it.correct * 100 / it.total
        }
        return BadgeContext(
            missionsCompleted = progressDao.completedMissions(),
            perfectMissions = progressDao.perfectMissions(),
            noHintMissions = progressDao.noHintMissions(),
            totalStars = progressDao.totalStars(),
            xp = profile.xp,
            streakDays = profile.streakDays,
            collectionSize = rewardDao.collectibleCount(),
            pendingReviewItems = progressDao.pendingReviewCount(),
            reviewSessionsCleared = profile.reviewSessionsCleared,
            completedByWorld = completedByWorld,
            totalByWorld = totalByWorld,
            accuracyByWorld = accuracy
        )
    }

    private suspend fun unlockPendingBadges(allBadges: List<Badge>, timestamp: Long): List<Badge> {
        val context = buildBadgeContext()
        val earned = BadgeEngine.newlyEarned(allBadges, unlockedBadgeIds(), context)
        earned.forEach { rewardDao.unlockBadge(BadgeUnlockEntity(it.id, timestamp)) }
        return earned
    }

    /** Milisegundos transcurridos hoy, para alinear la ventana de 7 dias. */
    private fun startOfDayOffset(): Long = now() % TimeUnit.DAYS.toMillis(1)
}
