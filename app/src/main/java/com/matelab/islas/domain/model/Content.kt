package com.matelab.islas.domain.model

/**
 * Modelos del catalogo de contenido de MateLab.
 *
 * El catalogo describe QUE se aprende (islas, misiones, retos, recompensas).
 * El progreso del nino vive en tablas aparte; aqui no hay estado mutable.
 */

/** Cada isla del archipielago tiene su propia identidad visual. */
enum class WorldTheme { FORMAS, MEDIDA, FRACCION, NUMEROS }

/** Dificultad relativa dentro de una isla. Afecta al XP y al orden. */
enum class Difficulty(val xpMultiplier: Int) {
    EXPLORADOR(1),
    AVENTURERO(2),
    MAESTRO(3)
}

/** Familia de mini-juego que resuelve el reto. */
enum class GameKind {
    GEOBOARD,
    SHAPE_SORT,
    ANGLE_DIAL,
    SYMMETRY,
    RULER,
    BALANCE,
    CLOCK,
    FRACTION_PIE,
    FRACTION_LINE,
    PLACE_VALUE,
    PATTERN,
    QUIZ
}

/** Estado visible de una mision en el mapa. */
enum class MissionStatus {
    BLOQUEADA,
    DISPONIBLE,
    EMPEZADA,
    COMPLETADA,
    DOMINADA
}

/**
 * Una isla del archipielago. Contiene misiones y se desbloquea con XP.
 */
data class World(
    val id: String,
    val order: Int,
    val name: String,
    val subtitle: String,
    val description: String,
    val theme: WorldTheme,
    /** XP total necesario para que la isla emerja del mar. */
    val xpToUnlock: Int
)

/**
 * Una mision es una sesion corta (5-8 minutos) formada por varios retos.
 */
data class Mission(
    val id: String,
    val worldId: String,
    val order: Int,
    val name: String,
    val goal: String,
    /** Frase de Kubo al presentar la mision. */
    val briefing: String,
    val difficulty: Difficulty,
    /** Misiones que deben estar completadas antes. Vacio = disponible. */
    val requires: List<String> = emptyList(),
    /** Coleccionable que entrega al completarse por primera vez. */
    val rewardCollectibleId: String? = null
)

/**
 * Un reto concreto dentro de una mision. El [payload] describe el mini-juego.
 */
data class Challenge(
    val id: String,
    val missionId: String,
    val order: Int,
    val kind: GameKind,
    val prompt: String,
    /** Explicacion educativa que se muestra SIEMPRE, se acierte o no. */
    val explanation: String,
    val hint: String,
    val payload: ChallengePayload,
    val xp: Int = 10
)

/** Categoria de insignia, usada por [com.matelab.islas.domain.engine.BadgeEngine]. */
enum class BadgeRule {
    FIRST_MISSION,
    WORLD_COMPLETE,
    PERFECT_MISSION,
    TOTAL_STARS,
    TOTAL_XP,
    STREAK_DAYS,
    COLLECTION_SIZE,
    REVIEW_CLEARED,
    NO_HINT_MISSION,
    TOPIC_MASTER
}

/**
 * Insignia ilustrada. [threshold] y [param] los interpreta cada regla.
 */
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val rule: BadgeRule,
    val threshold: Int = 0,
    val param: String? = null,
    /** Semilla de dibujo: define emblema, forma y colores del escudo. */
    val artSeed: Int
)

/** Rareza de un cristal: cambia el brillo y el numero de caras del dibujo. */
enum class Rarity { COMUN, RARO, LEGENDARIO }

/**
 * Cristal de Ingenio: pieza coleccionable que se desbloquea con progreso real.
 */
data class Collectible(
    val id: String,
    val name: String,
    /** Dato matematico curioso que se revela al desbloquearlo. */
    val fact: String,
    val worldId: String,
    val rarity: Rarity,
    /** Semilla de dibujo: caras, inclinacion y paleta del cristal. */
    val artSeed: Int
)
