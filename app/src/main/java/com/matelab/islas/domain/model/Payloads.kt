package com.matelab.islas.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Configuracion concreta de cada mini-juego.
 *
 * Se guarda en la tabla `challenge` como JSON (kotlinx.serialization,
 * polimorfismo cerrado con discriminador "type"), asi que anadir un
 * mini-juego nuevo no obliga a migrar el esquema.
 */
@Serializable
sealed class ChallengePayload

// ---------------------------------------------------------------- GEOPLANO

@Serializable
enum class GeoObjective {
    /** Construir un poligono cuya area sea exactamente el objetivo. */
    AREA,

    /** Construir un poligono cuyo perimetro sea exactamente el objetivo. */
    PERIMETRO,

    /** Construir un poligono con un numero exacto de lados. */
    LADOS
}

@Serializable
@SerialName("geoboard")
data class GeoboardPayload(
    val grid: Int = 6,
    val objective: GeoObjective,
    val target: Double,
    val minVertices: Int = 3,
    /** Texto de la unidad ("cuadraditos", "unidades"). */
    val unitLabel: String = "cuadraditos"
) : ChallengePayload()

// ------------------------------------------------------- CLASIFICAR FIGURAS

@Serializable
enum class ShapeCriterion { NUM_LADOS, TIENE_ANGULO_RECTO, LADOS_IGUALES, ES_CURVA }

@Serializable
data class ShapeSpec(
    val id: String,
    val name: String,
    val sides: Int,
    val allSidesEqual: Boolean,
    val rightAngles: Int,
    val curved: Boolean = false,
    /** Rotacion en grados con la que se dibuja (evita que todas se vean igual). */
    val rotation: Int = 0
)

@Serializable
data class BucketSpec(
    val id: String,
    val label: String,
    val criterion: ShapeCriterion,
    val value: Int = 0
)

@Serializable
@SerialName("shape_sort")
data class ShapeSortPayload(
    val shapes: List<ShapeSpec>,
    val buckets: List<BucketSpec>
) : ChallengePayload()

// ----------------------------------------------------------------- ANGULOS

@Serializable
@SerialName("angle_dial")
data class AngleDialPayload(
    val targetDegrees: Int,
    val tolerance: Int = 5,
    /** Si es true, ademas hay que decir si es agudo, recto u obtuso. */
    val askClassification: Boolean = false,
    val showProtractor: Boolean = true
) : ChallengePayload()

// ---------------------------------------------------------------- SIMETRIA

@Serializable
enum class SymmetryAxis { VERTICAL, HORIZONTAL }

@Serializable
@SerialName("symmetry")
data class SymmetryPayload(
    val rows: Int,
    val cols: Int,
    val axis: SymmetryAxis,
    /** Indices (fila * cols + columna) ya pintados en la mitad de partida. */
    val given: List<Int>
) : ChallengePayload()

// ------------------------------------------------------------------- REGLA

@Serializable
@SerialName("ruler")
data class RulerPayload(
    /** Longitud real del objeto en milimetros. */
    val objectMm: Int,
    /** Tolerancia admitida al leer, en milimetros. */
    val toleranceMm: Int = 2,
    /** Unidad en la que se responde: "cm" o "mm". */
    val answerUnit: String = "cm",
    /** Que objeto se dibuja: lapiz, cinta, hoja, gusano, llave, clip. */
    val objectKind: String = "lapiz"
) : ChallengePayload()

// ----------------------------------------------------------------- BALANZA

@Serializable
@SerialName("balance")
data class BalancePayload(
    /** Masa del objeto misterioso del plato izquierdo, en gramos. */
    val leftGrams: Int,
    val leftLabel: String,
    /** Pesas disponibles en la bandeja, en gramos. */
    val weights: List<Int>,
    /** Cuantas veces puede repetirse cada pesa. */
    val maxPerWeight: Int = 3
) : ChallengePayload()

// ------------------------------------------------------------------- RELOJ

@Serializable
enum class ClockMode {
    /** Colocar las manecillas en la hora pedida. */
    PONER_HORA,

    /** Adelantar el reloj los minutos indicados. */
    AVANZAR
}

@Serializable
@SerialName("clock")
data class ClockPayload(
    val startHour: Int,
    val startMinute: Int,
    val mode: ClockMode,
    /** Minutos a avanzar cuando el modo es AVANZAR. */
    val deltaMinutes: Int = 0,
    /** Hora objetivo cuando el modo es PONER_HORA. */
    val targetHour: Int = 0,
    val targetMinute: Int = 0
) : ChallengePayload()

// --------------------------------------------------------------- FRACCIONES

@Serializable
enum class PieShape { CIRCULO, BARRA }

@Serializable
enum class FractionMode {
    /** Pintar exactamente num/den. */
    PINTAR,

    /** Pintar una fraccion equivalente a la mostrada. */
    EQUIVALENTE
}

@Serializable
@SerialName("fraction_pie")
data class FractionPiePayload(
    val shape: PieShape,
    /** Numero de porciones en que esta dividida la figura. */
    val parts: Int,
    val targetNumerator: Int,
    val targetDenominator: Int,
    val mode: FractionMode = FractionMode.PINTAR
) : ChallengePayload()

@Serializable
@SerialName("fraction_line")
data class FractionLinePayload(
    /** Denominador de las marcas de la recta. */
    val denominator: Int,
    /** Numerador objetivo (puede ser mayor que el denominador). */
    val numerator: Int,
    /** Cuantas unidades enteras abarca la recta. */
    val wholes: Int = 1,
    /** Cuantas marcas de error se admiten. */
    val toleranceSteps: Int = 0,
    /** Si es true se rotula con decimales en vez de fracciones. */
    val decimalLabels: Boolean = false
) : ChallengePayload()

// ------------------------------------------------------------ VALOR POSICIONAL

@Serializable
@SerialName("place_value")
data class PlaceValuePayload(
    val target: Int,
    /** Piezas disponibles: 1, 10, 100, 1000. */
    val pieces: List<Int> = listOf(1, 10, 100, 1000),
    val maxPerPiece: Int = 9
) : ChallengePayload()

// ---------------------------------------------------------------- PATRONES

@Serializable
data class PatternToken(
    /** Texto que se muestra (numero) o cadena vacia si es figura. */
    val label: String = "",
    /** Figura: triangulo, cuadrado, circulo, rombo, estrella, hexagono. */
    val shape: String? = null,
    val colorIndex: Int = 0,
    val rotation: Int = 0
)

@Serializable
@SerialName("pattern")
data class PatternPayload(
    /** Secuencia visible; la posicion vacia se marca con [holeIndex]. */
    val sequence: List<PatternToken>,
    val holeIndex: Int,
    val options: List<PatternToken>,
    val answerIndex: Int,
    /** Regla en lenguaje natural, se revela en la explicacion. */
    val rule: String
) : ChallengePayload()

// -------------------------------------------------------------------- QUIZ

@Serializable
@SerialName("quiz")
data class QuizPayload(
    val options: List<String>,
    val answerIndex: Int,
    /** Ilustracion de apoyo: mercado, planos, mapa, cinta, reloj, tarta. */
    val art: String = "kubo"
) : ChallengePayload()
