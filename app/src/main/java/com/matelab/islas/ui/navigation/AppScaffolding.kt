package com.matelab.islas.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.matelab.islas.core.di.AppContainer

/** Contenedor de dependencias accesible desde cualquier pantalla. */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer no proporcionado")
}

/**
 * Crea un ViewModel pasandole el contenedor de dependencias.
 * Evita repetir una fabrica por pantalla.
 */
@Composable
inline fun <reified VM : ViewModel> mateViewModel(
    key: String? = null,
    crossinline create: (AppContainer) -> VM
): VM {
    val container = LocalAppContainer.current
    return viewModel(
        key = key,
        factory = viewModelFactory {
            initializer { create(container) }
        }
    )
}

/**
 * Rutas de navegacion de MateLab.
 *
 * El splash no es una ruta: se muestra mientras se lee el perfil, antes de
 * crear el NavHost, para no tener que navegar fuera de el despues.
 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val PROFILE_SETUP = "perfil"
    const val MAP = "mapa"
    const val ISLAND = "isla/{worldId}"
    const val MISSION = "mision/{missionId}"
    const val COLLECTION = "coleccion"
    const val BADGES = "insignias"
    const val STATS = "estadisticas"
    const val REVIEW = "repaso"
    const val SETTINGS = "ajustes"

    fun island(worldId: String) = "isla/$worldId"
    fun mission(missionId: String) = "mision/$missionId"
}
