package com.matelab.islas.domain.content

import com.matelab.islas.domain.model.Badge
import com.matelab.islas.domain.model.Challenge
import com.matelab.islas.domain.model.Collectible
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.World

/**
 * Catalogo maestro de MateLab.
 *
 * Es la fuente de la semilla que se vuelca en Room la primera vez que se
 * abre la app. A partir de ahi, la aplicacion lee el contenido de la base
 * de datos, no de estas listas.
 */
object Catalog {

    val worlds: List<World> = listOf(
        CatalogFormas.world,
        CatalogMedida.world,
        CatalogFraccion.world,
        CatalogNumeros.world
    ).sortedBy { it.order }

    val missions: List<Mission> =
        CatalogFormas.missions +
            CatalogMedida.missions +
            CatalogFraccion.missions +
            CatalogNumeros.missions

    val challenges: List<Challenge> =
        CatalogFormas.challenges +
            CatalogMedida.challenges +
            CatalogFraccion.challenges +
            CatalogNumeros.challenges

    val badges: List<Badge> = CatalogRewards.badges

    val collectibles: List<Collectible> = CatalogRewards.collectibles

    // ------------------------------------------------------------- consultas

    fun world(id: String): World? = worlds.firstOrNull { it.id == id }

    fun mission(id: String): Mission? = missions.firstOrNull { it.id == id }

    fun missionsOf(worldId: String): List<Mission> =
        missions.filter { it.worldId == worldId }.sortedBy { it.order }

    fun challengesOf(missionId: String): List<Challenge> =
        challenges.filter { it.missionId == missionId }.sortedBy { it.order }

    fun worldIdOfMission(missionId: String): String? = mission(missionId)?.worldId

    fun worldIdOfChallenge(challengeId: String): String? =
        challenges.firstOrNull { it.id == challengeId }
            ?.let { worldIdOfMission(it.missionId) }

    /** Version del catalogo. Al subirla, la semilla se vuelve a escribir. */
    const val VERSION = 1

    // --------------------------------------------------------- verificaciones

    /**
     * Comprobaciones de integridad del contenido. Las usan las pruebas
     * unitarias para que un error de datos no llegue nunca al nino.
     */
    fun integrityProblems(): List<String> {
        val problems = mutableListOf<String>()

        val worldIds = worlds.map { it.id }
        if (worldIds.size != worldIds.toSet().size) problems += "Islas con id repetido"

        val missionIds = missions.map { it.id }
        if (missionIds.size != missionIds.toSet().size) problems += "Misiones con id repetido"

        val challengeIds = challenges.map { it.id }
        if (challengeIds.size != challengeIds.toSet().size) problems += "Retos con id repetido"

        missions.forEach { mission ->
            if (mission.worldId !in worldIds) {
                problems += "La mision ${mission.id} apunta a una isla inexistente"
            }
            mission.requires.forEach { req ->
                if (req !in missionIds) problems += "La mision ${mission.id} requiere ${req}, que no existe"
            }
            val own = challengesOf(mission.id)
            if (own.size < 4) problems += "La mision ${mission.id} tiene menos de 4 retos"
            mission.rewardCollectibleId?.let { reward ->
                if (collectibles.none { it.id == reward }) {
                    problems += "La mision ${mission.id} premia un cristal inexistente"
                }
            }
        }

        challenges.forEach { challenge ->
            if (challenge.missionId !in missionIds) {
                problems += "El reto ${challenge.id} apunta a una mision inexistente"
            }
            if (challenge.prompt.isBlank()) problems += "El reto ${challenge.id} no tiene enunciado"
            if (challenge.explanation.isBlank()) problems += "El reto ${challenge.id} no explica el porque"
            if (challenge.hint.isBlank()) problems += "El reto ${challenge.id} no tiene pista"
        }

        badges.forEach { badge ->
            if (badge.param != null && badge.param !in worldIds && badge.rule.name.contains("WORLD")) {
                problems += "La insignia ${badge.id} apunta a una isla inexistente"
            }
        }

        return problems
    }
}
