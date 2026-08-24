package com.matelab.islas.domain.engine

import com.matelab.islas.domain.model.BucketSpec
import com.matelab.islas.domain.model.ShapeCriterion
import com.matelab.islas.domain.model.ShapeSpec

/**
 * Motor de la cinta clasificadora: decide en que cesta cae cada figura.
 */
object ShapeSortEngine {

    fun matches(shape: ShapeSpec, bucket: BucketSpec): Boolean = when (bucket.criterion) {
        ShapeCriterion.NUM_LADOS -> !shape.curved && shape.sides == bucket.value
        ShapeCriterion.TIENE_ANGULO_RECTO -> shape.rightAngles > 0
        ShapeCriterion.LADOS_IGUALES -> !shape.curved && shape.allSidesEqual
        ShapeCriterion.ES_CURVA -> shape.curved
    }

    /** Cesta correcta para la figura, o null si ninguna la acepta. */
    fun bucketFor(shape: ShapeSpec, buckets: List<BucketSpec>): String? =
        buckets.firstOrNull { matches(shape, it) }?.id

    /** True si la colocacion del nino es la correcta. */
    fun isPlacementCorrect(shape: ShapeSpec, bucketId: String, buckets: List<BucketSpec>): Boolean {
        val bucket = buckets.firstOrNull { it.id == bucketId } ?: return false
        return matches(shape, bucket)
    }

    /** Cuantas figuras estan bien colocadas. */
    fun countCorrect(
        placement: Map<String, String>,
        shapes: List<ShapeSpec>,
        buckets: List<BucketSpec>
    ): Int = placement.count { (shapeId, bucketId) ->
        val shape = shapes.firstOrNull { it.id == shapeId }
        shape != null && isPlacementCorrect(shape, bucketId, buckets)
    }

    fun isSolved(
        placement: Map<String, String>,
        shapes: List<ShapeSpec>,
        buckets: List<BucketSpec>
    ): Boolean = shapes.isNotEmpty() &&
        placement.size == shapes.size &&
        countCorrect(placement, shapes, buckets) == shapes.size

    /**
     * Verifica que el reto tenga solucion: toda figura debe caber en alguna
     * cesta y ninguna cesta puede quedarse vacia.
     */
    fun isChallengeWellFormed(shapes: List<ShapeSpec>, buckets: List<BucketSpec>): Boolean {
        if (shapes.isEmpty() || buckets.isEmpty()) return false
        if (shapes.any { bucketFor(it, buckets) == null }) return false
        return buckets.all { bucket -> shapes.any { matches(it, bucket) } }
    }
}
