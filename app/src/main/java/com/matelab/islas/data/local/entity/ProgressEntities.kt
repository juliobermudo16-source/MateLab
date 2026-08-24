package com.matelab.islas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tablas de estado del jugador. Todo es local y anonimo: no hay ningun campo
 * que permita identificar a una persona real.
 */

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1,
    /** Apodo elegido por el nino. Nunca se pide el nombre real. */
    val alias: String = "",
    @ColumnInfo(name = "avatar_id") val avatarId: Int = 0,
    val xp: Int = 0,
    @ColumnInfo(name = "streak_days") val streakDays: Int = 1,
    @ColumnInfo(name = "last_played_day") val lastPlayedDay: Long = 0L,
    @ColumnInfo(name = "onboarding_done") val onboardingDone: Boolean = false,
    @ColumnInfo(name = "profile_done") val profileDone: Boolean = false,
    @ColumnInfo(name = "review_sessions_cleared") val reviewSessionsCleared: Int = 0
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "sound_enabled") val soundEnabled: Boolean = true,
    @ColumnInfo(name = "haptics_enabled") val hapticsEnabled: Boolean = true,
    @ColumnInfo(name = "animations_enabled") val animationsEnabled: Boolean = true,
    @ColumnInfo(name = "big_text_enabled") val bigTextEnabled: Boolean = false
)

@Entity(tableName = "mission_progress")
data class MissionProgressEntity(
    @PrimaryKey @ColumnInfo(name = "mission_id") val missionId: String,
    val status: String,
    val stars: Int,
    @ColumnInfo(name = "best_percent") val bestPercent: Int,
    @ColumnInfo(name = "times_played") val timesPlayed: Int,
    @ColumnInfo(name = "no_hint_run") val noHintRun: Boolean = false,
    @ColumnInfo(name = "last_played_at") val lastPlayedAt: Long
)

@Entity(
    tableName = "attempt",
    indices = [Index("mission_id"), Index("world_id"), Index("timestamp")]
)
data class AttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "challenge_id") val challengeId: String,
    @ColumnInfo(name = "mission_id") val missionId: String,
    @ColumnInfo(name = "world_id") val worldId: String,
    val correct: Boolean,
    @ColumnInfo(name = "used_hint") val usedHint: Boolean,
    @ColumnInfo(name = "elapsed_ms") val elapsedMs: Long,
    val timestamp: Long
)

@Entity(tableName = "badge_unlock")
data class BadgeUnlockEntity(
    @PrimaryKey @ColumnInfo(name = "badge_id") val badgeId: String,
    @ColumnInfo(name = "unlocked_at") val unlockedAt: Long
)

@Entity(tableName = "collectible_unlock")
data class CollectibleUnlockEntity(
    @PrimaryKey @ColumnInfo(name = "collectible_id") val collectibleId: String,
    @ColumnInfo(name = "unlocked_at") val unlockedAt: Long
)

@Entity(tableName = "review_item")
data class ReviewItemEntity(
    @PrimaryKey @ColumnInfo(name = "challenge_id") val challengeId: String,
    @ColumnInfo(name = "mission_id") val missionId: String,
    @ColumnInfo(name = "world_id") val worldId: String,
    @ColumnInfo(name = "wrong_count") val wrongCount: Int,
    @ColumnInfo(name = "last_wrong_at") val lastWrongAt: Long,
    val resolved: Boolean
)

/** Pares clave/valor internos: version del catalogo, contadores, etc. */
@Entity(tableName = "meta")
data class MetaEntity(
    @PrimaryKey val key: String,
    val value: String
)
