package com.matelab.islas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.matelab.islas.data.local.entity.BadgeEntity
import com.matelab.islas.data.local.entity.ChallengeEntity
import com.matelab.islas.data.local.entity.CollectibleEntity
import com.matelab.islas.data.local.entity.MissionEntity
import com.matelab.islas.data.local.entity.WorldEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {

    @Query("SELECT * FROM world ORDER BY order_index")
    fun observeWorlds(): Flow<List<WorldEntity>>

    @Query("SELECT * FROM world ORDER BY order_index")
    suspend fun worlds(): List<WorldEntity>

    @Query("SELECT * FROM mission ORDER BY order_index")
    fun observeMissions(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM mission ORDER BY order_index")
    suspend fun missions(): List<MissionEntity>

    @Query("SELECT * FROM mission WHERE world_id = :worldId ORDER BY order_index")
    suspend fun missionsOf(worldId: String): List<MissionEntity>

    @Query("SELECT * FROM mission WHERE id = :missionId")
    suspend fun mission(missionId: String): MissionEntity?

    @Query("SELECT * FROM challenge WHERE mission_id = :missionId ORDER BY order_index")
    suspend fun challengesOf(missionId: String): List<ChallengeEntity>

    @Query("SELECT * FROM challenge WHERE id IN (:ids)")
    suspend fun challengesByIds(ids: List<String>): List<ChallengeEntity>

    @Query("SELECT * FROM challenge")
    suspend fun allChallenges(): List<ChallengeEntity>

    @Query("SELECT * FROM badge")
    fun observeBadges(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM badge")
    suspend fun badges(): List<BadgeEntity>

    @Query("SELECT * FROM collectible")
    fun observeCollectibles(): Flow<List<CollectibleEntity>>

    @Query("SELECT * FROM collectible")
    suspend fun collectibles(): List<CollectibleEntity>

    @Query("SELECT COUNT(*) FROM world")
    suspend fun worldCount(): Int

    // ------------------------------------------------------------- semilla

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorlds(items: List<WorldEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(items: List<MissionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(items: List<ChallengeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(items: List<BadgeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectibles(items: List<CollectibleEntity>)

    @Query("DELETE FROM challenge")
    suspend fun clearChallenges()

    @Query("DELETE FROM mission")
    suspend fun clearMissions()

    @Query("DELETE FROM world")
    suspend fun clearWorlds()

    @Query("DELETE FROM badge")
    suspend fun clearBadges()

    @Query("DELETE FROM collectible")
    suspend fun clearCollectibles()
}
