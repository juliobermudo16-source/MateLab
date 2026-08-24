package com.matelab.islas.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matelab.islas.core.di.AppContainer
import com.matelab.islas.domain.model.Profile
import com.matelab.islas.domain.model.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class RootUiState(
    val loading: Boolean = true,
    val profile: Profile = Profile(),
    val settings: Settings = Settings()
) {
    /** Ruta inicial: onboarding, creacion de perfil o mapa. */
    val startRoute: String
        get() = when {
            !profile.onboardingDone -> "onboarding"
            !profile.profileDone -> "perfil"
            else -> "mapa"
        }
}

/**
 * Estado global minimo: perfil y ajustes.
 * Decide la primera pantalla y alimenta el tema y el sonido.
 */
class RootViewModel(container: AppContainer) : ViewModel() {

    val state: StateFlow<RootUiState> = combine(
        container.playerRepository.observeProfile(),
        container.playerRepository.observeSettings()
    ) { profile, settings ->
        container.soundManager.enabled = settings.soundEnabled
        RootUiState(loading = false, profile = profile, settings = settings)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, RootUiState())

    // El SoundManager NO se libera aqui a proposito: vive en el AppContainer,
    // que dura lo que el proceso. Si se liberase al destruirse la Activity, al
    // volver a abrirla el pool seguiria descargado y la app se quedaria muda.
}
