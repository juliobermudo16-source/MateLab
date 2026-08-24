package com.matelab.islas.data.local

import com.matelab.islas.domain.model.ChallengePayload
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Serializacion de la configuracion de los mini-juegos.
 *
 * Se usa polimorfismo cerrado: el JSON lleva un campo "type" que identifica
 * la clase concreta de [ChallengePayload].
 */
object MateJson {

    val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        classDiscriminator = "type"
        prettyPrint = false
    }

    fun encode(payload: ChallengePayload): String =
        json.encodeToString<ChallengePayload>(payload)

    fun decode(raw: String): ChallengePayload =
        json.decodeFromString<ChallengePayload>(raw)

    /** Decodifica sin lanzar: si el JSON esta corrupto devuelve null. */
    fun decodeOrNull(raw: String): ChallengePayload? = runCatching { decode(raw) }.getOrNull()
}
