# Memoria descriptiva

## MateLab — Islas del Ingenio

Aplicación Android educativa de matemáticas para niños de 8 a 12 años.
Versión 1.0.0.

---

## 1. Planteamiento

### 1.1 Problema

Buena parte del software educativo de matemáticas se limita a repetir el
cuaderno de clase en una pantalla: un enunciado, cuatro opciones y un "correcto"
o "incorrecto". El niño responde, pero no *manipula* nada. La geometría se
aprende mirando dibujos en vez de construirlos; las medidas se estudian sin
tocar una regla; las fracciones se memorizan sin repartir nada.

### 1.2 Propuesta

MateLab convierte cada contenido en un objeto que se puede tocar:

| En vez de… | MateLab hace que el niño… |
|---|---|
| Elegir el área de un rectángulo | Tienda gomas en un geoplano hasta encerrar 6 cuadraditos |
| Leer "un ángulo recto mide 90°" | Gire un rayo con el dedo hasta abrir 90° |
| Marcar cuánto mide un lápiz | Arrastre una regla, alinee el cero y toque la marca final |
| Decir cuánto pesa un saco | Ponga pesas en una balanza que se inclina de verdad |
| Señalar qué fracción es 3/4 | Pinte 3 de las 4 porciones de una losa |
| Elegir el número 152 | Arrastre 1 placa de cien, 5 barras de diez y 2 cubitos |

El 80 % del contenido es manipulación directa. El 20 % restante son retos de
decisión con ilustración, usados como cierre o comprobación conceptual.

### 1.3 Público

Niños de 8 a 12 años. El diseño evita deliberadamente la estética preescolar:
no hay caras sonrientes por todas partes, ni botones desproporcionados, ni
lenguaje condescendiente. El tono es el de una expedición: el niño es un
explorador competente, no un alumno al que hay que animar constantemente.

---

## 2. Identidad del producto

### 2.1 El mundo

**Un archipiélago flotante de cuatro islas.** Cada una guarda un área de las
matemáticas y tiene un edificio propio que la identifica de un vistazo en el
mapa: un faro, una grúa de puerto, un volcán y una cueva de cristales.

### 2.2 El personaje

**Kubo**, un robot explorador con antena de diamante y visor turquesa. Aparece
en el mapa proponiendo la siguiente misión, en el informe previo a cada misión,
y en el feedback tras cada reto. Tiene cinco estados de ánimo (neutro, feliz,
pensando, animando, celebrando) que se dibujan cambiando ojos, boca e
inclinación. No interrumpe: nunca habla más de dos frases seguidas.

### 2.3 Paleta

| Uso | Color |
|---|---|
| Mar profundo / tinta | `#0B3B4A` |
| Turquesa (Bahía de las Formas) | `#12B3A6` |
| Mango (Puerto Medida) | `#FF8A3D` |
| Coral (Volcán Fracción) | `#F2585B` |
| Violeta (Cueva de los Números) | `#7C5CFF` |
| Sol / estrellas | `#FFC846` |
| Arena / fondo | `#FFF6E9` |

Cada isla tiene su gama propia (primario, oscuro, suave y acento), que se
propaga a sus misiones, botones y barras de progreso. La app tiene tema claro
y oscuro completos.

### 2.4 Ilustración

**Todo el arte es vectorial y local.** No hay ni un solo PNG, ni una sola
descarga. Se usan dos técnicas:

1. **Vector drawables XML** para el icono adaptativo del lanzador (con capa
   monocroma para Android 13+), su versión de respaldo para Android 7, y el
   icono de la pantalla de arranque.
2. **Compose Canvas** para todo lo demás, en nueve ficheros del paquete
   `ui/art`.

Inventario visual:

| Elemento | Cantidad | Fichero |
|---|---|---|
| Kubo (5 estados de ánimo) | 1 personaje | `Kubo.kt` |
| Islas ilustradas | 4 escenas + versión insignia | `IslandArt.kt` |
| Fondo marino animado (cielo, sol/luna, nubes, olas, estrellas) | 1 sistema | `Backdrop.kt` |
| Cristales coleccionables paramétricos | 27 variantes únicas | `CrystalArt.kt` |
| Insignias paramétricas (3 formas × 7 emblemas) | 13 variantes | `BadgeArt.kt` |
| Avatares del niño | 8 variantes | `AvatarArt.kt` |
| Figuras geométricas reales | 10 tipos | `ShapeArt.kt` |
| Viñetas temáticas para retos de decisión | 13 escenas | `SceneArt.kt` |
| Primitivas de dibujo (polígonos, estrellas, ondas) | — | `DrawUtils.kt` |

Los cristales y las insignias se generan a partir de una **semilla**: el número
de caras, la inclinación, el color y el emblema se derivan de ella de forma
determinista. Así hay 27 cristales visiblemente distintos sin 27 ficheros de
imagen, y el mismo cristal se dibuja siempre igual.

---

## 3. Contenido educativo

### 3.1 Volumen

| Elemento | Cantidad |
|---|---|
| Islas | 4 |
| Misiones | 19 |
| Retos | 95 |
| Tipos de mini-juego | 12 |
| Insignias | 13 |
| Cristales coleccionables | 27 |
| Avatares | 8 |
| Pantallas principales | 12 |

### 3.2 Las cuatro islas

**Bahía de las Formas** (5 misiones, 25 retos) — desbloqueada desde el inicio.
Clasificación de polígonos por lados y ángulos, construcción de figuras con
área y perímetro exactos, medida de ángulos con transportador, simetría axial.

**Puerto Medida** (5 misiones, 25 retos) — se abre a los 120 XP.
Longitud con regla graduada en milímetros, masa con balanza de dos platos,
tiempo con reloj analógico de manecillas arrastrables, conversiones entre
unidades y capacidad.

**Volcán Fracción** (5 misiones, 25 retos) — se abre a los 320 XP.
Reparto en partes iguales, fracciones equivalentes, colocación en la recta
numérica, comparación, y el puente entre fracción y decimal.

**Cueva de los Números** (4 misiones, 20 retos) — se abre a los 560 XP.
Valor posicional con bloques de base diez, patrones aditivos y multiplicativos,
patrones visuales cíclicos, multiplicación y división.

### 3.3 Progresión pedagógica

Cada isla escala en tres niveles de dificultad, que además multiplican el XP:

- **Explorador** (×1): exploración guiada, una idea por reto.
- **Aventurero** (×2): decisiones y combinación de dos ideas.
- **Maestro** (×3): síntesis, requiere haber completado misiones previas.

Las misiones de nivel Maestro declaran requisitos explícitos: *Arquitectos de
la bahía* no se abre hasta terminar *Gomas en el geoplano* y *El transportador
perdido*.

### 3.4 Los doce mini-juegos

| Clave | Mecánica | Motor que la evalúa |
|---|---|---|
| `GEOBOARD` | Tocar clavos para tender gomas | Fórmula del zapato, perímetro euclídeo, detección de polígonos cruzados |
| `SHAPE_SORT` | Arrastrar figuras a cestas | Propiedades reales de cada figura (lados, ángulos rectos, curvatura) |
| `ANGLE_DIAL` | Girar un rayo con el dedo | Ángulo del vector dedo-centro, clasificación e imantado |
| `SYMMETRY` | Pintar celdas con el dedo | Reflejo respecto al eje, celdas que faltan y celdas que sobran |
| `RULER` | Arrastrar una regla y tocar la marca | Conversión de unidades y tolerancia en milímetros |
| `BALANCE` | Poner pesas en el plato | Suma de masas e inclinación proporcional a la diferencia |
| `CLOCK` | Arrastrar las manecillas | Hora deducida del ángulo de cada aguja, suma de duraciones |
| `FRACTION_PIE` | Pintar porciones | Equivalencia de fracciones, no igualdad literal |
| `FRACTION_LINE` | Arrastrar una ficha, imantada a las marcas | Posición en la recta con tolerancia configurable |
| `PLACE_VALUE` | Añadir y quitar bloques de base diez | Suma posicional y descomposición canónica |
| `PATTERN` | Colocar la pieza que falta | Detección de progresión aritmética, geométrica o ciclo visual |
| `QUIZ` | Decidir sobre una ilustración | Comprobación conceptual de cierre |

### 3.5 Calidad del contenido

Verificada por pruebas automáticas, no por revisión manual:

- Ningún enunciado supera 130 caracteres; ninguna explicación, 260.
- Todo reto tiene enunciado, explicación y pista no vacíos.
- Toda balanza tiene solución con las pesas disponibles y sin exceder el máximo
  de repeticiones.
- Toda fracción pedida se puede pintar con las porciones disponibles.
- Toda figura del clasificador encaja **exactamente** en una cesta.
- Toda celda de partida de un mosaico está en la mitad no editable.
- La respuesta de todo patrón coincide con la regla deducida por el motor.
- Menos del 50 % del contenido son retos de decisión (real: 20 %).

---

## 4. Diseño de la experiencia

### 4.1 Los primeros 30 segundos

El niño abre la app y ve el mapa del archipiélago: mar animado, cuatro islas,
Kubo señalando una tarjeta con la siguiente misión y un botón grande. Un toque
y ya está en el informe de misión; otro y está tendiendo gomas en el geoplano.
No hay menús que atravesar.

### 4.2 Ciclo principal

```
        ┌──────────────────────────────────────────────┐
        │                                              │
        v                                              │
   MAPA DEL ARCHIPIÉLAGO                                │
        │  Kubo propone la siguiente misión             │
        v                                              │
   INFORME DE MISIÓN                                    │
        │  narrativa breve + nº de retos + dificultad   │
        v                                              │
   5 RETOS ENCADENADOS                                  │
        │  manipular → comprobar → explicación          │
        │  (si falla: reintentar sin castigo)           │
        v                                              │
   PARTE FINAL                                          │
        │  estrellas + XP + cristal + insignia + nivel  │
        v                                              │
   DESBLOQUEO                                           │
           isla nueva / cristal / insignia ─────────────┘
```

### 4.3 Qué motiva volver mañana

- **Racha diaria** visible en el mapa (sube si se juega al día siguiente).
- **Cristales de Ingenio**: 27 piezas, cada una con un dato matemático real que
  solo se revela al conseguirla ("un litro de agua pesa casi exactamente un
  kilogramo").
- **Islas que emergen**: la siguiente isla se ve sumergida y con candado, con el
  XP que falta escrito encima.
- **Tres estrellas** exigen pleno de aciertos *y* no usar ninguna pista, así que
  siempre queda margen para volver a una misión ya superada.

### 4.4 Feedback

Nunca se muestra un "correcto/incorrecto" a secas. Cada respuesta abre un panel
con Kubo, un titular ("Bien resuelto" / "Casi lo tienes") y **siempre la
explicación educativa**, se haya acertado o no. Si se ha fallado, aparece un
botón *Reintentar* que borra el intento y permite volver a probar. El lenguaje
evita el juicio: *"Casi lo tienes. Mira la explicación y vuelve a intentarlo."*

### 4.5 Estados visuales

Los cinco estados de misión (bloqueada, disponible, empezada, completada,
dominada) se comunican **siempre** con tres canales a la vez: color, icono y
texto. Nunca solo con color, para no excluir a niños con daltonismo.

### 4.6 Accesibilidad

- Interruptores independientes para sonido, vibración, animaciones y texto
  grande (escala toda la tipografía un 18 %).
- Desactivar animaciones detiene el balanceo de Kubo, la deriva del fondo y los
  efectos de pulsación.
- Las estrellas y las barras llevan descripción de contenido para lectores de
  pantalla.

### 4.7 Sonido y háptica

Seis efectos WAV cortos generados proceduralmente (toque, acierto, fallo,
estrella, desbloqueo, subida de nivel), con envolvente suave para que no
resulten estridentes. El de fallo es un intervalo grave y breve, sin
dramatismo. Todo es silenciable y nada suena al abrir la app.

---

## 5. Arquitectura

Tres capas, con las reglas de negocio comprobables sin interfaz:

```
ui/       Compose: tema, arte, componentes, 12 mini-juegos, 12 pantallas, VMs
   │  (depende de domain, nunca de data)
domain/   Modelos, 12 motores puros, catálogo de contenido, interfaces de repo
   │  (no depende de Android)
data/     Room: entidades, DAO, semilla, implementaciones de repositorio
```

- **MVVM** con `StateFlow` y `collectAsStateWithLifecycle`.
- **Repository** con interfaces en `domain` e implementaciones en `data`.
- **Inyección manual** mediante `AppContainer`, sin Hilt: con tres repositorios
  y un gestor de sonido, un contenedor explícito es más legible y reduce el
  riesgo de compilación.
- **Room real**: 13 tablas, claves foráneas con borrado en cascada, índices en
  las columnas de consulta y semilla en el callback de creación. No hay listas
  en memoria haciendo de base de datos, ni SQL dentro de composables.

Los 12 motores de `domain/engine` son objetos Kotlin puros sin ninguna
dependencia de Android, lo que permite probarlos en la JVM.

---

## 6. Pruebas

171 pruebas unitarias en 10 clases, ejecutables con `./gradlew testDebugUnitTest`:

| Clase | Pruebas | Qué cubre |
|---|---|---|
| `GridEnginesTest` | 31 | Simetría, valor posicional, patrones y clasificador de figuras |
| `CatalogIntegrityTest` | 22 | Control de calidad del contenido educativo |
| `RewardEnginesTest` | 21 | Insignias, cristales, hitos y taller de repaso |
| `FractionEngineTest` | 20 | Simplificación, equivalencia, suma, comparación, recta, casos límite |
| `ProgressEngineTest` | 19 | Niveles, estrellas, XP, desbloqueos, racha, mejor resultado |
| `ClockAndAngleTest` | 17 | Ángulos, clasificación, manecillas, duraciones, rangos inválidos |
| `GeoboardEngineTest` | 16 | Área, perímetro, polígonos cruzados, clavos repetidos, listas vacías |
| `MeasureEngineTest` | 15 | Conversiones, escalera de unidades, lectura de regla, balanza |
| `PayloadSerializationTest` | 5 | Ida y vuelta de los 95 retos por JSON |
| `AliasSanitizerTest` | 5 | Apodos vacíos, con saltos de línea o kilométricos |

Casos límite cubiertos explícitamente: listas vacías, valores negativos,
denominador cero, texto vacío y kilométrico, clavos duplicados, doble
comprobación del mismo reto, repetir una misión con peor resultado, minutos
fuera de rango, base de datos recién creada y reinicio de progreso.

---

## 7. Privacidad

MateLab **no declara ningún permiso** en el manifiesto.

- Sin Internet, sin backend, sin analítica, sin publicidad, sin login.
- Sin cámara, micrófono, ubicación, contactos ni almacenamiento externo.
- No se pide nombre real, edad, correo ni teléfono: solo un apodo de hasta 14
  caracteres, opcional, que se sustituye por el nombre del avatar si se deja
  vacío.
- Todos los datos viven en una base SQLite dentro del sandbox de la app y
  desaparecen al desinstalarla. Existe un botón para borrar el progreso.

---

## 8. Limitaciones conocidas

Declaradas de forma explícita, sin sustituciones silenciosas:

1. **El APK no se compiló en la máquina de desarrollo.** El entorno disponible
   tenía JDK 25, sin Gradle ni SDK de Android. La compilación real la ejecuta
   el workflow de GitHub Actions; el estado se documenta en
   `docs/BUILD_REPORT.md`.
2. **El binario `gradle-wrapper.jar` no está versionado.** Se genera con
   `gradle wrapper` (el workflow lo hace automáticamente).
3. **La balanza se maneja tocando las pesas, no arrastrándolas.** El
   comportamiento es real (suma de masas, inclinación proporcional), pero el
   gesto es tocar, no arrastrar, para que la pila de pesas sea legible en
   pantallas pequeñas. El clasificador de figuras sí usa arrastre completo.
4. **Los tests de persistencia son de dominio, no instrumentados.** La lógica de
   progreso, recompensas y repaso se prueba sobre motores puros; no se ejecuta
   Room en las pruebas unitarias para no depender de Robolectric.
5. **Sin traducciones.** La app está solo en español.

---

## 9. Entregables

```
deliverables/
    MateLab-v1.0.0-source.zip     Código fuente completo
    MEMORIA_DESCRIPTIVA.pdf
    MANUAL_USUARIO.pdf
    MANUAL_TECNICO.pdf
    LEEME.md                      Cómo obtener el APK
```

El APK lo produce GitHub Actions y se publica como artefacto del workflow y,
si se etiqueta la versión, como *Release* del repositorio.
