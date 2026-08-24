package com.matelab.islas.core.di

import android.content.Context
import com.matelab.islas.data.local.MateLabDatabase
import com.matelab.islas.data.repository.CatalogRepositoryImpl
import com.matelab.islas.data.repository.PlayerRepositoryImpl
import com.matelab.islas.data.repository.ProgressRepositoryImpl
import com.matelab.islas.domain.repository.CatalogRepository
import com.matelab.islas.domain.repository.PlayerRepository
import com.matelab.islas.domain.repository.ProgressRepository
import com.matelab.islas.ui.audio.SoundManager

/**
 * Inyeccion de dependencias manual.
 *
 * El proyecto no usa Hilt a proposito: con tres repositorios y un gestor de
 * sonido, un contenedor explicito es mas facil de leer y de compilar.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val database: MateLabDatabase by lazy { MateLabDatabase.get(appContext) }

    val catalogRepository: CatalogRepository by lazy {
        CatalogRepositoryImpl(database.catalogDao())
    }

    val playerRepository: PlayerRepository by lazy {
        PlayerRepositoryImpl(database.playerDao(), database.rewardDao())
    }

    val progressRepository: ProgressRepository by lazy {
        ProgressRepositoryImpl(
            progressDao = database.progressDao(),
            catalogDao = database.catalogDao(),
            rewardDao = database.rewardDao(),
            playerDao = database.playerDao()
        )
    }

    val soundManager: SoundManager by lazy { SoundManager(appContext) }
}
