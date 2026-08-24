package com.matelab.islas.data.local.seed

import androidx.sqlite.db.SupportSQLiteDatabase
import com.matelab.islas.data.local.MateJson
import com.matelab.islas.domain.content.Catalog

/**
 * Vuelca el catalogo de contenido en la base de datos.
 *
 * Se ejecuta con SQL directo dentro del callback de Room para garantizar que,
 * cuando la primera pantalla consulta la base, el contenido ya esta escrito.
 */
object DatabaseSeeder {

    const val META_CATALOG_VERSION = "catalog_version"

    /** Escribe (o reescribe) el catalogo completo. No toca el progreso. */
    fun seedCatalog(db: SupportSQLiteDatabase) {
        // El borrado en cascada de world limpia mission y challenge.
        db.execSQL("DELETE FROM challenge")
        db.execSQL("DELETE FROM mission")
        db.execSQL("DELETE FROM world")
        db.execSQL("DELETE FROM badge")
        db.execSQL("DELETE FROM collectible")

        Catalog.worlds.forEach { w ->
            db.execSQL(
                "INSERT INTO world (id, order_index, name, subtitle, description, theme, xp_to_unlock) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>(w.id, w.order, w.name, w.subtitle, w.description, w.theme.name, w.xpToUnlock)
            )
        }

        Catalog.missions.forEach { m ->
            db.execSQL(
                "INSERT INTO mission (id, world_id, order_index, name, goal, briefing, difficulty, requires, reward_collectible_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>(
                    m.id, m.worldId, m.order, m.name, m.goal, m.briefing,
                    m.difficulty.name, m.requires.joinToString(","), m.rewardCollectibleId
                )
            )
        }

        Catalog.challenges.forEach { c ->
            db.execSQL(
                "INSERT INTO challenge (id, mission_id, order_index, kind, prompt, explanation, hint, payload_json, xp) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>(
                    c.id, c.missionId, c.order, c.kind.name, c.prompt,
                    c.explanation, c.hint, MateJson.encode(c.payload), c.xp
                )
            )
        }

        Catalog.badges.forEach { b ->
            db.execSQL(
                "INSERT INTO badge (id, name, description, rule, threshold, param, art_seed) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>(b.id, b.name, b.description, b.rule.name, b.threshold, b.param, b.artSeed)
            )
        }

        Catalog.collectibles.forEach { c ->
            db.execSQL(
                "INSERT INTO collectible (id, name, fact, world_id, rarity, art_seed) " +
                    "VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf<Any?>(c.id, c.name, c.fact, c.worldId, c.rarity.name, c.artSeed)
            )
        }

        db.execSQL(
            "INSERT OR REPLACE INTO meta (key, value) VALUES (?, ?)",
            arrayOf<Any?>(META_CATALOG_VERSION, Catalog.VERSION.toString())
        )
    }

    /** Crea el perfil y los ajustes por defecto si aun no existen. */
    fun seedPlayerDefaults(db: SupportSQLiteDatabase) {
        db.execSQL(
            "INSERT OR IGNORE INTO profile " +
                "(id, alias, avatar_id, xp, streak_days, last_played_day, onboarding_done, profile_done, review_sessions_cleared) " +
                "VALUES (1, '', 0, 0, 1, 0, 0, 0, 0)"
        )
        db.execSQL(
            "INSERT OR IGNORE INTO settings " +
                "(id, sound_enabled, haptics_enabled, animations_enabled, big_text_enabled) " +
                "VALUES (1, 1, 1, 1, 0)"
        )
    }

    /** True si la base todavia no tiene el catalogo o esta desactualizado. */
    fun needsCatalogSeed(db: SupportSQLiteDatabase): Boolean {
        var stored: String? = null
        db.query("SELECT value FROM meta WHERE key = '$META_CATALOG_VERSION'").use { cursor ->
            if (cursor.moveToFirst()) stored = cursor.getString(0)
        }
        if (stored != Catalog.VERSION.toString()) return true

        var worlds = 0
        db.query("SELECT COUNT(*) FROM world").use { cursor ->
            if (cursor.moveToFirst()) worlds = cursor.getInt(0)
        }
        return worlds == 0
    }
}
