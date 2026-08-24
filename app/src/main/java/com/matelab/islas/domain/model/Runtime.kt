package com.matelab.islas.domain.model

/** Resultado de un reto concreto durante una mision en curso. */
data class ChallengeResult(
    val challengeId: String,
    val correct: Boolean,
    val usedHint: Boolean,
    val elapsedMs: Long
)

/** Veredicto que devuelve un mini-juego al comprobar la respuesta. */
data class Verdict(
    val correct: Boolean,
    /** Frase corta y concreta sobre lo que ha pasado. Nunca humilla. */
    val feedback: String
)
