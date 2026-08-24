package com.matelab.islas.ui.games

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.matelab.islas.domain.model.AngleDialPayload
import com.matelab.islas.domain.model.BalancePayload
import com.matelab.islas.domain.model.Challenge
import com.matelab.islas.domain.model.ClockPayload
import com.matelab.islas.domain.model.FractionLinePayload
import com.matelab.islas.domain.model.FractionPiePayload
import com.matelab.islas.domain.model.GeoboardPayload
import com.matelab.islas.domain.model.PatternPayload
import com.matelab.islas.domain.model.PlaceValuePayload
import com.matelab.islas.domain.model.QuizPayload
import com.matelab.islas.domain.model.RulerPayload
import com.matelab.islas.domain.model.ShapeSortPayload
import com.matelab.islas.domain.model.SymmetryPayload

/**
 * Elige el mini-juego que corresponde a cada reto.
 *
 * La clave [key] fuerza a Compose a reiniciar el estado interno cuando se
 * pasa al siguiente reto, para que no se arrastre lo del anterior.
 */
@Composable
fun ChallengeGame(
    challenge: Challenge,
    enabled: Boolean,
    onSubmit: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.runtime.key(challenge.id) {
        when (val payload = challenge.payload) {
            is GeoboardPayload -> GeoboardGame(payload, enabled, onSubmit, modifier)
            is ShapeSortPayload -> ShapeSortGame(payload, enabled, onSubmit, modifier)
            is AngleDialPayload -> AngleDialGame(payload, enabled, onSubmit, modifier)
            is SymmetryPayload -> SymmetryGame(payload, enabled, onSubmit, modifier)
            is RulerPayload -> RulerGame(payload, enabled, onSubmit, modifier)
            is BalancePayload -> BalanceGame(payload, enabled, onSubmit, modifier)
            is ClockPayload -> ClockGame(payload, enabled, onSubmit, modifier)
            is FractionPiePayload -> FractionPieGame(payload, enabled, onSubmit, modifier)
            is FractionLinePayload -> FractionLineGame(payload, enabled, onSubmit, modifier)
            is PlaceValuePayload -> PlaceValueGame(payload, enabled, onSubmit, modifier)
            is PatternPayload -> PatternGame(payload, enabled, onSubmit, modifier)
            is QuizPayload -> QuizGame(payload, enabled, onSubmit, modifier)
        }
    }
}

/** Texto corto que describe el tipo de actividad, para la cabecera del reto. */
fun activityLabel(challenge: Challenge): String = when (challenge.payload) {
    is GeoboardPayload -> "Geoplano"
    is ShapeSortPayload -> "Clasificar"
    is AngleDialPayload -> "Transportador"
    is SymmetryPayload -> "Mosaico"
    is RulerPayload -> "Medir"
    is BalancePayload -> "Balanza"
    is ClockPayload -> "Reloj"
    is FractionPiePayload -> "Repartir"
    is FractionLinePayload -> "Recta"
    is PlaceValuePayload -> "Bloques"
    is PatternPayload -> "Patron"
    is QuizPayload -> "Decidir"
}

/** Marcador de posicion usado solo si un payload llegase corrupto. */
@Composable
internal fun BrokenChallenge(modifier: Modifier = Modifier) {
    Text("Este reto no se ha podido cargar.", modifier.fillMaxWidth())
}
