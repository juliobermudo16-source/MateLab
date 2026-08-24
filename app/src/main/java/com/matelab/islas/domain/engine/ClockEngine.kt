package com.matelab.islas.domain.engine

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

/** Hora de reloj analogico de 12 horas. */
data class ClockTime(val hour: Int, val minute: Int) {

    init {
        require(hour in 0..23) { "Hora fuera de rango: $hour" }
        require(minute in 0..59) { "Minuto fuera de rango: $minute" }
    }

    /** 0 se muestra como 12 en un reloj analogico. */
    val displayHour: Int get() = if (hour % 12 == 0) 12 else hour % 12

    fun label(): String = "%d:%02d".format(displayHour, minute)

    /** Minutos transcurridos desde las 12:00. */
    fun asMinutes(): Int = (hour % 12) * 60 + minute
}

/**
 * Motor del reloj de arrastre de Puerto Medida.
 * Convierte angulos de manecillas en horas y calcula duraciones.
 */
object ClockEngine {

    const val MINUTE_DEGREES = 6.0   // 360 / 60
    const val HOUR_DEGREES = 30.0    // 360 / 12

    /** Angulo del minutero (0 grados = las 12, sentido horario). */
    fun minuteHandAngle(minute: Int): Double = AngleEngine.normalize(minute * MINUTE_DEGREES)

    /** Angulo de la aguja horaria, que avanza medio grado por minuto. */
    fun hourHandAngle(hour: Int, minute: Int): Double =
        AngleEngine.normalize((hour % 12) * HOUR_DEGREES + minute * 0.5)

    /** Minuto mas cercano al angulo arrastrado. */
    fun minuteFromAngle(degrees: Double): Int =
        (round(AngleEngine.normalize(degrees) / MINUTE_DEGREES).toInt()) % 60

    /** Hora (0..11) que corresponde al angulo arrastrado. */
    fun hourFromAngle(degrees: Double): Int =
        (floor(AngleEngine.normalize(degrees) / HOUR_DEGREES).toInt()) % 12

    /** Construye la hora a partir de los dos angulos de las manecillas. */
    fun timeFromAngles(hourAngle: Double, minuteAngle: Double): ClockTime {
        val minute = minuteFromAngle(minuteAngle)
        val hour = hourFromAngle(hourAngle)
        return ClockTime(hour, minute)
    }

    fun addMinutes(time: ClockTime, delta: Int): ClockTime {
        var total = (time.hour % 12) * 60 + time.minute + delta
        total %= (12 * 60)
        if (total < 0) total += 12 * 60
        return ClockTime(total / 60, total % 60)
    }

    /** Minutos que van de [from] a [to] dentro de la misma vuelta de 12 h. */
    fun durationMinutes(from: ClockTime, to: ClockTime): Int {
        var diff = to.asMinutes() - from.asMinutes()
        if (diff < 0) diff += 12 * 60
        return diff
    }

    /** Compara horas ignorando am/pm y admitiendo un margen de minutos. */
    fun matches(actual: ClockTime, expected: ClockTime, toleranceMinutes: Int = 0): Boolean {
        if (actual.hour % 12 != expected.hour % 12) return false
        val diff = abs(actual.minute - expected.minute)
        return minOf(diff, 60 - diff) <= toleranceMinutes
    }

    /** "1 h 25 min", "45 min", "2 h". */
    fun durationLabel(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0 -> "$m min"
            m == 0 -> "$h h"
            else -> "$h h $m min"
        }
    }
}
