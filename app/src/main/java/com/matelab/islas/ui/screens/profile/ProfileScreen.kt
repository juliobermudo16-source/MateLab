package com.matelab.islas.ui.screens.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matelab.islas.core.di.AppContainer
import com.matelab.islas.data.repository.PlayerRepositoryImpl
import com.matelab.islas.domain.repository.PlayerRepository
import com.matelab.islas.ui.art.AvatarArt
import com.matelab.islas.ui.art.AvatarNames
import com.matelab.islas.ui.art.SeaBackdrop
import com.matelab.islas.ui.components.KuboSays
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.MatePanel
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.navigation.mateViewModel
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.Teal
import kotlinx.coroutines.launch

class ProfileViewModel(container: AppContainer) : ViewModel() {
    private val player: PlayerRepository = container.playerRepository

    fun save(alias: String, avatarId: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            player.completeProfile(alias, avatarId)
            onDone()
        }
    }
}

/**
 * Eleccion de apodo y ayudante.
 *
 * Nunca se pide el nombre real, ni edad, ni correo: solo un apodo corto que
 * se guarda en el propio movil.
 */
@Composable
fun ProfileSetupScreen(onDone: () -> Unit) {
    val viewModel: ProfileViewModel = mateViewModel { ProfileViewModel(it) }
    val feedback = rememberUiFeedback()
    val scroll = rememberScrollState()

    var alias by remember { mutableStateOf("") }
    var avatar by remember { mutableIntStateOf(0) }

    SeaBackdrop(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(20.dp)
        ) {
            Text(
                "Tu equipo",
                style = MaterialTheme.typography.displayMedium,
                color = MateTheme.colors.ink
            )
            Spacer(Modifier.height(8.dp))
            KuboSays("Elige un apodo y un ayudante. No hace falta tu nombre real.")

            Spacer(Modifier.height(18.dp))

            MatePanel(contentPadding = 16.dp) {
                Column {
                    Text(
                        "Apodo de explorador",
                        style = MaterialTheme.typography.titleLarge,
                        color = MateTheme.colors.ink
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = alias,
                        onValueChange = {
                            alias = PlayerRepositoryImpl.sanitizeAlias(it)
                        },
                        singleLine = true,
                        placeholder = { Text("Por ejemplo: Nova") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${alias.length} de ${PlayerRepositoryImpl.MAX_ALIAS_LENGTH} letras",
                        style = MaterialTheme.typography.labelSmall,
                        color = MateTheme.colors.inkSoft
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "Elige tu ayudante",
                style = MaterialTheme.typography.headlineSmall,
                color = MateTheme.colors.ink
            )
            Spacer(Modifier.height(10.dp))

            AvatarNames.indices.chunked(4).forEach { row ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { id ->
                        val selected = avatar == id
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (selected) Teal.copy(alpha = 0.18f) else MateTheme.colors.card
                                )
                                .border(
                                    width = if (selected) 3.dp else 1.5.dp,
                                    color = if (selected) Teal else MateTheme.colors.outline,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    avatar = id
                                    feedback.tap()
                                }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AvatarArt(avatarId = id, size = 54.dp, background = false)
                            Text(
                                AvatarNames[id],
                                style = MaterialTheme.typography.labelSmall,
                                color = MateTheme.colors.ink,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    if (row.size < 4) {
                        repeat(4 - row.size) { Box(Modifier.weight(1f)) }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(10.dp))

            MateButton(
                text = "Zarpar",
                onClick = {
                    feedback.unlock()
                    viewModel.save(alias.ifBlank { AvatarNames[avatar] }, avatar, onDone)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
