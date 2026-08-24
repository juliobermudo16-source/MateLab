package com.matelab.islas.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.matelab.islas.R

/** Efectos disponibles. Todos son ficheros WAV locales, cortos y suaves. */
enum class Sfx(val resId: Int) {
    TAP(R.raw.sfx_tap),
    CORRECT(R.raw.sfx_correct),
    WRONG(R.raw.sfx_wrong),
    STAR(R.raw.sfx_star),
    UNLOCK(R.raw.sfx_unlock),
    LEVEL(R.raw.sfx_level)
}

/**
 * Reproductor de efectos con SoundPool.
 *
 * El sonido nunca es obligatorio: si el nino lo desactiva en los ajustes,
 * [play] no hace nada. Tampoco se reproduce nada al abrir la app.
 */
class SoundManager(context: Context) {

    private val appContext = context.applicationContext

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids = mutableMapOf<Sfx, Int>()
    private var loaded = false

    @Volatile
    var enabled: Boolean = true

    init {
        runCatching {
            Sfx.entries.forEach { sfx ->
                ids[sfx] = pool.load(appContext, sfx.resId, 1)
            }
            loaded = true
        }
    }

    fun play(sfx: Sfx, volume: Float = 0.7f) {
        if (!enabled || !loaded) return
        val id = ids[sfx] ?: return
        runCatching { pool.play(id, volume, volume, 1, 0, 1f) }
    }

    fun release() {
        runCatching { pool.release() }
        loaded = false
    }
}
