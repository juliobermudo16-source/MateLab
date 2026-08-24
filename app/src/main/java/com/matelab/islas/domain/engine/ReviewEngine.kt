package com.matelab.islas.domain.engine

import com.matelab.islas.domain.model.Challenge
import com.matelab.islas.domain.model.ReviewItem

/**
 * Motor del Taller de Repaso.
 *
 * No hace diagnostico ni etiqueta al nino: solo recoge los retos que fallo
 * y los vuelve a proponer, empezando por los que mas se le resistieron.
 */
object ReviewEngine {

    const val DEFAULT_SESSION_SIZE = 6

    /** Retos pendientes ordenados por dificultad percibida y antiguedad. */
    fun buildSession(
        items: List<ReviewItem>,
        challenges: List<Challenge>,
        size: Int = DEFAULT_SESSION_SIZE
    ): List<Challenge> {
        if (size <= 0) return emptyList()
        val byId = challenges.associateBy { it.id }
        return items
            .asSequence()
            .filter { !it.resolved }
            .sortedWith(compareByDescending<ReviewItem> { it.wrongCount }.thenBy { it.lastWrongAt })
            .mapNotNull { byId[it.challengeId] }
            .distinctBy { it.id }
            .take(size)
            .toList()
    }

    /** Un fallo nuevo suma; el mismo reto no se duplica en la lista. */
    fun register(items: List<ReviewItem>, item: ReviewItem, correct: Boolean): List<ReviewItem> {
        val existing = items.firstOrNull { it.challengeId == item.challengeId }
        return if (existing == null) {
            if (correct) items else items + item.copy(wrongCount = 1, resolved = false)
        } else {
            items.map {
                if (it.challengeId != item.challengeId) {
                    it
                } else if (correct) {
                    it.copy(resolved = true)
                } else {
                    it.copy(
                        wrongCount = it.wrongCount + 1,
                        lastWrongAt = item.lastWrongAt,
                        resolved = false
                    )
                }
            }
        }
    }

    fun pendingCount(items: List<ReviewItem>): Int = items.count { !it.resolved }

    /** Mensaje de Kubo segun cuantos retos quedan por repasar. */
    fun statusMessage(pending: Int): String = when {
        pending == 0 -> "No queda nada pendiente. El taller esta reluciente."
        pending <= 3 -> "Solo faltan $pending piezas por reparar. Es un momento."
        else -> "Hay $pending piezas esperando en el taller."
    }

    /** El repaso solo cuenta como superado si se acierta todo. */
    fun isSessionCleared(correct: Int, total: Int): Boolean = total > 0 && correct == total
}
