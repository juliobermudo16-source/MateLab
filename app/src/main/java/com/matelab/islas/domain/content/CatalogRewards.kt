package com.matelab.islas.domain.content

import com.matelab.islas.domain.model.Badge
import com.matelab.islas.domain.model.BadgeRule
import com.matelab.islas.domain.model.Collectible
import com.matelab.islas.domain.model.Rarity

/**
 * Recompensas del archipielago: insignias y Cristales de Ingenio.
 *
 * Cada cristal guarda un dato matematico real que se revela al conseguirlo.
 */
internal object CatalogRewards {

    val badges = listOf(
        Badge(
            id = "b_primer_paso",
            name = "Primer desembarco",
            description = "Completa tu primera mision del archipielago.",
            rule = BadgeRule.FIRST_MISSION,
            threshold = 1,
            artSeed = 1
        ),
        Badge(
            id = "b_formas",
            name = "Guardian del faro",
            description = "Termina todas las misiones de la Bahia de las Formas.",
            rule = BadgeRule.WORLD_COMPLETE,
            param = "w_formas",
            artSeed = 2
        ),
        Badge(
            id = "b_medida",
            name = "Capataz del puerto",
            description = "Termina todas las misiones de Puerto Medida.",
            rule = BadgeRule.WORLD_COMPLETE,
            param = "w_medida",
            artSeed = 3
        ),
        Badge(
            id = "b_fraccion",
            name = "Domador de lava",
            description = "Termina todas las misiones del Volcan Fraccion.",
            rule = BadgeRule.WORLD_COMPLETE,
            param = "w_fraccion",
            artSeed = 4
        ),
        Badge(
            id = "b_numeros",
            name = "Llave de la cueva",
            description = "Termina todas las misiones de la Cueva de los Numeros.",
            rule = BadgeRule.WORLD_COMPLETE,
            param = "w_numeros",
            artSeed = 5
        ),
        Badge(
            id = "b_perfecto",
            name = "Pulso firme",
            description = "Consigue 3 estrellas en 3 misiones distintas.",
            rule = BadgeRule.PERFECT_MISSION,
            threshold = 3,
            artSeed = 6
        ),
        Badge(
            id = "b_estrellas",
            name = "Cielo estrellado",
            description = "Reune 25 estrellas en total.",
            rule = BadgeRule.TOTAL_STARS,
            threshold = 25,
            artSeed = 7
        ),
        Badge(
            id = "b_experto",
            name = "Ingeniero jefe",
            description = "Alcanza 800 puntos de experiencia.",
            rule = BadgeRule.TOTAL_XP,
            threshold = 800,
            artSeed = 8
        ),
        Badge(
            id = "b_racha",
            name = "Explorador constante",
            description = "Juega 3 dias seguidos.",
            rule = BadgeRule.STREAK_DAYS,
            threshold = 3,
            artSeed = 9
        ),
        Badge(
            id = "b_coleccionista",
            name = "Coleccionista",
            description = "Consigue 12 Cristales de Ingenio.",
            rule = BadgeRule.COLLECTION_SIZE,
            threshold = 12,
            artSeed = 10
        ),
        Badge(
            id = "b_taller",
            name = "Manos de taller",
            description = "Supera 2 sesiones de repaso sin fallos.",
            rule = BadgeRule.REVIEW_CLEARED,
            threshold = 2,
            artSeed = 11
        ),
        Badge(
            id = "b_sin_pistas",
            name = "Sin ayudas",
            description = "Termina 5 misiones sin pedir ni una pista.",
            rule = BadgeRule.NO_HINT_MISSION,
            threshold = 5,
            artSeed = 12
        ),
        Badge(
            id = "b_maestro_formas",
            name = "Ojo geometrico",
            description = "Acierta el 85 % en la Bahia de las Formas.",
            rule = BadgeRule.TOPIC_MASTER,
            param = "w_formas",
            threshold = 85,
            artSeed = 13
        )
    )

    val collectibles = listOf(
        // ---- Bahia de las Formas ----
        Collectible(
            "cr_faro", "Cristal Faro",
            "Un poligono tiene siempre el mismo numero de lados que de vertices.",
            "w_formas", Rarity.COMUN, 1
        ),
        Collectible(
            "cr_geoplano", "Cristal Geoplano",
            "Dos figuras muy distintas pueden encerrar exactamente la misma area.",
            "w_formas", Rarity.COMUN, 2
        ),
        Collectible(
            "cr_transportador", "Cristal Transportador",
            "Una vuelta completa son 360 grados; los babilonios ya usaban ese numero.",
            "w_formas", Rarity.RARO, 3
        ),
        Collectible(
            "cr_espejo", "Cristal Espejo",
            "El cuerpo humano es casi simetrico, pero nunca del todo.",
            "w_formas", Rarity.RARO, 4
        ),
        Collectible(
            "cr_muelle", "Cristal Muelle",
            "Con el mismo perimetro, el cuadrado es el rectangulo de mayor area.",
            "w_formas", Rarity.LEGENDARIO, 5
        ),

        // ---- Puerto Medida ----
        Collectible(
            "cr_regla", "Cristal Regla",
            "El metro nacio en Francia como la diezmillonesima parte de un cuarto de meridiano.",
            "w_medida", Rarity.COMUN, 6
        ),
        Collectible(
            "cr_balanza", "Cristal Balanza",
            "La balanza de dos platos tiene mas de 4000 anos de antiguedad.",
            "w_medida", Rarity.COMUN, 7
        ),
        Collectible(
            "cr_reloj", "Cristal Reloj",
            "La hora se divide en 60 minutos porque los sumerios contaban en base 60.",
            "w_medida", Rarity.RARO, 8
        ),
        Collectible(
            "cr_escalera", "Cristal Escalera",
            "Cada peldano del sistema metrico multiplica o divide por 10.",
            "w_medida", Rarity.RARO, 9
        ),
        Collectible(
            "cr_cargamento", "Cristal Cargamento",
            "Un litro de agua pesa casi exactamente un kilogramo.",
            "w_medida", Rarity.LEGENDARIO, 10
        ),

        // ---- Volcan Fraccion ----
        Collectible(
            "cr_pizza", "Cristal Porcion",
            "Los egipcios escribian casi todas sus fracciones con numerador 1.",
            "w_fraccion", Rarity.COMUN, 11
        ),
        Collectible(
            "cr_cinta", "Cristal Cinta",
            "Multiplicar arriba y abajo por el mismo numero no cambia el valor.",
            "w_fraccion", Rarity.COMUN, 12
        ),
        Collectible(
            "cr_puente", "Cristal Puente",
            "Entre dos fracciones cualesquiera siempre cabe otra fraccion.",
            "w_fraccion", Rarity.RARO, 13
        ),
        Collectible(
            "cr_duelo", "Cristal Duelo",
            "Con el mismo numerador, gana la fraccion de denominador mas pequeno.",
            "w_fraccion", Rarity.RARO, 14
        ),
        Collectible(
            "cr_decimal", "Cristal Decimal",
            "La coma decimal se popularizo en Europa hace apenas 400 anos.",
            "w_fraccion", Rarity.LEGENDARIO, 15
        ),

        // ---- Cueva de los Numeros ----
        Collectible(
            "cr_bloque", "Cristal Bloque",
            "Usamos base diez casi seguro porque tenemos diez dedos.",
            "w_numeros", Rarity.COMUN, 16
        ),
        Collectible(
            "cr_secuencia", "Cristal Secuencia",
            "En la naturaleza hay patrones numericos, como las espirales de un girasol.",
            "w_numeros", Rarity.COMUN, 17
        ),
        Collectible(
            "cr_eco", "Cristal Eco",
            "Multiplicar por 10 solo desplaza las cifras una posicion a la izquierda.",
            "w_numeros", Rarity.RARO, 18
        ),
        Collectible(
            "cr_tesoro", "Cristal Tesoro",
            "El cero tardo siglos en aceptarse como numero de pleno derecho.",
            "w_numeros", Rarity.LEGENDARIO, 19
        ),

        // ---- Hitos ----
        Collectible(
            "cr_hito_formas", "Sello de la Bahia",
            "Has dominado la geometria de la isla.",
            "w_formas", Rarity.LEGENDARIO, 20
        ),
        Collectible(
            "cr_hito_medida", "Sello del Puerto",
            "Has dominado las medidas del archipielago.",
            "w_medida", Rarity.LEGENDARIO, 21
        ),
        Collectible(
            "cr_hito_fraccion", "Sello del Volcan",
            "Has dominado las fracciones y los decimales.",
            "w_fraccion", Rarity.LEGENDARIO, 22
        ),
        Collectible(
            "cr_hito_numeros", "Sello de la Cueva",
            "Has dominado los numeros y sus patrones.",
            "w_numeros", Rarity.LEGENDARIO, 23
        ),
        Collectible(
            "cr_hito_nivel5", "Nucleo Nivel 5",
            "La constancia tambien es una habilidad matematica.",
            "w_formas", Rarity.RARO, 24
        ),
        Collectible(
            "cr_hito_nivel10", "Nucleo Nivel 10",
            "Diez niveles de expedicion. Kubo esta impresionado.",
            "w_medida", Rarity.LEGENDARIO, 25
        ),
        Collectible(
            "cr_hito_estrellas", "Estrella Polar",
            "30 estrellas iluminan todo el archipielago.",
            "w_fraccion", Rarity.RARO, 26
        ),
        Collectible(
            "cr_hito_insignias", "Nucleo de Insignias",
            "Ocho insignias son ocho retos distintos superados.",
            "w_numeros", Rarity.LEGENDARIO, 27
        )
    )
}
