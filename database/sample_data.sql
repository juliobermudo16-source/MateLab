-- =====================================================================
--  MateLab - Islas del Ingenio
--  Datos de ejemplo
--
--  Contiene el catalogo completo de islas, misiones, insignias y
--  cristales, una muestra representativa de retos con su JSON real y
--  un estado de jugador de ejemplo (nino que ya ha jugado un rato).
--
--  La app genera estos mismos INSERT desde
--  data/local/seed/DatabaseSeeder.kt la primera vez que se abre.
-- =====================================================================

PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------------
-- ISLAS
-- ---------------------------------------------------------------------
INSERT INTO world (id, order_index, name, subtitle, description, theme, xp_to_unlock) VALUES
 ('w_formas',   1, 'Bahia de las Formas', 'Poligonos, angulos y simetria',
  'Un faro roto ilumina una bahia llena de figuras flotantes. Kubo necesita clasificarlas para volver a encender la luz.',
  'FORMAS', 0),
 ('w_medida',   2, 'Puerto Medida', 'Reglas, balanzas y relojes',
  'En el puerto todo se pesa, se mide y se entrega a su hora. Sin medidas exactas, los barcos no zarpan.',
  'MEDIDA', 120),
 ('w_fraccion', 3, 'Volcan Fraccion', 'Partes, equivalencias y decimales',
  'La lava del volcan se enfria en losas que hay que repartir en partes iguales. Si el reparto falla, el puente no aguanta.',
  'FRACCION', 320),
 ('w_numeros',  4, 'Cueva de los Numeros', 'Valor posicional y patrones',
  'Dentro de la cueva los cristales crecen siguiendo reglas. Quien descubre la regla, controla la cueva.',
  'NUMEROS', 560);

-- ---------------------------------------------------------------------
-- MISIONES (19)
-- ---------------------------------------------------------------------
INSERT INTO mission (id, world_id, order_index, name, goal, briefing, difficulty, requires, reward_collectible_id) VALUES
 ('m_f1','w_formas',1,'El faro de los poligonos','Clasifica las figuras por sus lados y sus angulos','Las lentes del faro se han soltado. Ordenalas y volvera la luz.','EXPLORADOR','','cr_faro'),
 ('m_f2','w_formas',2,'Gomas en el geoplano','Construye figuras con el area exacta','Este tablero de clavos mide terrenos. Vamos a marcar parcelas.','EXPLORADOR','','cr_geoplano'),
 ('m_f3','w_formas',3,'El transportador perdido','Gira el rayo hasta el angulo pedido','Sin transportador no hay rumbo. Ajusta el rayo grado a grado.','AVENTURERO','m_f1','cr_transportador'),
 ('m_f4','w_formas',4,'Mosaicos del espejo','Completa la mitad que falta del mosaico','El suelo del faro es un mosaico simetrico. Le falta media pieza.','AVENTURERO','m_f1','cr_espejo'),
 ('m_f5','w_formas',5,'Arquitectos de la bahia','Domina area y perimetro a la vez','Hay que disenar el muelle nuevo. Cada medida cuenta.','MAESTRO','m_f2,m_f3','cr_muelle'),

 ('m_m1','w_medida',1,'La regla de Kubo','Mide objetos colocando bien el cero','Se han perdido las etiquetas del almacen. Hay que medirlo todo.','EXPLORADOR','','cr_regla'),
 ('m_m2','w_medida',2,'La balanza del puerto','Equilibra la balanza con las pesas justas','Sin peso exacto no hay factura. Ayuda al capataz con las pesas.','EXPLORADOR','','cr_balanza'),
 ('m_m3','w_medida',3,'El reloj de la torre','Coloca las manecillas y calcula duraciones','El reloj de la torre se atrasa. Ponlo en hora, marinero.','AVENTURERO','m_m1','cr_reloj'),
 ('m_m4','w_medida',4,'La escalera de unidades','Pasa de milimetros a metros y de gramos a kilos','Cada peldano multiplica o divide por 10. Sube y baja con cuidado.','AVENTURERO','m_m1,m_m2','cr_escalera'),
 ('m_m5','w_medida',5,'Cargamento exacto','Mide, pesa y entrega a tiempo','El ultimo barco zarpa al amanecer. No puede fallar ni un gramo.','MAESTRO','m_m3,m_m4','cr_cargamento'),

 ('m_r1','w_fraccion',1,'La pizzeria del volcan','Pinta la fraccion exacta que te piden','En la pizzeria del crater todo se reparte en partes iguales.','EXPLORADOR','','cr_pizza'),
 ('m_r2','w_fraccion',2,'Cintas equivalentes','Descubre fracciones que valen lo mismo','Dos cintas distintas pueden cubrir el mismo trozo de puente.','AVENTURERO','m_r1','cr_cinta'),
 ('m_r3','w_fraccion',3,'El puente de la recta','Coloca cada fraccion en su sitio','Las tablas del puente estan numeradas con fracciones. Ordenalas.','AVENTURERO','m_r1','cr_puente'),
 ('m_r4','w_fraccion',4,'Duelo de fracciones','Compara fracciones y decide cual es mayor','Dos herreros discuten por quien tiene mas metal. Resuelvelo tu.','AVENTURERO','m_r2,m_r3','cr_duelo'),
 ('m_r5','w_fraccion',5,'Rios de decimales','Une fracciones y numeros decimales','El rio de lava se mide en decimas. Es la misma idea con otra ropa.','MAESTRO','m_r4','cr_decimal'),

 ('m_n1','w_numeros',1,'Bloques de la cueva','Construye numeros con unidades, decenas y centenas','Cada bloque vale diez veces mas que el anterior. Construye con cabeza.','EXPLORADOR','','cr_bloque'),
 ('m_n2','w_numeros',2,'Cristales en secuencia','Descubre la regla y completa el patron','Los cristales crecen en orden. Adivina cual falta.','EXPLORADOR','','cr_secuencia'),
 ('m_n3','w_numeros',3,'El eco de las tablas','Usa la multiplicacion para saltar de diez en diez','El eco repite los numeros multiplicados. Sigue el ritmo.','AVENTURERO','m_n2','cr_eco'),
 ('m_n4','w_numeros',4,'El gran calculo','Junta todo lo aprendido en la cueva','La sala del tesoro solo se abre con el numero exacto.','MAESTRO','m_n1,m_n3','cr_tesoro');

-- ---------------------------------------------------------------------
-- RETOS (muestra representativa: uno de cada mini-juego)
-- El campo payload_json es exactamente lo que escribe la app.
-- ---------------------------------------------------------------------
INSERT INTO challenge (id, mission_id, order_index, kind, prompt, explanation, hint, payload_json, xp) VALUES
 ('c_f1_1','m_f1',1,'SHAPE_SORT',
  'Arrastra cada lente a su caja segun cuantos lados tiene.',
  'Un poligono se nombra por su numero de lados: 3 lados es un triangulo y 4 lados es un cuadrilatero. Los lados se cuentan siguiendo el borde sin levantar el dedo.',
  'Recorre el borde con el dedo y cuenta cada tramo recto.',
  '{"type":"shape_sort","shapes":[{"id":"s_tri_eq","name":"triangulo equilatero","sides":3,"allSidesEqual":true,"rightAngles":0,"curved":false,"rotation":0},{"id":"s_cuad","name":"cuadrado","sides":4,"allSidesEqual":true,"rightAngles":4,"curved":false,"rotation":0},{"id":"s_tri_es","name":"triangulo escaleno","sides":3,"allSidesEqual":false,"rightAngles":0,"curved":false,"rotation":200},{"id":"s_rombo","name":"rombo","sides":4,"allSidesEqual":true,"rightAngles":0,"curved":false,"rotation":45},{"id":"s_tri_re","name":"triangulo rectangulo","sides":3,"allSidesEqual":false,"rightAngles":1,"curved":false,"rotation":15},{"id":"s_trap","name":"trapecio","sides":4,"allSidesEqual":false,"rightAngles":2,"curved":false,"rotation":0}],"buckets":[{"id":"b3","label":"3 lados","criterion":"NUM_LADOS","value":3},{"id":"b4","label":"4 lados","criterion":"NUM_LADOS","value":4}]}',
  10),

 ('c_f2_1','m_f2',1,'GEOBOARD',
  'Marca una parcela de 4 cuadraditos de area.',
  'El area cuenta cuantos cuadraditos caben dentro. Un cuadrado de 2 por 2 encierra 4 cuadraditos.',
  'Prueba con un cuadrado de 2 clavos de ancho y 2 de alto.',
  '{"type":"geoboard","grid":6,"objective":"AREA","target":4.0,"minVertices":3,"unitLabel":"cuadraditos"}',
  10),

 ('c_f3_1','m_f3',1,'ANGLE_DIAL',
  'Gira el rayo hasta formar un angulo recto.',
  'El angulo recto mide 90 grados. Es el que forman las paredes con el suelo o las agujas del reloj a las 3 en punto.',
  'Es justo un cuarto de vuelta completa.',
  '{"type":"angle_dial","targetDegrees":90,"tolerance":4,"askClassification":false,"showProtractor":true}',
  10),

 ('c_f4_1','m_f4',1,'SYMMETRY',
  'Completa el mosaico al otro lado del espejo.',
  'En una simetria, cada baldosa tiene su reflejo a la misma distancia del eje, pero al otro lado.',
  'Cuenta cuantas casillas hay del eje a la baldosa y repite al otro lado.',
  '{"type":"symmetry","rows":6,"cols":6,"axis":"VERTICAL","given":[2,7,8,12,13,14,20,26]}',
  10),

 ('c_m1_1','m_m1',1,'RULER',
  'Mide el lapiz. Cuantos centimetros tiene?',
  'Para medir bien, el 0 de la regla debe coincidir con el principio del objeto. Si empiezas en el 1, la medida sale mal.',
  'Arrastra la regla hasta que el 0 toque la punta del lapiz.',
  '{"type":"ruler","objectMm":120,"toleranceMm":2,"answerUnit":"cm","objectKind":"lapiz"}',
  10),

 ('c_m2_1','m_m2',1,'BALANCE',
  'Equilibra la balanza con el saco de arroz.',
  'Una balanza se equilibra cuando los dos platos pesan lo mismo. 300 g se consiguen con 200 g + 100 g.',
  'Empieza siempre por la pesa mas grande que no se pase.',
  '{"type":"balance","leftGrams":300,"leftLabel":"Saco de arroz","weights":[500,200,100,50],"maxPerWeight":3}',
  10),

 ('c_m3_1','m_m3',1,'CLOCK',
  'Pon el reloj a las 3 y cuarto.',
  'Y cuarto significa 15 minutos pasados. El minutero apunta al 3 porque cada numero vale 5 minutos: 3 x 5 = 15.',
  'El minutero va al numero 3 del reloj.',
  '{"type":"clock","startHour":12,"startMinute":0,"mode":"PONER_HORA","deltaMinutes":0,"targetHour":3,"targetMinute":15}',
  10),

 ('c_r1_2','m_r1',2,'FRACTION_PIE',
  'Pinta 3/4 de la losa.',
  '3/4 significa 3 partes de las 4 en que esta dividida la losa. Queda 1/4 sin pintar.',
  'Pinta todas menos una.',
  '{"type":"fraction_pie","shape":"BARRA","parts":4,"targetNumerator":3,"targetDenominator":4,"mode":"PINTAR"}',
  10),

 ('c_r3_1','m_r3',1,'FRACTION_LINE',
  'Coloca la tabla en 1/4.',
  'Para colocar 1/4 se divide el tramo de 0 a 1 en 4 partes iguales y se avanza una.',
  'Cuenta cuatro huecos entre el 0 y el 1 y para en el primero.',
  '{"type":"fraction_line","denominator":4,"numerator":1,"wholes":1,"toleranceSteps":0,"decimalLabels":false}',
  10),

 ('c_n1_2','m_n1',2,'PLACE_VALUE',
  'Ahora construye 152.',
  '152 son 1 centena, 5 decenas y 2 unidades. La placa grande vale 100 porque son 10 barras de 10.',
  'Coloca primero la placa de 100.',
  '{"type":"place_value","target":152,"pieces":[1,10,100],"maxPerPiece":9}',
  10),

 ('c_n2_2','m_n2',2,'PATTERN',
  'Completa la serie numerica.',
  'Cada numero suma 4 al anterior: 3, 7, 11, 15, 19. Es una progresion de paso constante.',
  'Resta dos numeros seguidos para descubrir el salto.',
  '{"type":"pattern","sequence":[{"label":"3","shape":null,"colorIndex":0,"rotation":0},{"label":"7","shape":null,"colorIndex":0,"rotation":0},{"label":"11","shape":null,"colorIndex":0,"rotation":0},{"label":"","shape":null,"colorIndex":0,"rotation":0},{"label":"19","shape":null,"colorIndex":0,"rotation":0}],"holeIndex":3,"options":[{"label":"13","shape":null,"colorIndex":0,"rotation":0},{"label":"14","shape":null,"colorIndex":0,"rotation":0},{"label":"15","shape":null,"colorIndex":0,"rotation":0},{"label":"16","shape":null,"colorIndex":0,"rotation":0}],"answerIndex":2,"rule":"Se suma 4 cada vez."}',
  10),

 ('c_n1_5','m_n1',5,'QUIZ',
  'En el numero 4703, cuanto vale el 7?',
  'El 7 esta en las centenas, asi que vale 700. La posicion de una cifra decide su valor.',
  'Cuenta las posiciones desde la derecha: unidades, decenas, centenas.',
  '{"type":"quiz","options":["7","70","700","7000"],"answerIndex":2,"art":"cueva"}',
  10);

-- ---------------------------------------------------------------------
-- INSIGNIAS (13)
-- ---------------------------------------------------------------------
INSERT INTO badge (id, name, description, rule, threshold, param, art_seed) VALUES
 ('b_primer_paso','Primer desembarco','Completa tu primera mision del archipielago.','FIRST_MISSION',1,NULL,1),
 ('b_formas','Guardian del faro','Termina todas las misiones de la Bahia de las Formas.','WORLD_COMPLETE',0,'w_formas',2),
 ('b_medida','Capataz del puerto','Termina todas las misiones de Puerto Medida.','WORLD_COMPLETE',0,'w_medida',3),
 ('b_fraccion','Domador de lava','Termina todas las misiones del Volcan Fraccion.','WORLD_COMPLETE',0,'w_fraccion',4),
 ('b_numeros','Llave de la cueva','Termina todas las misiones de la Cueva de los Numeros.','WORLD_COMPLETE',0,'w_numeros',5),
 ('b_perfecto','Pulso firme','Consigue 3 estrellas en 3 misiones distintas.','PERFECT_MISSION',3,NULL,6),
 ('b_estrellas','Cielo estrellado','Reune 25 estrellas en total.','TOTAL_STARS',25,NULL,7),
 ('b_experto','Ingeniero jefe','Alcanza 800 puntos de experiencia.','TOTAL_XP',800,NULL,8),
 ('b_racha','Explorador constante','Juega 3 dias seguidos.','STREAK_DAYS',3,NULL,9),
 ('b_coleccionista','Coleccionista','Consigue 12 Cristales de Ingenio.','COLLECTION_SIZE',12,NULL,10),
 ('b_taller','Manos de taller','Supera 2 sesiones de repaso sin fallos.','REVIEW_CLEARED',2,NULL,11),
 ('b_sin_pistas','Sin ayudas','Termina 5 misiones sin pedir ni una pista.','NO_HINT_MISSION',5,NULL,12),
 ('b_maestro_formas','Ojo geometrico','Acierta el 85 % en la Bahia de las Formas.','TOPIC_MASTER',85,'w_formas',13);

-- ---------------------------------------------------------------------
-- CRISTALES DE INGENIO (27)
-- ---------------------------------------------------------------------
INSERT INTO collectible (id, name, fact, world_id, rarity, art_seed) VALUES
 ('cr_faro','Cristal Faro','Un poligono tiene siempre el mismo numero de lados que de vertices.','w_formas','COMUN',1),
 ('cr_geoplano','Cristal Geoplano','Dos figuras muy distintas pueden encerrar exactamente la misma area.','w_formas','COMUN',2),
 ('cr_transportador','Cristal Transportador','Una vuelta completa son 360 grados; los babilonios ya usaban ese numero.','w_formas','RARO',3),
 ('cr_espejo','Cristal Espejo','El cuerpo humano es casi simetrico, pero nunca del todo.','w_formas','RARO',4),
 ('cr_muelle','Cristal Muelle','Con el mismo perimetro, el cuadrado es el rectangulo de mayor area.','w_formas','LEGENDARIO',5),
 ('cr_regla','Cristal Regla','El metro nacio en Francia como la diezmillonesima parte de un cuarto de meridiano.','w_medida','COMUN',6),
 ('cr_balanza','Cristal Balanza','La balanza de dos platos tiene mas de 4000 anos de antiguedad.','w_medida','COMUN',7),
 ('cr_reloj','Cristal Reloj','La hora se divide en 60 minutos porque los sumerios contaban en base 60.','w_medida','RARO',8),
 ('cr_escalera','Cristal Escalera','Cada peldano del sistema metrico multiplica o divide por 10.','w_medida','RARO',9),
 ('cr_cargamento','Cristal Cargamento','Un litro de agua pesa casi exactamente un kilogramo.','w_medida','LEGENDARIO',10),
 ('cr_pizza','Cristal Porcion','Los egipcios escribian casi todas sus fracciones con numerador 1.','w_fraccion','COMUN',11),
 ('cr_cinta','Cristal Cinta','Multiplicar arriba y abajo por el mismo numero no cambia el valor.','w_fraccion','COMUN',12),
 ('cr_puente','Cristal Puente','Entre dos fracciones cualesquiera siempre cabe otra fraccion.','w_fraccion','RARO',13),
 ('cr_duelo','Cristal Duelo','Con el mismo numerador, gana la fraccion de denominador mas pequeno.','w_fraccion','RARO',14),
 ('cr_decimal','Cristal Decimal','La coma decimal se popularizo en Europa hace apenas 400 anos.','w_fraccion','LEGENDARIO',15),
 ('cr_bloque','Cristal Bloque','Usamos base diez casi seguro porque tenemos diez dedos.','w_numeros','COMUN',16),
 ('cr_secuencia','Cristal Secuencia','En la naturaleza hay patrones numericos, como las espirales de un girasol.','w_numeros','COMUN',17),
 ('cr_eco','Cristal Eco','Multiplicar por 10 solo desplaza las cifras una posicion a la izquierda.','w_numeros','RARO',18),
 ('cr_tesoro','Cristal Tesoro','El cero tardo siglos en aceptarse como numero de pleno derecho.','w_numeros','LEGENDARIO',19),
 ('cr_hito_formas','Sello de la Bahia','Has dominado la geometria de la isla.','w_formas','LEGENDARIO',20),
 ('cr_hito_medida','Sello del Puerto','Has dominado las medidas del archipielago.','w_medida','LEGENDARIO',21),
 ('cr_hito_fraccion','Sello del Volcan','Has dominado las fracciones y los decimales.','w_fraccion','LEGENDARIO',22),
 ('cr_hito_numeros','Sello de la Cueva','Has dominado los numeros y sus patrones.','w_numeros','LEGENDARIO',23),
 ('cr_hito_nivel5','Nucleo Nivel 5','La constancia tambien es una habilidad matematica.','w_formas','RARO',24),
 ('cr_hito_nivel10','Nucleo Nivel 10','Diez niveles de expedicion. Kubo esta impresionado.','w_medida','LEGENDARIO',25),
 ('cr_hito_estrellas','Estrella Polar','30 estrellas iluminan todo el archipielago.','w_fraccion','RARO',26),
 ('cr_hito_insignias','Nucleo de Insignias','Ocho insignias son ocho retos distintos superados.','w_numeros','LEGENDARIO',27);

-- ---------------------------------------------------------------------
-- ESTADO INICIAL DEL JUGADOR
-- ---------------------------------------------------------------------
INSERT INTO profile (id, alias, avatar_id, xp, streak_days, last_played_day, onboarding_done, profile_done, review_sessions_cleared)
VALUES (1, '', 0, 0, 1, 0, 0, 0, 0);

INSERT INTO settings (id, sound_enabled, haptics_enabled, animations_enabled, big_text_enabled)
VALUES (1, 1, 1, 1, 0);

INSERT INTO meta (key, value) VALUES ('catalog_version', '1');

-- ---------------------------------------------------------------------
-- EJEMPLO DE PARTIDA AVANZADA (opcional, solo para pruebas manuales)
-- Descomenta el bloque para simular a un nino que ya lleva varios dias.
-- ---------------------------------------------------------------------
-- UPDATE profile SET alias = 'Nova', avatar_id = 3, xp = 385, streak_days = 4,
--                    onboarding_done = 1, profile_done = 1 WHERE id = 1;
--
-- INSERT INTO mission_progress (mission_id, status, stars, best_percent, times_played, no_hint_run, last_played_at) VALUES
--  ('m_f1','DOMINADA',3,100,2,1,1735689600000),
--  ('m_f2','DOMINADA',3,100,1,1,1735776000000),
--  ('m_f3','COMPLETADA',2,80,1,0,1735862400000),
--  ('m_f4','COMPLETADA',2,80,1,0,1735948800000),
--  ('m_m1','EMPEZADA',0,40,1,0,1736035200000);
--
-- INSERT INTO attempt (challenge_id, mission_id, world_id, correct, used_hint, elapsed_ms, timestamp) VALUES
--  ('c_f1_1','m_f1','w_formas',1,0,18400,1735689600000),
--  ('c_f1_2','m_f1','w_formas',1,0,15200,1735689600000),
--  ('c_f2_1','m_f2','w_formas',1,0,22100,1735776000000),
--  ('c_f3_1','m_f3','w_formas',0,1,31000,1735862400000),
--  ('c_m1_1','m_m1','w_medida',0,0,27500,1736035200000);
--
-- INSERT INTO badge_unlock (badge_id, unlocked_at) VALUES
--  ('b_primer_paso',1735689600000);
--
-- INSERT INTO collectible_unlock (collectible_id, unlocked_at) VALUES
--  ('cr_faro',1735689600000),
--  ('cr_geoplano',1735776000000);
--
-- INSERT INTO review_item (challenge_id, mission_id, world_id, wrong_count, last_wrong_at, resolved) VALUES
--  ('c_f3_1','m_f3','w_formas',1,1735862400000,0),
--  ('c_m1_1','m_m1','w_medida',1,1736035200000,0);
