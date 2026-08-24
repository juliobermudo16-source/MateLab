package com.matelab.islas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.matelab.islas.data.local.entity.AttemptEntity
import com.matelab.islas.data.local.entity.MissionProgressEntity
import com.matelab.islas.data.local.entity.ReviewItemEntity
import kotlinx.coroutines.flow.Flow

/** Fila agregada de aciertos por isla. */
data class WorldAccuracyRow(
    val worldId: String,
    val total: Int,
    val correct: Int
)

@Dao
interface ProgressDao {

    // ------------------------------------------------------ progreso mision

    @Query("SELECT * FROM mission_progress")
    fun observeProgress(): Flow<List<MissionProgressEntity>>

    @Query("SELECT * FROM mission_progress")
    suspend fun allProgress(): List<MissionProgressEntity>

    @Query("SELECT * FROM mission_progress WHERE mission_id = :missionId")
    suspend fun progressOf(missionId: String): MissionProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: MissionProgressEntity)

    @Query("SELECT COALESCE(SUM(stars), 0) FROM mission_progress")
    suspend fun totalStars(): Int

    @Query("SELECT COUNT(*) FROM mission_progress WHERE stars >= 1")
    suspend fun completedMissions(): Int

    @Query("SELECT COUNT(*) FROM mission_progress WHERE stars >= 3")
    suspend fun perfectMissions(): Int

    @Query("SELECT COUNT(*) FROM mission_progress WHERE no_hint_run = 1")
    suspend fun noHintMissions(): Int

    @Query("DELETE FROM mission_progress")
    suspend fun clearProgress()

    // ----------------------------------------------------------- intentos

    @Insert
    suspend fun insertAttempt(attempt: AttemptEntity)

    @Query("SELECT COUNT(*) FROM attempt")
    suspend fun attemptCount(): Int

    @Query("SELECT COUNT(*) FROM attempt WHERE correct = 1")
    suspend fun correctCount(): Int

    @Query(
        """
        SELECT world_id AS worldId,
               COUNT(*) AS total,
               SUM(CASE WHEN correct = 1 THEN 1 ELSE 0 END) AS correct
        FROM attempt
        GROUP BY world_id
        """
    )
    suspend fun accuracyByWorld(): List<WorldAccuracyRow>

    @Query("SELECT timestamp FROM attempt WHERE timestamp >= :since ORDER BY timestamp")
    suspend fun attemptTimestampsSince(since: Long): List<Long>

    @Query("SELECT * FROM attempt ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recentAttempts(limit: Int): List<AttemptEntity>

    @Query("DELETE FROM attempt")
    suspend fun clearAttempts()

    // ------------------------------------------------------------- repaso

    @Query("SELECT * FROM review_item ORDER BY wrong_count DESC, last_wrong_at ASC")
    fun observeReview(): Flow<List<ReviewItemEntity>>

    @Query("SELECT * FROM review_item ORDER BY wrong_count DESC, last_wrong_at ASC")
    suspend fun reviewItems(): List<ReviewItemEntity>

    @Query("SELECT * FROM review_item WHERE challenge_id = :challengeId")
    suspend fun reviewItem(challengeId: String): ReviewItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReview(item: ReviewItemEntity)

    @Query("SELECT COUNT(*) FROM review_item WHERE resolved = 0")
    suspend fun pendingReviewCount(): Int

    @Query("SELECT COUNT(*) FROM review_item WHERE resolved = 0")
    fun observePendingReviewCount(): Flow<Int>

    @Query("DELETE FROM review_item")
    suspend fun clearReview()
}
