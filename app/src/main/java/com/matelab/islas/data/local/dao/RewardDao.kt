package com.matelab.islas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.matelab.islas.data.local.entity.BadgeUnlockEntity
import com.matelab.islas.data.local.entity.CollectibleUnlockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {

    @Query("SELECT * FROM badge_unlock ORDER BY unlocked_at DESC")
    fun observeBadgeUnlocks(): Flow<List<BadgeUnlockEntity>>

    @Query("SELECT * FROM badge_unlock")
    suspend fun badgeUnlocks(): List<BadgeUnlockEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockBadge(entry: BadgeUnlockEntity)

    @Query("SELECT COUNT(*) FROM badge_unlock")
    suspend fun badgeCount(): Int

    @Query("SELECT * FROM collectible_unlock ORDER BY unlocked_at DESC")
    fun observeCollectibleUnlocks(): Flow<List<CollectibleUnlockEntity>>

    @Query("SELECT * FROM collectible_unlock")
    suspend fun collectibleUnlocks(): List<CollectibleUnlockEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockCollectible(entry: CollectibleUnlockEntity)

    @Query("SELECT COUNT(*) FROM collectible_unlock")
    suspend fun collectibleCount(): Int

    @Query("SELECT COUNT(*) FROM collectible_unlock")
    fun observeCollectibleCount(): Flow<Int>

    @Query("DELETE FROM badge_unlock")
    suspend fun clearBadges()

    @Query("DELETE FROM collectible_unlock")
    suspend fun clearCollectibles()
}
