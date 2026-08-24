package com.matelab.islas.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.matelab.islas.domain.engine.ShapeSortEngine
import com.matelab.islas.domain.model.BucketSpec
import com.matelab.islas.domain.model.ShapeSortPayload
import com.matelab.islas.domain.model.ShapeSpec
import com.matelab.islas.ui.art.GeoShape
import com.matelab.islas.ui.components.MateButton
import com.matelab.islas.ui.components.rememberUiFeedback
import com.matelab.islas.ui.theme.MateTheme
import com.matelab.islas.ui.theme.ShapePalette
import com.matelab.islas.ui.theme.Teal
import kotlin.math.roundToInt

/**
 * Cinta clasificadora: el nino arrastra cada figura hasta su caja.
 * El acierto se decide con las propiedades reales de la figura.
 */
@Composable
fun ShapeSortGame(
    payload: ShapeSortPayload,
    enabled: Boolean,
    onSubmit: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val feedback = rememberUiFeedback()
    val placement = remember { mutableStateMapOf<String, String>() }
    var rootCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val bucketCoords = remember { mutableStateMapOf<String, LayoutCoordinates>() }

    val pending = payload.shapes.filter { it.id !in placement }

    Box(
        modifier
            .fillMaxWidth()
            .onGloballyPositioned { rootCoords = it }
    ) {
        Column(Modifier.fillMaxWidth()) {

            // ------------------------------------------------------ cestas
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                payload.buckets.forEach { bucket ->
                    BucketBox(
                        bucket = bucket,
                        shapes = payload.shapes.filter { placement[it.id] == bucket.id },
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { bucketCoords[bucket.id] = it },
                        onRemove = { shapeId ->
                            if (enabled) {
                                placement.remove(shapeId)
                                feedback.tap()
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                if (pending.isEmpty()) "Todas colocadas. Comprueba tu clasificacion."
                else "Arrastra las figuras a su caja",
                style = MaterialTheme.typography.titleMedium,
                color = MateTheme.colors.inkSoft
            )

            Spacer(Modifier.height(8.dp))

            // ------------------------------------------------------ bandeja
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MateTheme.colors.cardAlt)
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pending.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEachIndexed { index, shape ->
                                DraggableShape(
                                    shape = shape,
                                    color = ShapePalette[(payload.shapes.indexOf(shape) + index) % ShapePalette.size],
                                    enabled = enabled,
                                    onDropped = { coords ->
                                        val root = rootCoords
                                        if (root != null) {
                                            val center = root.localBoundingBoxOf(coords).center
                                            val target = payload.buckets.firstOrNull { bucket ->
                                                val bc = bucketCoords[bucket.id] ?: return@firstOrNull false
                                                root.localBoundingBoxOf(bc).contains(center)
                                            }
                                            if (target != null) {
                                                placement[shape.id] = target.id
                                                feedback.tap()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            MateButton(
                text = "Comprobar",
                onClick = {
                    onSubmit(
                        ShapeSortEngine.isSolved(placement.toMap(), payload.shapes, payload.buckets)
                    )
                },
                enabled = enabled && pending.isEmpty(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BucketBox(
    bucket: BucketSpec,
    shapes: List<ShapeSpec>,
    modifier: Modifier = Modifier,
    onRemove: (String) -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Teal.copy(alpha = 0.10f))
            .border(2.dp, Teal.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .padding(10.dp)
            .heightIn(min = 128.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            bucket.label,
            style = MaterialTheme.typography.labelMedium,
            color = MateTheme.colors.ink,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        shapes.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { shape ->
                    Box(Modifier.size(40.dp)) {
                        GeoShape(
                            spec = shape,
                            color = ShapePalette[shape.sides % ShapePalette.size],
                            size = 40.dp,
                            modifier = Modifier.clickableNoRipple { onRemove(shape.id) }
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun DraggableShape(
    shape: ShapeSpec,
    color: androidx.compose.ui.graphics.Color,
    enabled: Boolean,
    onDropped: (LayoutCoordinates) -> Unit
) {
    var drag by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val scale by animateFloatAsState(if (dragging) 1.15f else 1f, tween(120), label = "drag")

    Box(
        Modifier
            .offset { IntOffset(drag.x.roundToInt(), drag.y.roundToInt()) }
            .scale(scale)
            .size(56.dp)
            .onGloballyPositioned { coords = it }
            .pointerInput(enabled, shape.id) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { dragging = true },
                    onDragEnd = {
                        dragging = false
                        coords?.let(onDropped)
                        drag = Offset.Zero
                    },
                    onDragCancel = {
                        dragging = false
                        drag = Offset.Zero
                    }
                ) { change, amount ->
                    change.consume()
                    drag += amount
                }
            },
        contentAlignment = Alignment.Center
    ) {
        GeoShape(spec = shape, color = color, size = 52.dp)
    }
}

/** Click sin efecto de onda, para elementos ilustrados. */
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.pointerInput(onClick) {
        detectTapGestures { onClick() }
    }
