package com.matelab.islas.domain.content

import com.matelab.islas.domain.model.Challenge
import com.matelab.islas.domain.model.Difficulty
import com.matelab.islas.domain.model.FractionLinePayload
import com.matelab.islas.domain.model.FractionMode
import com.matelab.islas.domain.model.FractionPiePayload
import com.matelab.islas.domain.model.GameKind
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.PieShape
import com.matelab.islas.domain.model.QuizPayload
import com.matelab.islas.domain.model.World
import com.matelab.islas.domain.model.WorldTheme

/**
 * Isla 3: Volcan Fraccion.
 * Reparto en partes iguales, equivalencias, recta numerica y decimales.
 */
internal object CatalogFraccion {

    const val WORLD_ID = "w_fraccion"

    val world = World(
        id = WORLD_ID,
        order = 3,
        name = "Volcan Fraccion",
        subtitle = "Partes, equivalencias y decimales",
        description = "La lava del volcan se enfria en losas que hay que repartir en " +
            "partes iguales. Si el reparto falla, el puente no aguanta.",
        theme = WorldTheme.FRACCION,
        xpToUnlock = 320
    )

    val missions = listOf(
        Mission(
            id = "m_r1", worldId = WORLD_ID, order = 1,
            name = "La pizzeria del volcan",
            goal = "Pinta la fraccion exacta que te piden",
            briefing = "En la pizzeria del crater todo se reparte en partes iguales.",
            difficulty = Difficulty.EXPLORADOR,
            rewardCollectibleId = "cr_pizza"
        ),
        Mission(
            id = "m_r2", worldId = WORLD_ID, order = 2,
            name = "Cintas equivalentes",
            goal = "Descubre fracciones que valen lo mismo",
            briefing = "Dos cintas distintas pueden cubrir el mismo trozo de puente.",
            difficulty = Difficulty.AVENTURERO,
            requires = listOf("m_r1"),
            rewardCollectibleId = "cr_cinta"
        ),
        Mission(
            id = "m_r3", worldId = WORLD_ID, order = 3,
            name = "El puente de la recta",
            goal = "Coloca cada fraccion en su sitio",
            briefing = "Las tablas del puente estan numeradas con fracciones. Ordenalas.",
            difficulty = Difficulty.AVENTURERO,
            requires = listOf("m_r1"),
            rewardCollectibleId = "cr_puente"
        ),
        Mission(
            id = "m_r4", worldId = WORLD_ID, order = 4,
            name = "Duelo de fracciones",
            goal = "Compara fracciones y decide cual es mayor",
            briefing = "Dos herreros discuten por quien tiene mas metal. Resuelvelo tu.",
            difficulty = Difficulty.AVENTURERO,
            requires = listOf("m_r2", "m_r3"),
            rewardCollectibleId = "cr_duelo"
        ),
        Mission(
            id = "m_r5", worldId = WORLD_ID, order = 5,
            name = "Rios de decimales",
            goal = "Une fracciones y numeros decimales",
            briefing = "El rio de lava se mide en decimas. Es la misma idea con otra ropa.",
            difficulty = Difficulty.MAESTRO,
            requires = listOf("m_r4"),
            rewardCollectibleId = "cr_decimal"
        )
    )

    val challenges = buildList {

        // ---- m_r1: pintar fracciones -------------------------------------------
        add(
            Challenge(
                id = "c_r1_1", missionId = "m_r1", order = 1, kind = GameKind.FRACTION_PIE,
                prompt = "Pinta 1/2 de la pizza.",
                explanation = "El denominador (abajo) dice en cuantas partes iguales se " +
                    "divide el todo. El numerador (arriba) dice cuantas se toman.",
                hint = "La mitad de 2 porciones es 1 porcion.",
                payload = FractionPiePayload(PieShape.CIRCULO, parts = 2, targetNumerator = 1, targetDenominator = 2)
            )
        )
        add(
            Challenge(
                id = "c_r1_2", missionId = "m_r1", order = 2, kind = GameKind.FRACTION_PIE,
                prompt = "Pinta 3/4 de la losa.",
                explanation = "3/4 significa 3 partes de las 4 en que esta dividida la losa. " +
                    "Queda 1/4 sin pintar.",
                hint = "Pinta todas menos una.",
                payload = FractionPiePayload(PieShape.BARRA, parts = 4, targetNumerator = 3, targetDenominator = 4)
            )
        )
        add(
            Challenge(
                id = "c_r1_3", missionId = "m_r1", order = 3, kind = GameKind.FRACTION_PIE,
                prompt = "Pinta 2/6 de la rueda de piedra.",
                explanation = "2/6 son dos porciones de seis. Fijate: 2/6 tambien es 1/3, " +
                    "porque las dos fracciones cubren lo mismo.",
                hint = "Cuenta 2 porciones de las 6 que hay.",
                payload = FractionPiePayload(PieShape.CIRCULO, parts = 6, targetNumerator = 2, targetDenominator = 6)
            )
        )
        add(
            Challenge(
                id = "c_r1_4", missionId = "m_r1", order = 4, kind = GameKind.FRACTION_PIE,
                prompt = "Pinta 5/8 del tablon.",
                explanation = "Cuanto mayor es el denominador, mas pequenas son las partes. " +
                    "5/8 es un poco mas de la mitad, porque la mitad seria 4/8.",
                hint = "La mitad son 4 partes: pinta una mas.",
                payload = FractionPiePayload(PieShape.BARRA, parts = 8, targetNumerator = 5, targetDenominator = 8)
            )
        )
        add(
            Challenge(
                id = "c_r1_5", missionId = "m_r1", order = 5, kind = GameKind.QUIZ,
                prompt = "En 3/7, que significa el 7?",
                explanation = "El 7 es el denominador: indica en cuantas partes iguales se " +
                    "ha dividido el total. El 3 es cuantas de esas partes se cogen.",
                hint = "Piensa en cuantos trozos hay en total.",
                payload = QuizPayload(
                    options = listOf(
                        "Cuantas partes se cogen",
                        "En cuantas partes se divide el total",
                        "Cuantas partes sobran",
                        "El resultado de la division"
                    ),
                    answerIndex = 1,
                    art = "tarta"
                )
            )
        )

        // ---- m_r2: equivalentes -------------------------------------------------
        add(
            Challenge(
                id = "c_r2_1", missionId = "m_r2", order = 1, kind = GameKind.FRACTION_PIE,
                prompt = "Cubre lo mismo que 1/2, pero usando cuartos.",
                explanation = "1/2 = 2/4. Si partes cada mitad en dos, necesitas el doble de " +
                    "trozos para cubrir lo mismo.",
                hint = "Pinta 2 de las 4 partes.",
                payload = FractionPiePayload(
                    PieShape.BARRA, parts = 4, targetNumerator = 1, targetDenominator = 2,
                    mode = FractionMode.EQUIVALENTE
                )
            )
        )
        add(
            Challenge(
                id = "c_r2_2", missionId = "m_r2", order = 2, kind = GameKind.FRACTION_PIE,
                prompt = "Cubre lo mismo que 1/3, pero con novenos.",
                explanation = "1/3 = 3/9. Al multiplicar arriba y abajo por el mismo numero, " +
                    "la fraccion no cambia de valor.",
                hint = "3 x 3 = 9, asi que arriba tambien multiplicas por 3.",
                payload = FractionPiePayload(
                    PieShape.CIRCULO, parts = 9, targetNumerator = 1, targetDenominator = 3,
                    mode = FractionMode.EQUIVALENTE
                )
            )
        )
        add(
            Challenge(
                id = "c_r2_3", missionId = "m_r2", order = 3, kind = GameKind.FRACTION_PIE,
                prompt = "Cubre lo mismo que 3/4, pero con octavos.",
                explanation = "3/4 = 6/8. El denominador se ha duplicado, asi que el " +
                    "numerador tambien.",
                hint = "Si 4 se convierte en 8, el 3 se convierte en 6.",
                payload = FractionPiePayload(
                    PieShape.BARRA, parts = 8, targetNumerator = 3, targetDenominator = 4,
                    mode = FractionMode.EQUIVALENTE
                )
            )
        )
        add(
            Challenge(
                id = "c_r2_4", missionId = "m_r2", order = 4, kind = GameKind.FRACTION_PIE,
                prompt = "Cubre lo mismo que 2/3, pero con doceavos.",
                explanation = "2/3 = 8/12, porque 3 x 4 = 12 y 2 x 4 = 8.",
                hint = "Cuantas veces cabe el 3 en el 12? Multiplica el 2 por ese numero.",
                payload = FractionPiePayload(
                    PieShape.CIRCULO, parts = 12, targetNumerator = 2, targetDenominator = 3,
                    mode = FractionMode.EQUIVALENTE
                )
            )
        )
        add(
            Challenge(
                id = "c_r2_5", missionId = "m_r2", order = 5, kind = GameKind.QUIZ,
                prompt = "Cual de estas fracciones NO equivale a 1/2?",
                explanation = "2/4, 5/10 y 8/16 valen todas 1/2. En cambio 3/5 es un poco " +
                    "mas grande que la mitad, porque la mitad de 5 seria 2,5.",
                hint = "Comprueba si el numerador es justo la mitad del denominador.",
                payload = QuizPayload(
                    options = listOf("2/4", "5/10", "3/5", "8/16"),
                    answerIndex = 2,
                    art = "cintas"
                )
            )
        )

        // ---- m_r3: recta numerica ------------------------------------------------
        add(
            Challenge(
                id = "c_r3_1", missionId = "m_r3", order = 1, kind = GameKind.FRACTION_LINE,
                prompt = "Coloca la tabla en 1/4.",
                explanation = "Para colocar 1/4 se divide el tramo de 0 a 1 en 4 partes " +
                    "iguales y se avanza una.",
                hint = "Cuenta cuatro huecos entre el 0 y el 1 y para en el primero.",
                payload = FractionLinePayload(denominator = 4, numerator = 1, wholes = 1)
            )
        )
        add(
            Challenge(
                id = "c_r3_2", missionId = "m_r3", order = 2, kind = GameKind.FRACTION_LINE,
                prompt = "Ahora 3/5.",
                explanation = "3/5 esta un poco mas alla de la mitad, porque la mitad de 5 " +
                    "estaria entre la marca 2 y la 3.",
                hint = "Avanza tres marcas de las cinco que hay.",
                payload = FractionLinePayload(denominator = 5, numerator = 3, wholes = 1)
            )
        )
        add(
            Challenge(
                id = "c_r3_3", missionId = "m_r3", order = 3, kind = GameKind.FRACTION_LINE,
                prompt = "Coloca 5/4. Ojo: pasa del 1.",
                explanation = "Cuando el numerador es mayor que el denominador, la fraccion " +
                    "vale mas de una unidad. 5/4 es 1 entero y 1/4 mas.",
                hint = "4/4 es justo el 1. Avanza una marca mas.",
                payload = FractionLinePayload(denominator = 4, numerator = 5, wholes = 2)
            )
        )
        add(
            Challenge(
                id = "c_r3_4", missionId = "m_r3", order = 4, kind = GameKind.FRACTION_LINE,
                prompt = "Coloca 7/6.",
                explanation = "7/6 es 1 entero y 1/6. Es solo un pelin mas que la unidad.",
                hint = "6/6 es el 1. Solo falta una marca mas.",
                payload = FractionLinePayload(denominator = 6, numerator = 7, wholes = 2)
            )
        )
        add(
            Challenge(
                id = "c_r3_5", missionId = "m_r3", order = 5, kind = GameKind.FRACTION_LINE,
                prompt = "Coloca 3/8 en la recta.",
                explanation = "3/8 es menos que la mitad, porque la mitad seria 4/8.",
                hint = "Cuenta tres marcas pequenas desde el cero.",
                payload = FractionLinePayload(denominator = 8, numerator = 3, wholes = 1)
            )
        )

        // ---- m_r4: comparar --------------------------------------------------------
        add(
            Challenge(
                id = "c_r4_1", missionId = "m_r4", order = 1, kind = GameKind.QUIZ,
                prompt = "Que fraccion es mayor: 3/5 o 2/5?",
                explanation = "Con el mismo denominador gana la que tiene el numerador mas " +
                    "grande: los trozos son del mismo tamano y hay mas.",
                hint = "Las partes son iguales de grandes; cuenta cuantas hay.",
                payload = QuizPayload(
                    options = listOf("3/5", "2/5", "Son iguales", "Falta informacion"),
                    answerIndex = 0,
                    art = "duelo"
                )
            )
        )
        add(
            Challenge(
                id = "c_r4_2", missionId = "m_r4", order = 2, kind = GameKind.QUIZ,
                prompt = "Que fraccion es mayor: 1/3 o 1/5?",
                explanation = "Con el mismo numerador gana la de denominador menor: si " +
                    "repartes un pastel entre 3 tocan trozos mas grandes que entre 5.",
                hint = "Cuanta mas gente reparte, mas pequeno es cada trozo.",
                payload = QuizPayload(
                    options = listOf("1/3", "1/5", "Son iguales", "Depende del pastel"),
                    answerIndex = 0,
                    art = "duelo"
                )
            )
        )
        add(
            Challenge(
                id = "c_r4_3", missionId = "m_r4", order = 3, kind = GameKind.FRACTION_LINE,
                prompt = "Coloca 2/3 y comprueba que queda a la derecha de 1/2.",
                explanation = "En la recta numerica, la fraccion que queda mas a la derecha " +
                    "es siempre la mayor. 2/3 esta despues de 1/2.",
                hint = "1/2 esta justo en el centro; 2/3 va un poco mas alla.",
                payload = FractionLinePayload(denominator = 6, numerator = 4, wholes = 1)
            )
        )
        add(
            Challenge(
                id = "c_r4_4", missionId = "m_r4", order = 4, kind = GameKind.FRACTION_PIE,
                prompt = "Pinta una fraccion que valga lo mismo que 4/6.",
                explanation = "4/6 = 2/3. Simplificar es dividir arriba y abajo por el mismo " +
                    "numero, en este caso por 2.",
                hint = "Divide 4 y 6 entre 2.",
                payload = FractionPiePayload(
                    PieShape.CIRCULO, parts = 3, targetNumerator = 4, targetDenominator = 6,
                    mode = FractionMode.EQUIVALENTE
                )
            )
        )
        add(
            Challenge(
                id = "c_r4_5", missionId = "m_r4", order = 5, kind = GameKind.QUIZ,
                prompt = "Un herrero usa 5/8 del metal y otro 3/4. Quien usa mas?",
                explanation = "3/4 = 6/8, y 6/8 es mas que 5/8. Para comparar hay que poner " +
                    "el mismo denominador.",
                hint = "Convierte 3/4 a octavos.",
                payload = QuizPayload(
                    options = listOf("El de 5/8", "El de 3/4", "Los dos igual", "No se puede comparar"),
                    answerIndex = 1,
                    art = "duelo"
                )
            )
        )

        // ---- m_r5: decimales --------------------------------------------------------
        add(
            Challenge(
                id = "c_r5_1", missionId = "m_r5", order = 1, kind = GameKind.FRACTION_LINE,
                prompt = "Coloca 0,7 en la recta.",
                explanation = "0,7 son 7 decimas, es decir 7/10. Los decimales son fracciones " +
                    "de denominador 10, 100, 1000...",
                hint = "Divide el tramo en 10 y avanza 7.",
                payload = FractionLinePayload(denominator = 10, numerator = 7, wholes = 1, decimalLabels = true)
            )
        )
        add(
            Challenge(
                id = "c_r5_2", missionId = "m_r5", order = 2, kind = GameKind.FRACTION_LINE,
                prompt = "Coloca 1,4 en la recta.",
                explanation = "1,4 es un entero y cuatro decimas: 14/10.",
                hint = "Pasa del 1 y avanza cuatro marcas mas.",
                payload = FractionLinePayload(denominator = 10, numerator = 14, wholes = 2, decimalLabels = true)
            )
        )
        add(
            Challenge(
                id = "c_r5_3", missionId = "m_r5", order = 3, kind = GameKind.FRACTION_PIE,
                prompt = "Pinta la parte que equivale a 0,25.",
                explanation = "0,25 son 25 centesimas, que simplificado es 1/4.",
                hint = "Un cuarto de la figura.",
                payload = FractionPiePayload(
                    PieShape.BARRA, parts = 4, targetNumerator = 25, targetDenominator = 100,
                    mode = FractionMode.EQUIVALENTE
                )
            )
        )
        add(
            Challenge(
                id = "c_r5_4", missionId = "m_r5", order = 4, kind = GameKind.QUIZ,
                prompt = "Cual de estos numeros es mayor: 0,5 o 0,45?",
                explanation = "0,5 son 50 centesimas y 0,45 son 45 centesimas. Tener mas " +
                    "cifras no significa ser mas grande.",
                hint = "Escribe 0,5 como 0,50 y compara.",
                payload = QuizPayload(
                    options = listOf("0,5", "0,45", "Son iguales", "0,45 porque tiene mas cifras"),
                    answerIndex = 0,
                    art = "rio"
                )
            )
        )
        add(
            Challenge(
                id = "c_r5_5", missionId = "m_r5", order = 5, kind = GameKind.FRACTION_LINE,
                prompt = "Coloca 3/10 en la recta de decimas.",
                explanation = "3/10 se escribe 0,3. Fraccion y decimal son dos formas de " +
                    "escribir el mismo numero.",
                hint = "Tres marcas de las diez.",
                payload = FractionLinePayload(denominator = 10, numerator = 3, wholes = 1, decimalLabels = true)
            )
        )
    }
}
