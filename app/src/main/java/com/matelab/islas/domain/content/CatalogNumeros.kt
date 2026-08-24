package com.matelab.islas.domain.content

import com.matelab.islas.domain.model.Challenge
import com.matelab.islas.domain.model.Difficulty
import com.matelab.islas.domain.model.GameKind
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.PatternPayload
import com.matelab.islas.domain.model.PatternToken
import com.matelab.islas.domain.model.PlaceValuePayload
import com.matelab.islas.domain.model.QuizPayload
import com.matelab.islas.domain.model.World
import com.matelab.islas.domain.model.WorldTheme

/**
 * Isla 4: Cueva de los Numeros.
 * Valor posicional con bloques de base diez y patrones numericos y visuales.
 */
internal object CatalogNumeros {

    const val WORLD_ID = "w_numeros"

    val world = World(
        id = WORLD_ID,
        order = 4,
        name = "Cueva de los Numeros",
        subtitle = "Valor posicional y patrones",
        description = "Dentro de la cueva los cristales crecen siguiendo reglas. " +
            "Quien descubre la regla, controla la cueva.",
        theme = WorldTheme.NUMEROS,
        xpToUnlock = 560
    )

    val missions = listOf(
        Mission(
            id = "m_n1", worldId = WORLD_ID, order = 1,
            name = "Bloques de la cueva",
            goal = "Construye numeros con unidades, decenas y centenas",
            briefing = "Cada bloque vale diez veces mas que el anterior. Construye con cabeza.",
            difficulty = Difficulty.EXPLORADOR,
            rewardCollectibleId = "cr_bloque"
        ),
        Mission(
            id = "m_n2", worldId = WORLD_ID, order = 2,
            name = "Cristales en secuencia",
            goal = "Descubre la regla y completa el patron",
            briefing = "Los cristales crecen en orden. Adivina cual falta.",
            difficulty = Difficulty.EXPLORADOR,
            rewardCollectibleId = "cr_secuencia"
        ),
        Mission(
            id = "m_n3", worldId = WORLD_ID, order = 3,
            name = "El eco de las tablas",
            goal = "Usa la multiplicacion para saltar de diez en diez",
            briefing = "El eco repite los numeros multiplicados. Sigue el ritmo.",
            difficulty = Difficulty.AVENTURERO,
            requires = listOf("m_n2"),
            rewardCollectibleId = "cr_eco"
        ),
        Mission(
            id = "m_n4", worldId = WORLD_ID, order = 4,
            name = "El gran calculo",
            goal = "Junta todo lo aprendido en la cueva",
            briefing = "La sala del tesoro solo se abre con el numero exacto.",
            difficulty = Difficulty.MAESTRO,
            requires = listOf("m_n1", "m_n3"),
            rewardCollectibleId = "cr_tesoro"
        )
    )

    private fun num(value: Int) = PatternToken(label = value.toString())
    private fun fig(shape: String, color: Int, rotation: Int = 0) =
        PatternToken(shape = shape, colorIndex = color, rotation = rotation)

    val challenges = buildList {

        // ---- m_n1: valor posicional ---------------------------------------------
        add(
            Challenge(
                id = "c_n1_1", missionId = "m_n1", order = 1, kind = GameKind.PLACE_VALUE,
                prompt = "Construye el numero 34 con los bloques.",
                explanation = "34 son 3 decenas y 4 unidades. Cada barra vale 10 y cada " +
                    "cubito vale 1.",
                hint = "Empieza colocando las barras de 10.",
                payload = PlaceValuePayload(target = 34, pieces = listOf(1, 10))
            )
        )
        add(
            Challenge(
                id = "c_n1_2", missionId = "m_n1", order = 2, kind = GameKind.PLACE_VALUE,
                prompt = "Ahora construye 152.",
                explanation = "152 son 1 centena, 5 decenas y 2 unidades. La placa grande " +
                    "vale 100 porque son 10 barras de 10.",
                hint = "Coloca primero la placa de 100.",
                payload = PlaceValuePayload(target = 152, pieces = listOf(1, 10, 100))
            )
        )
        add(
            Challenge(
                id = "c_n1_3", missionId = "m_n1", order = 3, kind = GameKind.PLACE_VALUE,
                prompt = "Construye 407. Cuidado con las decenas.",
                explanation = "407 tiene 4 centenas, 0 decenas y 7 unidades. El cero indica " +
                    "que esa posicion esta vacia, pero no se puede borrar.",
                hint = "No hace falta ninguna barra de 10.",
                payload = PlaceValuePayload(target = 407, pieces = listOf(1, 10, 100))
            )
        )
        add(
            Challenge(
                id = "c_n1_4", missionId = "m_n1", order = 4, kind = GameKind.PLACE_VALUE,
                prompt = "El numero mas grande de la sala: 1230.",
                explanation = "1230 son 1 millar, 2 centenas, 3 decenas y 0 unidades. " +
                    "Cada posicion vale diez veces la de su derecha.",
                hint = "El cubo grande vale 1000.",
                payload = PlaceValuePayload(target = 1230)
            )
        )
        add(
            Challenge(
                id = "c_n1_5", missionId = "m_n1", order = 5, kind = GameKind.QUIZ,
                prompt = "En el numero 4703, cuanto vale el 7?",
                explanation = "El 7 esta en las centenas, asi que vale 700. La posicion de " +
                    "una cifra decide su valor.",
                hint = "Cuenta las posiciones desde la derecha: unidades, decenas, centenas.",
                payload = QuizPayload(
                    options = listOf("7", "70", "700", "7000"),
                    answerIndex = 2,
                    art = "cueva"
                )
            )
        )

        // ---- m_n2: patrones ---------------------------------------------------------
        add(
            Challenge(
                id = "c_n2_1", missionId = "m_n2", order = 1, kind = GameKind.PATTERN,
                prompt = "Que cristal falta en la fila?",
                explanation = "La secuencia repite triangulo, cuadrado, circulo una y otra " +
                    "vez. Encontrar la unidad que se repite resuelve el patron.",
                hint = "Mira los tres primeros y busca donde vuelve a empezar.",
                payload = PatternPayload(
                    sequence = listOf(
                        fig("triangulo", 0), fig("cuadrado", 1), fig("circulo", 2),
                        fig("triangulo", 0), fig("cuadrado", 1), fig("circulo", 2),
                        fig("triangulo", 0), PatternToken()
                    ),
                    holeIndex = 7,
                    options = listOf(fig("circulo", 2), fig("cuadrado", 1), fig("triangulo", 0), fig("estrella", 3)),
                    answerIndex = 1,
                    rule = "Se repite: triangulo, cuadrado, circulo."
                )
            )
        )
        add(
            Challenge(
                id = "c_n2_2", missionId = "m_n2", order = 2, kind = GameKind.PATTERN,
                prompt = "Completa la serie numerica.",
                explanation = "Cada numero suma 4 al anterior: 3, 7, 11, 15, 19. " +
                    "Es una progresion de paso constante.",
                hint = "Resta dos numeros seguidos para descubrir el salto.",
                payload = PatternPayload(
                    sequence = listOf(num(3), num(7), num(11), PatternToken(), num(19)),
                    holeIndex = 3,
                    options = listOf(num(13), num(14), num(15), num(16)),
                    answerIndex = 2,
                    rule = "Se suma 4 cada vez."
                )
            )
        )
        add(
            Challenge(
                id = "c_n2_3", missionId = "m_n2", order = 3, kind = GameKind.PATTERN,
                prompt = "Que numero falta ahora?",
                explanation = "La serie baja de 6 en 6: 40, 34, 28, 22, 16. Los patrones " +
                    "tambien pueden ir hacia atras.",
                hint = "Comprueba si los numeros suben o bajan.",
                payload = PatternPayload(
                    sequence = listOf(num(40), num(34), PatternToken(), num(22), num(16)),
                    holeIndex = 2,
                    options = listOf(num(30), num(28), num(26), num(24)),
                    answerIndex = 1,
                    rule = "Se resta 6 cada vez."
                )
            )
        )
        add(
            Challenge(
                id = "c_n2_4", missionId = "m_n2", order = 4, kind = GameKind.PATTERN,
                prompt = "El patron de colores se ha roto. Arreglalo.",
                explanation = "La unidad que se repite tiene cuatro piezas: rombo, rombo, " +
                    "estrella, hexagono. Contar la unidad completa evita equivocarse.",
                hint = "Cuenta cuantas piezas hay antes de que todo vuelva a empezar.",
                payload = PatternPayload(
                    sequence = listOf(
                        fig("rombo", 0), fig("rombo", 1), fig("estrella", 3), fig("hexagono", 2),
                        fig("rombo", 0), fig("rombo", 1), PatternToken(), fig("hexagono", 2)
                    ),
                    holeIndex = 6,
                    options = listOf(fig("hexagono", 2), fig("rombo", 0), fig("estrella", 3), fig("circulo", 1)),
                    answerIndex = 2,
                    rule = "Se repite: rombo, rombo, estrella, hexagono."
                )
            )
        )
        add(
            Challenge(
                id = "c_n2_5", missionId = "m_n2", order = 5, kind = GameKind.PATTERN,
                prompt = "Una serie que crece deprisa.",
                explanation = "Cada numero es el doble del anterior: 2, 4, 8, 16, 32. " +
                    "Cuando se multiplica siempre por lo mismo, la serie se dispara.",
                hint = "Prueba a multiplicar en vez de sumar.",
                payload = PatternPayload(
                    sequence = listOf(num(2), num(4), num(8), PatternToken(), num(32)),
                    holeIndex = 3,
                    options = listOf(num(12), num(16), num(24), num(20)),
                    answerIndex = 1,
                    rule = "Se multiplica por 2 cada vez."
                )
            )
        )

        // ---- m_n3: tablas ------------------------------------------------------------
        add(
            Challenge(
                id = "c_n3_1", missionId = "m_n3", order = 1, kind = GameKind.PATTERN,
                prompt = "La tabla del 7 tiene un hueco.",
                explanation = "7, 14, 21, 28, 35: cada paso suma 7. Multiplicar por 7 es " +
                    "sumar 7 tantas veces como diga el otro numero.",
                hint = "Suma 7 al numero anterior.",
                payload = PatternPayload(
                    sequence = listOf(num(7), num(14), PatternToken(), num(28), num(35)),
                    holeIndex = 2,
                    options = listOf(num(20), num(21), num(22), num(24)),
                    answerIndex = 1,
                    rule = "Tabla del 7: se suma 7 cada vez."
                )
            )
        )
        add(
            Challenge(
                id = "c_n3_2", missionId = "m_n3", order = 2, kind = GameKind.PATTERN,
                prompt = "Saltos de 25 en 25.",
                explanation = "25, 50, 75, 100, 125. Contar de 25 en 25 es util con el " +
                    "dinero y con los cuartos de hora.",
                hint = "Cuatro saltos de 25 hacen 100.",
                payload = PatternPayload(
                    sequence = listOf(num(25), num(50), num(75), PatternToken(), num(125)),
                    holeIndex = 3,
                    options = listOf(num(90), num(95), num(100), num(110)),
                    answerIndex = 2,
                    rule = "Se suma 25 cada vez."
                )
            )
        )
        add(
            Challenge(
                id = "c_n3_3", missionId = "m_n3", order = 3, kind = GameKind.PATTERN,
                prompt = "Cristales que se triplican.",
                explanation = "1, 3, 9, 27, 81: cada numero es el triple del anterior.",
                hint = "Multiplica por 3.",
                payload = PatternPayload(
                    sequence = listOf(num(1), num(3), num(9), PatternToken(), num(81)),
                    holeIndex = 3,
                    options = listOf(num(18), num(27), num(36), num(45)),
                    answerIndex = 1,
                    rule = "Se multiplica por 3 cada vez."
                )
            )
        )
        add(
            Challenge(
                id = "c_n3_4", missionId = "m_n3", order = 4, kind = GameKind.QUIZ,
                prompt = "Si 6 x 8 = 48, cuanto es 6 x 80?",
                explanation = "80 es 8 x 10, asi que el resultado se multiplica tambien por " +
                    "10: 48 x 10 = 480.",
                hint = "Anadir un cero al final multiplica por 10.",
                payload = QuizPayload(
                    options = listOf("48", "408", "480", "4800"),
                    answerIndex = 2,
                    art = "eco"
                )
            )
        )
        add(
            Challenge(
                id = "c_n3_5", missionId = "m_n3", order = 5, kind = GameKind.QUIZ,
                prompt = "Reparto 42 cristales en 6 cajas iguales. Cuantos van en cada una?",
                explanation = "42 : 6 = 7. Dividir es repartir en partes iguales, lo " +
                    "contrario de multiplicar.",
                hint = "Que numero multiplicado por 6 da 42?",
                payload = QuizPayload(
                    options = listOf("6", "7", "8", "9"),
                    answerIndex = 1,
                    art = "cueva"
                )
            )
        )

        // ---- m_n4: maestria -------------------------------------------------------------
        add(
            Challenge(
                id = "c_n4_1", missionId = "m_n4", order = 1, kind = GameKind.PLACE_VALUE,
                prompt = "La cerradura pide el numero 2085.",
                explanation = "2085 son 2 millares, 0 centenas, 8 decenas y 5 unidades. " +
                    "El cero en las centenas es imprescindible.",
                hint = "No pongas ninguna placa de 100.",
                payload = PlaceValuePayload(target = 2085)
            )
        )
        add(
            Challenge(
                id = "c_n4_2", missionId = "m_n4", order = 2, kind = GameKind.PATTERN,
                prompt = "La ultima secuencia de la cueva.",
                explanation = "100, 90, 80, 70, 60: se resta 10 cada vez. Contar hacia atras " +
                    "de diez en diez ayuda a restar mentalmente.",
                hint = "Va bajando siempre lo mismo.",
                payload = PatternPayload(
                    sequence = listOf(num(100), num(90), num(80), PatternToken(), num(60)),
                    holeIndex = 3,
                    options = listOf(num(75), num(70), num(65), num(50)),
                    answerIndex = 1,
                    rule = "Se resta 10 cada vez."
                )
            )
        )
        add(
            Challenge(
                id = "c_n4_3", missionId = "m_n4", order = 3, kind = GameKind.PLACE_VALUE,
                prompt = "Y ahora 3609.",
                explanation = "3609 son 3 millares, 6 centenas, 0 decenas y 9 unidades.",
                hint = "Fijate bien en la posicion vacia.",
                payload = PlaceValuePayload(target = 3609)
            )
        )
        add(
            Challenge(
                id = "c_n4_4", missionId = "m_n4", order = 4, kind = GameKind.PATTERN,
                prompt = "Mosaico final de la puerta.",
                explanation = "El mosaico repite estrella, hexagono, triangulo, triangulo. " +
                    "Los patrones tambien decoran suelos y tejidos de verdad.",
                hint = "La unidad que se repite tiene cuatro piezas.",
                payload = PatternPayload(
                    sequence = listOf(
                        fig("estrella", 3), fig("hexagono", 2), fig("triangulo", 0), fig("triangulo", 1),
                        fig("estrella", 3), PatternToken(), fig("triangulo", 0), fig("triangulo", 1)
                    ),
                    holeIndex = 5,
                    options = listOf(fig("triangulo", 0), fig("hexagono", 2), fig("estrella", 3), fig("rombo", 1)),
                    answerIndex = 1,
                    rule = "Se repite: estrella, hexagono, triangulo, triangulo."
                )
            )
        )
        add(
            Challenge(
                id = "c_n4_5", missionId = "m_n4", order = 5, kind = GameKind.QUIZ,
                prompt = "Cual es el numero anterior a 3000?",
                explanation = "El anterior a 3000 es 2999. Al restar 1 cambian todas las " +
                    "posiciones que estaban a cero.",
                hint = "Piensa en el cuentakilometros de un coche.",
                payload = QuizPayload(
                    options = listOf("2900", "2990", "2999", "3001"),
                    answerIndex = 2,
                    art = "cueva"
                )
            )
        )
    }
}
