package com.matelab.islas.domain.content

import com.matelab.islas.domain.model.AngleDialPayload
import com.matelab.islas.domain.model.BucketSpec
import com.matelab.islas.domain.model.Challenge
import com.matelab.islas.domain.model.Difficulty
import com.matelab.islas.domain.model.GameKind
import com.matelab.islas.domain.model.GeoObjective
import com.matelab.islas.domain.model.GeoboardPayload
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.QuizPayload
import com.matelab.islas.domain.model.ShapeCriterion
import com.matelab.islas.domain.model.ShapeSortPayload
import com.matelab.islas.domain.model.ShapeSpec
import com.matelab.islas.domain.model.SymmetryAxis
import com.matelab.islas.domain.model.SymmetryPayload
import com.matelab.islas.domain.model.World
import com.matelab.islas.domain.model.WorldTheme

/**
 * Isla 1: Bahia de las Formas.
 * Geometria plana: poligonos, angulos, simetria, perimetro y area.
 */
internal object CatalogFormas {

    const val WORLD_ID = "w_formas"

    val world = World(
        id = WORLD_ID,
        order = 1,
        name = "Bahia de las Formas",
        subtitle = "Poligonos, angulos y simetria",
        description = "Un faro roto ilumina una bahia llena de figuras flotantes. " +
            "Kubo necesita clasificarlas para volver a encender la luz.",
        theme = WorldTheme.FORMAS,
        xpToUnlock = 0
    )

    val missions = listOf(
        Mission(
            id = "m_f1",
            worldId = WORLD_ID,
            order = 1,
            name = "El faro de los poligonos",
            goal = "Clasifica las figuras por sus lados y sus angulos",
            briefing = "Las lentes del faro se han soltado. Ordenalas y volvera la luz.",
            difficulty = Difficulty.EXPLORADOR,
            rewardCollectibleId = "cr_faro"
        ),
        Mission(
            id = "m_f2",
            worldId = WORLD_ID,
            order = 2,
            name = "Gomas en el geoplano",
            goal = "Construye figuras con el area exacta",
            briefing = "Este tablero de clavos mide terrenos. Vamos a marcar parcelas.",
            difficulty = Difficulty.EXPLORADOR,
            rewardCollectibleId = "cr_geoplano"
        ),
        Mission(
            id = "m_f3",
            worldId = WORLD_ID,
            order = 3,
            name = "El transportador perdido",
            goal = "Gira el rayo hasta el angulo pedido",
            briefing = "Sin transportador no hay rumbo. Ajusta el rayo grado a grado.",
            difficulty = Difficulty.AVENTURERO,
            requires = listOf("m_f1"),
            rewardCollectibleId = "cr_transportador"
        ),
        Mission(
            id = "m_f4",
            worldId = WORLD_ID,
            order = 4,
            name = "Mosaicos del espejo",
            goal = "Completa la mitad que falta del mosaico",
            briefing = "El suelo del faro es un mosaico simetrico. Le falta media pieza.",
            difficulty = Difficulty.AVENTURERO,
            requires = listOf("m_f1"),
            rewardCollectibleId = "cr_espejo"
        ),
        Mission(
            id = "m_f5",
            worldId = WORLD_ID,
            order = 5,
            name = "Arquitectos de la bahia",
            goal = "Domina area y perimetro a la vez",
            briefing = "Hay que disenar el muelle nuevo. Cada medida cuenta.",
            difficulty = Difficulty.MAESTRO,
            requires = listOf("m_f2", "m_f3"),
            rewardCollectibleId = "cr_muelle"
        )
    )

    // --------------------------------------------------------------- FIGURAS

    private val triEquilatero = ShapeSpec("s_tri_eq", "triangulo equilatero", 3, true, 0)
    private val triRectangulo = ShapeSpec("s_tri_re", "triangulo rectangulo", 3, false, 1, rotation = 15)
    private val triEscaleno = ShapeSpec("s_tri_es", "triangulo escaleno", 3, false, 0, rotation = 200)
    private val cuadrado = ShapeSpec("s_cuad", "cuadrado", 4, true, 4)
    private val rectangulo = ShapeSpec("s_rect", "rectangulo", 4, false, 4, rotation = 0)
    private val rombo = ShapeSpec("s_rombo", "rombo", 4, true, 0, rotation = 45)
    private val trapecio = ShapeSpec("s_trap", "trapecio", 4, false, 2, rotation = 0)
    private val paralelogramo = ShapeSpec("s_para", "paralelogramo", 4, false, 0, rotation = 10)
    private val pentagono = ShapeSpec("s_pent", "pentagono regular", 5, true, 0)
    private val hexagono = ShapeSpec("s_hex", "hexagono regular", 6, true, 0, rotation = 30)
    private val circulo = ShapeSpec("s_circ", "circulo", 0, false, 0, curved = true)
    private val ovalo = ShapeSpec("s_oval", "ovalo", 0, false, 0, curved = true, rotation = 20)
    private val semicirculo = ShapeSpec("s_semi", "semicirculo", 0, false, 0, curved = true, rotation = 180)

    // ---------------------------------------------------------------- RETOS

    val challenges = buildList {

        // ---- m_f1: clasificar figuras --------------------------------------
        add(
            Challenge(
                id = "c_f1_1", missionId = "m_f1", order = 1, kind = GameKind.SHAPE_SORT,
                prompt = "Arrastra cada lente a su caja segun cuantos lados tiene.",
                explanation = "Un poligono se nombra por su numero de lados: 3 lados es un " +
                    "triangulo y 4 lados es un cuadrilatero. Los lados se cuentan siguiendo " +
                    "el borde sin levantar el dedo.",
                hint = "Recorre el borde con el dedo y cuenta cada tramo recto.",
                payload = ShapeSortPayload(
                    shapes = listOf(triEquilatero, cuadrado, triEscaleno, rombo, triRectangulo, trapecio),
                    buckets = listOf(
                        BucketSpec("b3", "3 lados", ShapeCriterion.NUM_LADOS, 3),
                        BucketSpec("b4", "4 lados", ShapeCriterion.NUM_LADOS, 4)
                    )
                )
            )
        )
        add(
            Challenge(
                id = "c_f1_2", missionId = "m_f1", order = 2, kind = GameKind.SHAPE_SORT,
                prompt = "Separa las figuras con esquina recta de las que son curvas.",
                explanation = "Un angulo recto mide 90 grados: es la esquina de una hoja de " +
                    "papel. Las figuras curvas, como el circulo, no tienen esquinas.",
                hint = "Prueba a apoyar mentalmente la esquina de una hoja sobre la figura.",
                payload = ShapeSortPayload(
                    shapes = listOf(cuadrado, circulo, rectangulo, ovalo, triRectangulo, semicirculo),
                    buckets = listOf(
                        BucketSpec("bR", "Tiene esquina recta", ShapeCriterion.TIENE_ANGULO_RECTO),
                        BucketSpec("bC", "Es curva", ShapeCriterion.ES_CURVA)
                    )
                )
            )
        )
        add(
            Challenge(
                id = "c_f1_3", missionId = "m_f1", order = 3, kind = GameKind.SHAPE_SORT,
                prompt = "Coloca a la izquierda las figuras con todos los lados iguales.",
                explanation = "Una figura es regular cuando todos sus lados miden lo mismo. " +
                    "El rectangulo tiene 4 lados, pero dos largos y dos cortos: no es regular.",
                hint = "Compara los lados de dos en dos: si uno es mas largo, no son iguales.",
                payload = ShapeSortPayload(
                    shapes = listOf(triEquilatero, rectangulo, pentagono, trapecio, hexagono, paralelogramo),
                    buckets = listOf(
                        BucketSpec("bIg", "Lados iguales", ShapeCriterion.LADOS_IGUALES),
                        BucketSpec("b4b", "4 lados distintos", ShapeCriterion.NUM_LADOS, 4)
                    )
                )
            )
        )
        add(
            Challenge(
                id = "c_f1_4", missionId = "m_f1", order = 4, kind = GameKind.QUIZ,
                prompt = "El cristal del faro tiene 6 lados iguales. Como se llama?",
                explanation = "Hexagono viene de 'hexa', que significa seis. Un hexagono " +
                    "regular tiene 6 lados iguales y 6 vertices.",
                hint = "Piensa en la celdilla de un panal de abejas.",
                payload = QuizPayload(
                    options = listOf("Pentagono", "Hexagono", "Octogono", "Rombo"),
                    answerIndex = 1,
                    art = "faro"
                )
            )
        )
        add(
            Challenge(
                id = "c_f1_5", missionId = "m_f1", order = 5, kind = GameKind.QUIZ,
                prompt = "Cuantos vertices tiene un poligono de 5 lados?",
                explanation = "En cualquier poligono hay tantos vertices como lados. " +
                    "Un pentagono tiene 5 lados y 5 vertices.",
                hint = "Cada vez que dos lados se juntan aparece un vertice.",
                payload = QuizPayload(
                    options = listOf("4", "5", "6", "10"),
                    answerIndex = 1,
                    art = "poligono"
                )
            )
        )

        // ---- m_f2: geoplano -------------------------------------------------
        add(
            Challenge(
                id = "c_f2_1", missionId = "m_f2", order = 1, kind = GameKind.GEOBOARD,
                prompt = "Marca una parcela de 4 cuadraditos de area.",
                explanation = "El area cuenta cuantos cuadraditos caben dentro. Un cuadrado " +
                    "de 2 por 2 encierra 4 cuadraditos.",
                hint = "Prueba con un cuadrado de 2 clavos de ancho y 2 de alto.",
                payload = GeoboardPayload(grid = 6, objective = GeoObjective.AREA, target = 4.0)
            )
        )
        add(
            Challenge(
                id = "c_f2_2", missionId = "m_f2", order = 2, kind = GameKind.GEOBOARD,
                prompt = "Ahora una parcela de 6 cuadraditos de area.",
                explanation = "6 se puede repartir de varias formas: 2 x 3 o 6 x 1. " +
                    "Figuras distintas pueden tener la misma area.",
                hint = "Un rectangulo de 2 de alto y 3 de ancho ya vale.",
                payload = GeoboardPayload(grid = 6, objective = GeoObjective.AREA, target = 6.0)
            )
        )
        add(
            Challenge(
                id = "c_f2_3", missionId = "m_f2", order = 3, kind = GameKind.GEOBOARD,
                prompt = "Construye un triangulo con area 2.",
                explanation = "El triangulo ocupa la mitad del rectangulo que lo rodea. " +
                    "Si el rectangulo tiene area 4, el triangulo tiene area 2.",
                hint = "Toma un rectangulo de 2 x 2 y quedate con la mitad, en diagonal.",
                payload = GeoboardPayload(grid = 6, objective = GeoObjective.AREA, target = 2.0, minVertices = 3)
            )
        )
        add(
            Challenge(
                id = "c_f2_4", missionId = "m_f2", order = 4, kind = GameKind.GEOBOARD,
                prompt = "Encierra una figura de 5 lados.",
                explanation = "Un pentagono tiene 5 lados. En el geoplano no hace falta que " +
                    "sean iguales: sigue siendo un pentagono.",
                hint = "Empieza con un cuadrado y anade un clavo mas fuera de una esquina.",
                payload = GeoboardPayload(grid = 6, objective = GeoObjective.LADOS, target = 5.0, unitLabel = "lados")
            )
        )
        add(
            Challenge(
                id = "c_f2_5", missionId = "m_f2", order = 5, kind = GameKind.GEOBOARD,
                prompt = "Una parcela grande: area 9 cuadraditos.",
                explanation = "9 es 3 x 3, asi que un cuadrado de 3 de lado encaja perfecto. " +
                    "Los numeros que forman cuadrados se llaman cuadrados perfectos.",
                hint = "Cuenta 3 huecos hacia la derecha y 3 hacia abajo.",
                payload = GeoboardPayload(grid = 6, objective = GeoObjective.AREA, target = 9.0)
            )
        )

        // ---- m_f3: angulos ---------------------------------------------------
        add(
            Challenge(
                id = "c_f3_1", missionId = "m_f3", order = 1, kind = GameKind.ANGLE_DIAL,
                prompt = "Gira el rayo hasta formar un angulo recto.",
                explanation = "El angulo recto mide 90 grados. Es el que forman las paredes " +
                    "con el suelo o las agujas del reloj a las 3 en punto.",
                hint = "Es justo un cuarto de vuelta completa.",
                payload = AngleDialPayload(targetDegrees = 90, tolerance = 4)
            )
        )
        add(
            Challenge(
                id = "c_f3_2", missionId = "m_f3", order = 2, kind = GameKind.ANGLE_DIAL,
                prompt = "Ahora un angulo de 45 grados.",
                explanation = "45 grados es la mitad de un angulo recto. Es un angulo agudo " +
                    "porque mide menos de 90 grados.",
                hint = "Divide el angulo recto justo por la mitad.",
                payload = AngleDialPayload(targetDegrees = 45, tolerance = 4, askClassification = true)
            )
        )
        add(
            Challenge(
                id = "c_f3_3", missionId = "m_f3", order = 3, kind = GameKind.ANGLE_DIAL,
                prompt = "Abre el rayo hasta 135 grados.",
                explanation = "135 grados pasa de 90, asi que es un angulo obtuso. " +
                    "Se puede pensar como 90 + 45.",
                hint = "Llega primero a 90 y sigue medio angulo recto mas.",
                payload = AngleDialPayload(targetDegrees = 135, tolerance = 5, askClassification = true)
            )
        )
        add(
            Challenge(
                id = "c_f3_4", missionId = "m_f3", order = 4, kind = GameKind.ANGLE_DIAL,
                prompt = "Coloca el rayo en 30 grados.",
                explanation = "30 grados es un tercio del angulo recto. Cuanto mas cerrado " +
                    "esta el angulo, menos grados mide.",
                hint = "Muy poquito: menos de la mitad de la mitad de un recto.",
                payload = AngleDialPayload(targetDegrees = 30, tolerance = 4)
            )
        )
        add(
            Challenge(
                id = "c_f3_5", missionId = "m_f3", order = 5, kind = GameKind.ANGLE_DIAL,
                prompt = "Forma un angulo llano.",
                explanation = "El angulo llano mide 180 grados: los dos lados forman una " +
                    "linea recta. Es media vuelta completa.",
                hint = "Sigue girando hasta que el rayo apunte al lado contrario.",
                payload = AngleDialPayload(targetDegrees = 180, tolerance = 5, askClassification = true)
            )
        )

        // ---- m_f4: simetria --------------------------------------------------
        add(
            Challenge(
                id = "c_f4_1", missionId = "m_f4", order = 1, kind = GameKind.SYMMETRY,
                prompt = "Completa el mosaico al otro lado del espejo.",
                explanation = "En una simetria, cada baldosa tiene su reflejo a la misma " +
                    "distancia del eje, pero al otro lado.",
                hint = "Cuenta cuantas casillas hay del eje a la baldosa y repite al otro lado.",
                payload = SymmetryPayload(
                    rows = 6, cols = 6, axis = SymmetryAxis.VERTICAL,
                    given = listOf(2, 7, 8, 12, 13, 14, 20, 26)
                )
            )
        )
        add(
            Challenge(
                id = "c_f4_2", missionId = "m_f4", order = 2, kind = GameKind.SYMMETRY,
                prompt = "Refleja la vela del barco.",
                explanation = "El eje de simetria actua como un espejo: lo que esta cerca " +
                    "del eje sigue cerca, y lo que esta lejos sigue lejos.",
                hint = "Empieza por las baldosas pegadas al eje.",
                payload = SymmetryPayload(
                    rows = 6, cols = 6, axis = SymmetryAxis.VERTICAL,
                    given = listOf(2, 8, 13, 14, 19, 20, 25, 26, 31, 32)
                )
            )
        )
        add(
            Challenge(
                id = "c_f4_3", missionId = "m_f4", order = 3, kind = GameKind.SYMMETRY,
                prompt = "Esta vez el espejo es horizontal.",
                explanation = "El eje tambien puede estar tumbado. Entonces el reflejo se " +
                    "produce arriba y abajo en vez de a izquierda y derecha.",
                hint = "Mira las filas: la de arriba se refleja en la de abajo.",
                payload = SymmetryPayload(
                    rows = 6, cols = 6, axis = SymmetryAxis.HORIZONTAL,
                    given = listOf(2, 3, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17)
                )
            )
        )
        add(
            Challenge(
                id = "c_f4_4", missionId = "m_f4", order = 4, kind = GameKind.SYMMETRY,
                prompt = "Un mosaico mas grande para el suelo del faro.",
                explanation = "Aunque la figura sea complicada, la regla no cambia: " +
                    "cada casilla pintada necesita su pareja al otro lado del eje.",
                hint = "Ve fila por fila, sin saltarte ninguna.",
                payload = SymmetryPayload(
                    rows = 8, cols = 8, axis = SymmetryAxis.VERTICAL,
                    given = listOf(3, 10, 11, 17, 18, 19, 24, 25, 26, 27, 35, 43, 51, 58, 59)
                )
            )
        )
        add(
            Challenge(
                id = "c_f4_5", missionId = "m_f4", order = 5, kind = GameKind.QUIZ,
                prompt = "Cuantos ejes de simetria tiene un cuadrado?",
                explanation = "El cuadrado se puede doblar por la mitad de 4 formas distintas: " +
                    "dos por la mitad y dos en diagonal. Son 4 ejes de simetria.",
                hint = "Imagina que doblas una servilleta cuadrada de todas las formas posibles.",
                payload = QuizPayload(
                    options = listOf("1", "2", "4", "8"),
                    answerIndex = 2,
                    art = "espejo"
                )
            )
        )

        // ---- m_f5: maestria --------------------------------------------------
        add(
            Challenge(
                id = "c_f5_1", missionId = "m_f5", order = 1, kind = GameKind.GEOBOARD,
                prompt = "Disena un muelle con perimetro 12.",
                explanation = "El perimetro es lo que mide el borde. Un rectangulo de 4 x 2 " +
                    "tiene perimetro 4 + 2 + 4 + 2 = 12.",
                hint = "Suma los cuatro lados: dos largos y dos cortos.",
                payload = GeoboardPayload(
                    grid = 6, objective = GeoObjective.PERIMETRO, target = 12.0, unitLabel = "unidades"
                )
            )
        )
        add(
            Challenge(
                id = "c_f5_2", missionId = "m_f5", order = 2, kind = GameKind.GEOBOARD,
                prompt = "Un almacen de area 8.",
                explanation = "Area 8 admite 2 x 4 o 1 x 8. Aunque el area sea la misma, " +
                    "el perimetro cambia: por eso los arquitectos miden las dos cosas.",
                hint = "Busca dos numeros que multiplicados den 8.",
                payload = GeoboardPayload(grid = 6, objective = GeoObjective.AREA, target = 8.0)
            )
        )
        add(
            Challenge(
                id = "c_f5_3", missionId = "m_f5", order = 3, kind = GameKind.GEOBOARD,
                prompt = "Una plaza de perimetro 16.",
                explanation = "Con perimetro 16 caben muchas formas: 4 x 4, 5 x 3, 6 x 2... " +
                    "Todas tienen el mismo borde pero distinta superficie.",
                hint = "Si los cuatro lados son iguales, cada uno mide 16 : 4 = 4.",
                payload = GeoboardPayload(
                    grid = 6, objective = GeoObjective.PERIMETRO, target = 16.0, unitLabel = "unidades"
                )
            )
        )
        add(
            Challenge(
                id = "c_f5_4", missionId = "m_f5", order = 4, kind = GameKind.ANGLE_DIAL,
                prompt = "El faro debe girar 270 grados.",
                explanation = "270 grados son tres cuartos de vuelta. Es un angulo reflejo, " +
                    "porque pasa de 180 grados.",
                hint = "Una vuelta completa son 360. Quitale un cuarto.",
                payload = AngleDialPayload(targetDegrees = 270, tolerance = 6)
            )
        )
        add(
            Challenge(
                id = "c_f5_5", missionId = "m_f5", order = 5, kind = GameKind.QUIZ,
                prompt = "Dos parcelas tienen area 12. Una es 3 x 4 y otra 2 x 6. Que ocurre?",
                explanation = "Misma area no significa mismo perimetro. La de 3 x 4 tiene " +
                    "perimetro 14 y la de 2 x 6 tiene perimetro 16.",
                hint = "Calcula el borde de cada una y comparalos.",
                payload = QuizPayload(
                    options = listOf(
                        "Tienen el mismo perimetro",
                        "La de 2 x 6 tiene mas perimetro",
                        "La de 3 x 4 tiene mas perimetro",
                        "No se puede saber"
                    ),
                    answerIndex = 1,
                    art = "planos"
                )
            )
        )
    }
}
