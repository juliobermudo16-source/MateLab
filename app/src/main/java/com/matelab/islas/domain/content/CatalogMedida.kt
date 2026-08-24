package com.matelab.islas.domain.content

import com.matelab.islas.domain.model.BalancePayload
import com.matelab.islas.domain.model.Challenge
import com.matelab.islas.domain.model.ClockMode
import com.matelab.islas.domain.model.ClockPayload
import com.matelab.islas.domain.model.Difficulty
import com.matelab.islas.domain.model.GameKind
import com.matelab.islas.domain.model.Mission
import com.matelab.islas.domain.model.QuizPayload
import com.matelab.islas.domain.model.RulerPayload
import com.matelab.islas.domain.model.World
import com.matelab.islas.domain.model.WorldTheme

/**
 * Isla 2: Puerto Medida.
 * Longitud con regla, masa con balanza, tiempo con reloj y conversiones.
 */
internal object CatalogMedida {

    const val WORLD_ID = "w_medida"

    val world = World(
        id = WORLD_ID,
        order = 2,
        name = "Puerto Medida",
        subtitle = "Reglas, balanzas y relojes",
        description = "En el puerto todo se pesa, se mide y se entrega a su hora. " +
            "Sin medidas exactas, los barcos no zarpan.",
        theme = WorldTheme.MEDIDA,
        xpToUnlock = 120
    )

    val missions = listOf(
        Mission(
            id = "m_m1", worldId = WORLD_ID, order = 1,
            name = "La regla de Kubo",
            goal = "Mide objetos colocando bien el cero",
            briefing = "Se han perdido las etiquetas del almacen. Hay que medirlo todo.",
            difficulty = Difficulty.EXPLORADOR,
            rewardCollectibleId = "cr_regla"
        ),
        Mission(
            id = "m_m2", worldId = WORLD_ID, order = 2,
            name = "La balanza del puerto",
            goal = "Equilibra la balanza con las pesas justas",
            briefing = "Sin peso exacto no hay factura. Ayuda al capataz con las pesas.",
            difficulty = Difficulty.EXPLORADOR,
            rewardCollectibleId = "cr_balanza"
        ),
        Mission(
            id = "m_m3", worldId = WORLD_ID, order = 3,
            name = "El reloj de la torre",
            goal = "Coloca las manecillas y calcula duraciones",
            briefing = "El reloj de la torre se atrasa. Ponlo en hora, marinero.",
            difficulty = Difficulty.AVENTURERO,
            requires = listOf("m_m1"),
            rewardCollectibleId = "cr_reloj"
        ),
        Mission(
            id = "m_m4", worldId = WORLD_ID, order = 4,
            name = "La escalera de unidades",
            goal = "Pasa de milimetros a metros y de gramos a kilos",
            briefing = "Cada peldano multiplica o divide por 10. Sube y baja con cuidado.",
            difficulty = Difficulty.AVENTURERO,
            requires = listOf("m_m1", "m_m2"),
            rewardCollectibleId = "cr_escalera"
        ),
        Mission(
            id = "m_m5", worldId = WORLD_ID, order = 5,
            name = "Cargamento exacto",
            goal = "Mide, pesa y entrega a tiempo",
            briefing = "El ultimo barco zarpa al amanecer. No puede fallar ni un gramo.",
            difficulty = Difficulty.MAESTRO,
            requires = listOf("m_m3", "m_m4"),
            rewardCollectibleId = "cr_cargamento"
        )
    )

    val challenges = buildList {

        // ---- m_m1: regla -----------------------------------------------------
        add(
            Challenge(
                id = "c_m1_1", missionId = "m_m1", order = 1, kind = GameKind.RULER,
                prompt = "Mide el lapiz. Cuantos centimetros tiene?",
                explanation = "Para medir bien, el 0 de la regla debe coincidir con el " +
                    "principio del objeto. Si empiezas en el 1, la medida sale mal.",
                hint = "Arrastra la regla hasta que el 0 toque la punta del lapiz.",
                payload = RulerPayload(objectMm = 120, toleranceMm = 2, answerUnit = "cm", objectKind = "lapiz")
            )
        )
        add(
            Challenge(
                id = "c_m1_2", missionId = "m_m1", order = 2, kind = GameKind.RULER,
                prompt = "Ahora el clip. Responde en milimetros.",
                explanation = "Cada centimetro tiene 10 rayitas pequenas: son los milimetros. " +
                    "Un clip suele medir unos 3 centimetros, es decir 30 milimetros.",
                hint = "Cuenta las rayitas pequenas desde el cero.",
                payload = RulerPayload(objectMm = 32, toleranceMm = 2, answerUnit = "mm", objectKind = "clip")
            )
        )
        add(
            Challenge(
                id = "c_m1_3", missionId = "m_m1", order = 3, kind = GameKind.RULER,
                prompt = "Mide la cinta del paquete.",
                explanation = "Cuando el objeto acaba entre dos numeros, se usan los " +
                    "decimales: 9 centimetros y 5 rayitas son 9,5 cm.",
                hint = "Si acaba justo en la mitad, la medida lleva coma 5.",
                payload = RulerPayload(objectMm = 95, toleranceMm = 2, answerUnit = "cm", objectKind = "cinta")
            )
        )
        add(
            Challenge(
                id = "c_m1_4", missionId = "m_m1", order = 4, kind = GameKind.RULER,
                prompt = "El gusano de la bodega no para quieto. Cuanto mide?",
                explanation = "Aunque el objeto este torcido o se mueva, la medida se toma " +
                    "en linea recta de un extremo al otro.",
                hint = "Alinea el cero y mira donde termina la cola.",
                payload = RulerPayload(objectMm = 65, toleranceMm = 3, answerUnit = "cm", objectKind = "gusano")
            )
        )
        add(
            Challenge(
                id = "c_m1_5", missionId = "m_m1", order = 5, kind = GameKind.QUIZ,
                prompt = "Cuantos milimetros hay en 7 centimetros?",
                explanation = "1 centimetro son 10 milimetros, asi que 7 cm son 7 x 10 = 70 mm.",
                hint = "Multiplica por 10.",
                payload = QuizPayload(
                    options = listOf("7 mm", "17 mm", "70 mm", "700 mm"),
                    answerIndex = 2,
                    art = "cinta"
                )
            )
        )

        // ---- m_m2: balanza ----------------------------------------------------
        add(
            Challenge(
                id = "c_m2_1", missionId = "m_m2", order = 1, kind = GameKind.BALANCE,
                prompt = "Equilibra la balanza con el saco de arroz.",
                explanation = "Una balanza se equilibra cuando los dos platos pesan lo mismo. " +
                    "300 g se consiguen con 200 g + 100 g.",
                hint = "Empieza siempre por la pesa mas grande que no se pase.",
                payload = BalancePayload(
                    leftGrams = 300, leftLabel = "Saco de arroz",
                    weights = listOf(500, 200, 100, 50)
                )
            )
        )
        add(
            Challenge(
                id = "c_m2_2", missionId = "m_m2", order = 2, kind = GameKind.BALANCE,
                prompt = "Este bidon pesa mas. Encuentra las pesas exactas.",
                explanation = "750 g = 500 g + 200 g + 50 g. Cuando te pasas, la balanza se " +
                    "inclina al otro lado: quita una pesa y prueba con otra menor.",
                hint = "Coloca la de 500 y mira cuanto falta todavia.",
                payload = BalancePayload(
                    leftGrams = 750, leftLabel = "Bidon de aceite",
                    weights = listOf(500, 200, 100, 50)
                )
            )
        )
        add(
            Challenge(
                id = "c_m2_3", missionId = "m_m2", order = 3, kind = GameKind.BALANCE,
                prompt = "Un kilo justo. Cuidado, hay muchas maneras.",
                explanation = "1 kilogramo son 1000 gramos. Puedes formarlo con 500 + 500 " +
                    "o con 500 + 200 + 200 + 100.",
                hint = "Recuerda: 1 kg = 1000 g.",
                payload = BalancePayload(
                    leftGrams = 1000, leftLabel = "Caja de clavos (1 kg)",
                    weights = listOf(500, 200, 100, 50)
                )
            )
        )
        add(
            Challenge(
                id = "c_m2_4", missionId = "m_m2", order = 4, kind = GameKind.BALANCE,
                prompt = "El ancla pequena pesa 1,35 kg. Equilibrala.",
                explanation = "1,35 kg son 1350 g. Se forma con 1000 + 200 + 100 + 50.",
                hint = "Pasa primero los kilos a gramos: 1,35 kg = 1350 g.",
                payload = BalancePayload(
                    leftGrams = 1350, leftLabel = "Ancla pequena",
                    weights = listOf(1000, 500, 200, 100, 50)
                )
            )
        )
        add(
            Challenge(
                id = "c_m2_5", missionId = "m_m2", order = 5, kind = GameKind.QUIZ,
                prompt = "Que pesa mas: 2 kg de plumas o 1800 g de plomo?",
                explanation = "2 kg son 2000 g y 2000 g es mas que 1800 g. El material da " +
                    "igual: para comparar hay que usar la misma unidad.",
                hint = "Pasa los kilos a gramos antes de comparar.",
                payload = QuizPayload(
                    options = listOf("Las plumas", "El plomo", "Pesan igual", "No se puede saber"),
                    answerIndex = 0,
                    art = "balanza"
                )
            )
        )

        // ---- m_m3: reloj ------------------------------------------------------
        add(
            Challenge(
                id = "c_m3_1", missionId = "m_m3", order = 1, kind = GameKind.CLOCK,
                prompt = "Pon el reloj a las 3 y cuarto.",
                explanation = "Y cuarto significa 15 minutos pasados. El minutero apunta al 3 " +
                    "porque cada numero vale 5 minutos: 3 x 5 = 15.",
                hint = "El minutero va al numero 3 del reloj.",
                payload = ClockPayload(
                    startHour = 12, startMinute = 0, mode = ClockMode.PONER_HORA,
                    targetHour = 3, targetMinute = 15
                )
            )
        )
        add(
            Challenge(
                id = "c_m3_2", missionId = "m_m3", order = 2, kind = GameKind.CLOCK,
                prompt = "El barco llega a las 7 y 40. Marca esa hora.",
                explanation = "40 minutos son 8 numeros del reloj (8 x 5 = 40). El minutero " +
                    "apunta al 8 y la aguja pequena ya casi llega al 8.",
                hint = "Divide 40 entre 5 para saber a que numero va el minutero.",
                payload = ClockPayload(
                    startHour = 12, startMinute = 0, mode = ClockMode.PONER_HORA,
                    targetHour = 7, targetMinute = 40
                )
            )
        )
        add(
            Challenge(
                id = "c_m3_3", missionId = "m_m3", order = 3, kind = GameKind.CLOCK,
                prompt = "Son las 2:50 y la descarga dura 25 minutos. A que hora acaba?",
                explanation = "2:50 + 25 min = 3:15. Al pasar de 60 minutos cambia la hora: " +
                    "quedan 10 minutos para las 3 y sobran 15.",
                hint = "Primero llega a las 3 en punto y despues suma lo que sobre.",
                payload = ClockPayload(
                    startHour = 2, startMinute = 50, mode = ClockMode.AVANZAR, deltaMinutes = 25
                )
            )
        )
        add(
            Challenge(
                id = "c_m3_4", missionId = "m_m3", order = 4, kind = GameKind.CLOCK,
                prompt = "Marca las 11 y 5.",
                explanation = "Las horas en punto y los 5 minutos son faciles de confundir: " +
                    "la aguja larga marca los minutos y la corta las horas.",
                hint = "La aguja larga apunta al 1, que vale 5 minutos.",
                payload = ClockPayload(
                    startHour = 12, startMinute = 0, mode = ClockMode.PONER_HORA,
                    targetHour = 11, targetMinute = 5
                )
            )
        )
        add(
            Challenge(
                id = "c_m3_5", missionId = "m_m3", order = 5, kind = GameKind.CLOCK,
                prompt = "Salida a las 9:30. El viaje dura 45 minutos. Hora de llegada?",
                explanation = "9:30 + 45 min = 10:15. Media hora mas nos lleva a las 10:00 " +
                    "y quedan 15 minutos.",
                hint = "Suma primero 30 minutos y luego los 15 que faltan.",
                payload = ClockPayload(
                    startHour = 9, startMinute = 30, mode = ClockMode.AVANZAR, deltaMinutes = 45
                )
            )
        )

        // ---- m_m4: escalera de unidades ---------------------------------------
        add(
            Challenge(
                id = "c_m4_1", missionId = "m_m4", order = 1, kind = GameKind.RULER,
                prompt = "Mide la llave y responde en milimetros.",
                explanation = "Medir en milimetros da mas precision. 5,5 cm son 55 mm: " +
                    "al bajar un peldano de la escalera se multiplica por 10.",
                hint = "Cuenta primero los centimetros y despues las rayitas.",
                payload = RulerPayload(objectMm = 55, toleranceMm = 2, answerUnit = "mm", objectKind = "llave")
            )
        )
        add(
            Challenge(
                id = "c_m4_2", missionId = "m_m4", order = 2, kind = GameKind.QUIZ,
                prompt = "Cuantos centimetros son 3 metros?",
                explanation = "1 metro son 100 centimetros, asi que 3 m = 3 x 100 = 300 cm.",
                hint = "Del metro al centimetro se baja dos peldanos: x10 y otra vez x10.",
                payload = QuizPayload(
                    options = listOf("30 cm", "300 cm", "3000 cm", "0,3 cm"),
                    answerIndex = 1,
                    art = "escalera"
                )
            )
        )
        add(
            Challenge(
                id = "c_m4_3", missionId = "m_m4", order = 3, kind = GameKind.BALANCE,
                prompt = "Medio kilo exacto en el plato.",
                explanation = "Medio kilo son 500 g. Tambien se escribe 0,5 kg.",
                hint = "La mitad de 1000 es 500.",
                payload = BalancePayload(
                    leftGrams = 500, leftLabel = "Bolsa de sal (0,5 kg)",
                    weights = listOf(500, 200, 100, 50)
                )
            )
        )
        add(
            Challenge(
                id = "c_m4_4", missionId = "m_m4", order = 4, kind = GameKind.QUIZ,
                prompt = "Una botella tiene 1,5 litros. Cuantos mililitros son?",
                explanation = "1 litro son 1000 ml, asi que 1,5 L = 1500 ml. La capacidad " +
                    "funciona con la misma escalera que la longitud.",
                hint = "1 L = 1000 ml. Suma la mitad de 1000.",
                payload = QuizPayload(
                    options = listOf("15 ml", "150 ml", "1500 ml", "15000 ml"),
                    answerIndex = 2,
                    art = "mercado"
                )
            )
        )
        add(
            Challenge(
                id = "c_m4_5", missionId = "m_m4", order = 5, kind = GameKind.RULER,
                prompt = "Mide el ancho de la tabla en centimetros.",
                explanation = "Cuando el resultado no es exacto se usa la coma. " +
                    "78 mm son 7,8 cm.",
                hint = "Cuenta 7 centimetros completos y 8 rayitas mas.",
                payload = RulerPayload(objectMm = 78, toleranceMm = 2, answerUnit = "cm", objectKind = "tabla")
            )
        )

        // ---- m_m5: maestria ----------------------------------------------------
        add(
            Challenge(
                id = "c_m5_1", missionId = "m_m5", order = 1, kind = GameKind.BALANCE,
                prompt = "El cofre pesa 1,75 kg. Equilibra la balanza.",
                explanation = "1,75 kg = 1750 g = 1000 + 500 + 200 + 50.",
                hint = "Pasa a gramos y ve de la pesa mayor a la menor.",
                payload = BalancePayload(
                    leftGrams = 1750, leftLabel = "Cofre del capitan",
                    weights = listOf(1000, 500, 200, 100, 50)
                )
            )
        )
        add(
            Challenge(
                id = "c_m5_2", missionId = "m_m5", order = 2, kind = GameKind.RULER,
                prompt = "La cuerda de amarre. Cuanto mide en centimetros?",
                explanation = "Medir siempre desde el cero evita el error mas comun del puerto.",
                hint = "Coloca el cero y sigue la cuerda hasta el final.",
                payload = RulerPayload(objectMm = 137, toleranceMm = 3, answerUnit = "cm", objectKind = "cuerda")
            )
        )
        add(
            Challenge(
                id = "c_m5_3", missionId = "m_m5", order = 3, kind = GameKind.CLOCK,
                prompt = "Zarpa a las 5:20 y navega 1 hora y 10 minutos. Cuando llega?",
                explanation = "5:20 + 70 minutos = 6:30. Una hora y diez son 70 minutos.",
                hint = "Suma primero la hora entera y despues los 10 minutos.",
                payload = ClockPayload(
                    startHour = 5, startMinute = 20, mode = ClockMode.AVANZAR, deltaMinutes = 70
                )
            )
        )
        add(
            Challenge(
                id = "c_m5_4", missionId = "m_m5", order = 4, kind = GameKind.BALANCE,
                prompt = "Ultimo bulto: 2,4 kg.",
                explanation = "2,4 kg = 2400 g = 1000 + 1000 + 200 + 200.",
                hint = "Puedes repetir la misma pesa varias veces.",
                payload = BalancePayload(
                    leftGrams = 2400, leftLabel = "Bulto de red",
                    weights = listOf(1000, 500, 200, 100), maxPerWeight = 4
                )
            )
        )
        add(
            Challenge(
                id = "c_m5_5", missionId = "m_m5", order = 5, kind = GameKind.QUIZ,
                prompt = "El barco carga 3 cajas de 750 g. Cuanto pesa la carga?",
                explanation = "750 x 3 = 2250 g, es decir 2,25 kg. Sumar tres veces lo mismo " +
                    "es multiplicar.",
                hint = "750 + 750 + 750, o mejor 750 x 3.",
                payload = QuizPayload(
                    options = listOf("2,25 kg", "1,5 kg", "22,5 kg", "750 kg"),
                    answerIndex = 0,
                    art = "mercado"
                )
            )
        )
    }
}
