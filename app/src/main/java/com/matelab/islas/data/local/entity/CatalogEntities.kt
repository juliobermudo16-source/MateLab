package com.matelab.islas.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tablas del catalogo. Se rellenan con la semilla la primera vez que se abre
 * la app y se vuelven a escribir si sube la version del catalogo.
 */

@Entity(tableName = "world")
data class WorldEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    val name: String,
    val subtitle: String,
    val description: String,
    val theme: String,
    @ColumnInfo(name = "xp_to_unlock") val xpToUnlock: Int
)

@Entity(
    tableName = "mission",
    foreignKeys = [
        ForeignKey(
            entity = WorldEntity::class,
            parentColumns = ["id"],
            childColumns = ["world_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("world_id")]
)
data class MissionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "world_id") val worldId: String,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    val name: String,
    val goal: String,
    val briefing: String,
    val difficulty: String,
    /** Ids separados por coma de las misiones necesarias. Vacio si no hay. */
    val requires: String,
    @ColumnInfo(name = "reward_collectible_id") val rewardCollectibleId: String?
)

@Entity(
    tableName = "challenge",
    foreignKeys = [
        ForeignKey(
            entity = MissionEntity::class,
            parentColumns = ["id"],
            childColumns = ["mission_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mission_id")]
)
data class ChallengeEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "mission_id") val missionId: String,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    val kind: String,
    val prompt: String,
    val explanation: String,
    val hint: String,
    /** Configuracion del mini-juego serializada con kotlinx.serialization. */
    @ColumnInfo(name = "payload_json") val payloadJson: String,
    val xp: Int
)

@Entity(tableName = "badge")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val rule: String,
    val threshold: Int,
    val param: String?,
    @ColumnInfo(name = "art_seed") val artSeed: Int
)

@Entity(tableName = "collectible", indices = [Index("world_id")])
data class CollectibleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val fact: String,
    @ColumnInfo(name = "world_id") val worldId: String,
    val rarity: String,
    @ColumnInfo(name = "art_seed") val artSeed: Int
)
