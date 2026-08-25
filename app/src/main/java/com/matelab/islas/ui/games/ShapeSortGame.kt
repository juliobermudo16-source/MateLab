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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
    val bucketCoords = remember { mutableStateMapOf<String, LayoutCoordinates>() }

    val pending = payload.shapes.filter { it.id !in placement }

    Box(
        modifier
            .fillMaxWidth()
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
                        allShapes = payload.shapes,
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
            // Sin clip a proposito: con el, la figura arrastrada desaparecia
            // en cuanto salia de la bandeja. background(color, shape) pinta
            // las esquinas redondeadas sin recortar a los hijos.
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp)
                    .background(MateTheme.colors.cardAlt, RoundedCornerShape(20.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pending.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { shape ->
                                DraggableShape(
                                    shape = shape,
                                    color = shapeColor(shape, payload.shapes),
                                    enabled = enabled,
                                    onDropped = { fingerInWindow ->
                                        // Se compara el dedo con las cajas usando
                                        // coordenadas de ventana, que no se recortan.
                                        val target = payload.buckets.firstOrNull { bucket ->
                                            val bc = bucketCoords[bucket.id]
                                                ?: return@firstOrNull false
                                            bc.boundsInWindow().contains(fingerInWindow)
                                        }
                                        if (target != null) {
                                            placement[shape.id] = target.id
                                            feedback.tap()
                                        } else {
                                            // Fuera de toda caja: la figura vuelve sola.
                                            feedback.wrong()
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
    allShapes: List<ShapeSpec>,
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
                            color = shapeColor(shape, allShapes),
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

/**
 * Figura arrastrable.
 *
 * Informa de la posicion del DEDO en coordenadas de ventana, no del recuadro
 * de la figura. Medir el recuadro fallaba: al salir de la bandeja quedaba
 * recortado, su rectangulo se volvia vacio y el centro caia en (0,0), que
 * esta dentro de la primera caja. Resultado: todo iba a parar a la caja 1.
 */
@Composable
private fun DraggableShape(
    shape: ShapeSpec,
    color: Color,
    enabled: Boolean,
    onDropped: (Offset) -> Unit
) {
    var drag by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    // Punto donde esta el dedo, en coordenadas de ventana.
    var fingerInWindow by remember { mutableStateOf(Offset.Zero) }
    val scale by animateFloatAsState(if (dragging) 1.2f else 1f, tween(120), label = "drag")

    Box(
        Modifier
            .zIndex(if (dragging) 1f else 0f)
            .offset { IntOffset(drag.x.roundToInt(), drag.y.roundToInt()) }
            .scale(scale)
            .size(56.dp)
            .onGloballyPositioned { coords = it }
            .pointerInput(enabled, shape.id) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { start ->
                        dragging = true
                        coords?.let { fingerInWindow = it.localToWindow(start) }
                    },
                    onDragEnd = {
                        dragging = false
                        onDropped(fingerInWindow)
                        drag = Offset.Zero
                    },
                    onDragCancel = {
                        dragging = false
                        drag = Offset.Zero
                    }
                ) { change, amount ->
                    change.consume()
                    drag += amount
                    // El dedo se mueve con el gesto, no con el recuadro.
                    fingerInWindow += amount
                }
            },
        contentAlignment = Alignment.Center
    ) {
        GeoShape(spec = shape, color = color, size = 52.dp)
    }
}

/**
 * Color fijo de cada figura, calculado por su posicion en el reto.
 * Antes la bandeja y la caja usaban formulas distintas y la figura cambiaba
 * de color al soltarla, lo que despistaba.
 */
private fun shapeColor(shape: ShapeSpec, allShapes: List<ShapeSpec>): Color {
    val index = allShapes.indexOfFirst { it.id == shape.id }.coerceAtLeast(0)
    return ShapePalette[index % ShapePalette.size]
}

/** Click sin efecto de onda, para elementos ilustrados. */
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.pointerInput(onClick) {
        detectTapGestures { onClick() }
    }
