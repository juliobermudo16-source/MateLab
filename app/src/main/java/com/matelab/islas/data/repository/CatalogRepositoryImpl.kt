package com.matelab.islas.data.repository

import com.matelab.islas.data.local.dao.CatalogDao
import com.matelab.islas.data.local.toDomain
import com.matelab.islas.domain.model.Badge
import com.matelab.islas.domain.model.Challenge
import com.matelab.islas.domain.model.Collectible
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.World
import com.matelab.islas.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CatalogRepositoryImpl(
    private val dao: CatalogDao
) : CatalogRepository {

    override fun observeWorlds(): Flow<List<World>> =
        dao.observeWorlds().map { list -> list.map { it.toDomain() } }

    override fun observeMissions(): Flow<List<Mission>> =
        dao.observeMissions().map { list -> list.map { it.toDomain() } }

    override suspend fun worlds(): List<World> = dao.worlds().map { it.toDomain() }

    override suspend fun missions(): List<Mission> = dao.missions().map { it.toDomain() }

    override suspend fun missionsOf(worldId: String): List<Mission> =
        dao.missionsOf(worldId).map { it.toDomain() }

    override suspend fun mission(missionId: String): Mission? =
        dao.mission(missionId)?.toDomain()

    override suspend fun challengesOf(missionId: String): List<Challenge> =
        dao.challengesOf(missionId).map { it.toDomain() }

    override suspend fun challengesByIds(ids: List<String>): List<Challenge> {
        if (ids.isEmpty()) return emptyList()
        val found = dao.challengesByIds(ids).associateBy { it.id }
        // Se respeta el orden pedido, que en el repaso importa.
        return ids.mapNotNull { found[it]?.toDomain() }
    }

    override suspend fun badges(): List<Badge> = dao.badges().map { it.toDomain() }

    override suspend fun collectibles(): List<Collectible> = dao.collectibles().map { it.toDomain() }
}
