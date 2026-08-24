package com.matelab.islas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.matelab.islas.data.local.entity.MetaEntity
import com.matelab.islas.data.local.entity.ProfileEntity
import com.matelab.islas.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Query("SELECT * FROM profile WHERE id = 1")
    fun observeProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 1")
    suspend fun profile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: ProfileEntity)

    @Query("SELECT * FROM settings WHERE id = 1")
    fun observeSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun settings(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: SettingsEntity)

    @Query("SELECT value FROM meta WHERE key = :key")
    suspend fun meta(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putMeta(entry: MetaEntity)

    @Query("DELETE FROM profile")
    suspend fun clearProfile()
}
