package com.matelab.islas.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matelab.islas.core.di.AppContainer
import com.matelab.islas.domain.model.Profile
import com.matelab.islas.domain.model.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.matelab.islas.ui.art.AvatarArt
import com.matelab.islas.ui.art.AvatarNames
import com.matelab.islas.ui.art.SeaBackdrop
import com.matelab.islas.ui.components.GhostButton
import com.matelab.islas.ui.components.KuboSays
import com.matelab.islas.ui.components.MatePanel
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.navigation.mateViewModel
import com.matelab.islas.ui.screens.collection.Header
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Teal

data class SettingsUiState(
    val settings: Settings = Settings(),
    val profile: Profile = Profile()
)

class SettingsViewModel(container: AppContainer) : ViewModel() {

    private val player = container.playerRepository
    private val progress = container.progressRepository

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state

    init {
        viewModelScope.launch {
            player.observeSettings().collect { s -> _state.update { it.copy(settings = s) } }
        }
        viewModelScope.launch {
            player.observeProfile().collect { p -> _state.update { it.copy(profile = p) } }
        }
    }

    fun update(settings: Settings) {
        viewModelScope.launch { player.updateSettings(settings) }
    }

    fun setAvatar(id: Int) {
        viewModelScope.launch { player.saveAvatar(id) }
    }

    fun resetProgress() {
        viewModelScope.launch { progress.resetProgress() }
    }
}

/**
 * Cabina de Kubo: sonido, vibracion, accesibilidad, avatar y privacidad.
 */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val viewModel: SettingsViewModel = mateViewModel { SettingsViewModel(it) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val feedback = rememberUiFeedback()
    val scroll = rememberScrollState()
    var confirmReset by remember { mutableStateOf(false) }

    SeaBackdrop(Modifier.fillMaxSize(), showHorizon = false) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp)
        ) {
            Header(title = "Ajustes", onBack = onBack)
            Spacer(Modifier.height(14.dp))

            MatePanel(contentPadding = 8.dp) {
                Column {
                    SettingRow(
                        title = "Sonidos",
                        subtitle = "Efectos cortos al acertar y desbloquear",
                        checked = state.settings.soundEnabled,
                        onChange = {
                            viewModel.update(state.settings.copy(soundEnabled = it))
                        }
                    )
                    SettingRow(
                        title = "Vibracion",
                        subtitle = "Un toque suave en cada accion",
                        checked = state.settings.hapticsEnabled,
                        onChange = {
                            viewModel.update(state.settings.copy(hapticsEnabled = it))
                        }
                    )
                    SettingRow(
                        title = "Animaciones",
                        subtitle = "Desactivalas si te distraen o marean",
                        checked = state.settings.animationsEnabled,
                        onChange = {
                            viewModel.update(state.settings.copy(animationsEnabled = it))
                        }
                    )
                    SettingRow(
                        title = "Texto grande",
                        subtitle = "Aumenta el tamano de las letras",
                        checked = state.settings.bigTextEnabled,
                        onChange = {
                            viewModel.update(state.settings.copy(bigTextEnabled = it))
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Tu ayudante",
                style = MaterialTheme.typography.headlineSmall,
                color = MateTheme.colors.ink
            )
            Spacer(Modifier.height(8.dp))
            AvatarNames.indices.chunked(4).forEach { row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { id ->
                        val selected = state.profile.avatarId == id
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (selected) Teal.copy(alpha = 0.16f) else MateTheme.colors.card)
                                .border(
                                    width = if (selected) 3.dp else 1.5.dp,
                                    color = if (selected) Teal else MateTheme.colors.outline,
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .clickable {
                                    feedback.tap()
                                    viewModel.setAvatar(id)
                                }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AvatarArt(avatarId = id, size = 46.dp, background = false)
                            Text(
                                AvatarNames[id],
                                style = MaterialTheme.typography.labelSmall,
                                color = MateTheme.colors.ink,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    if (row.size < 4) repeat(4 - row.size) { Box(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(10.dp))

            MatePanel(contentPadding = 16.dp) {
                Column {
                    Text(
                        "Privacidad",
                        style = MaterialTheme.typography.titleLarge,
                        color = MateTheme.colors.ink
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "MateLab no pide permisos, no usa internet y no envia nada a ningun " +
                            "sitio. El apodo, el avatar y el progreso se guardan solo en este movil " +
                            "y se borran al desinstalar la app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MateTheme.colors.inkSoft
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            GhostButton(
                text = "Reiniciar mi progreso",
                onClick = { confirmReset = true },
                color = MateTheme.colors.warning,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            KuboSays("Si reinicias el progreso, las islas vuelven a empezar. El contenido no se borra.")

            Spacer(Modifier.height(16.dp))
            Text(
                "MateLab 1.0.0 - Islas del Ingenio",
                style = MaterialTheme.typography.labelSmall,
                color = MateTheme.colors.inkSoft,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reiniciar el progreso?") },
            text = {
                Text(
                    "Se borraran las estrellas, la experiencia, los cristales y las insignias. " +
                        "Esta accion no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetProgress()
                    confirmReset = false
                }) { Text("Si, reiniciar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MateTheme.colors.ink)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MateTheme.colors.inkSoft)
        }
        Spacer(Modifier.width(10.dp))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
