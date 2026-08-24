package com.matelab.islas.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.matelab.islas.ui.audio.Sfx
import com.matelab.islas.ui.audio.SoundManager

val LocalSoundManager = staticCompositionLocalOf<SoundManager?> { null }
val LocalHapticsEnabled = staticCompositionLocalOf { true }

/**
 * Une sonido y vibracion en un unico punto.
 * Las dos cosas son opcionales y se apagan desde los ajustes.
 */
class UiFeedback(
    private val sound: SoundManager?,
    private val hapticsEnabled: Boolean,
    private val haptics: HapticFeedback
) {
    fun tap() {
        sound?.play(Sfx.TAP, 0.5f)
        buzz(HapticFeedbackType.TextHandleMove)
    }

    fun correct() {
        sound?.play(Sfx.CORRECT)
        buzz(HapticFeedbackType.LongPress)
    }

    fun wrong() {
        sound?.play(Sfx.WRONG, 0.6f)
        buzz(HapticFeedbackType.TextHandleMove)
    }

    fun star() {
        sound?.play(Sfx.STAR)
        buzz(HapticFeedbackType.LongPress)
    }

    fun unlock() {
        sound?.play(Sfx.UNLOCK)
        buzz(HapticFeedbackType.LongPress)
    }

    fun levelUp() {
        sound?.play(Sfx.LEVEL)
        buzz(HapticFeedbackType.LongPress)
    }

    private fun buzz(type: HapticFeedbackType) {
        if (hapticsEnabled) runCatching { haptics.performHapticFeedback(type) }
    }
}

@Composable
fun rememberUiFeedback(): UiFeedback {
    val sound = LocalSoundManager.current
    val hapticsEnabled = LocalHapticsEnabled.current
    val haptics = LocalHapticFeedback.current
    return remember(sound, hapticsEnabled, haptics) {
        UiFeedback(sound, hapticsEnabled, haptics)
    }
}
