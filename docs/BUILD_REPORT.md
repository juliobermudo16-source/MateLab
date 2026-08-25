# Informe de compilación

## MateLab — Islas del Ingenio · v1.0.0

Fecha del informe: **2026-08-24**

---

## 1. Resultado

> ## ✅ COMPILACIÓN CORRECTA
>
> Verificada en GitHub Actions. Todos los datos de este informe proceden de
> una ejecución real; ninguno está simulado.

| Dato | Valor |
|---|---|
| Ejecución | [`32790617392`](https://github.com/juliobermudo16-source/MateLab/actions/runs/32790617392) |
| Commit | `d72aaa6bd908b3d12c06705b17bd20ec388556eb` |
| Fecha (UTC) | 2026-08-24 23:49:08 |
| Duración | 5 min 58 s |
| Entorno | `ubuntu-latest`, JDK 17 (Temurin), Gradle 8.9, Android SDK 34 |

### Tareas

| Tarea | Resultado |
|---|---|
| `gradle wrapper` | ✅ correcto |
| `./gradlew clean` | ✅ correcto |
| `./gradlew testDebugUnitTest` | ✅ **171 pruebas, 0 fallos** |
| `./gradlew lintDebug` | ✅ correcto |
| `./gradlew assembleDebug` | ✅ correcto |
| `./gradlew assembleRelease` | ✅ correcto |

### Pruebas

```
Total: 171  |  Fallidas: 0  |  Errores: 0  |  Omitidas: 0  |  APROBADAS: 171
```

Recuento extraído de los ficheros `TEST-*.xml` del artefacto
`MateLab-informes` de esa misma ejecución.

| Clase | Pruebas |
|---|---|
| `GridEnginesTest` | 31 |
| `CatalogIntegrityTest` | 22 |
| `RewardEnginesTest` | 21 |
| `FractionEngineTest` | 20 |
| `ProgressEngineTest` | 19 |
| `ClockAndAngleTest` | 17 |
| `GeoboardEngineTest` | 16 |
| `MeasureEngineTest` | 15 |
| `PayloadSerializationTest` | 5 |
| `AliasSanitizerTest` | 5 |
| **Total** | **171** |

### Lint

Sin errores. Los avisos emitidos son informativos y no bloquean la
compilación:

- Node.js 20 está en desuso en los runners (afecta a las acciones, no al código).
- Hay versiones más nuevas de `androidx.core:core-ktx` (1.19.0) y
  `androidx.lifecycle:lifecycle-runtime-compose` (2.11.0). Se mantienen las
  versiones fijadas a propósito: el proyecto no usa versiones dinámicas.

---

## 2. APK generados

| Fichero | Tamaño | SHA-256 |
|---|---|---|
| `MateLab-v1.0.0-debug.apk` | 17 498 465 B (17 MB) | `5bbdcb533d5833f1a76dd9187e95fff077bda193d9208a0be39264feab6713b1` |
| `MateLab-v1.0.0-release.apk` | 1 544 436 B (1,5 MB) | `8d265a6c6db14826c99ea2b2620cf3c2facda45c9bb1987e6cd612c3e0572cf4` |

La versión de producción ocupa **11 veces menos** gracias a R8 y a
`shrinkResources`.

### Verificación de los binarios

Descargados y comprobados localmente:

| Comprobación | debug | release |
|---|---|---|
| Cabecera ZIP (`50 4B 03 04`) | ✅ | ✅ |
| `AndroidManifest.xml` | ✅ | ✅ |
| `classes.dex` | ✅ | ✅ |
| Firma APK v2/v3 (`APK Sig Block 42`) | ✅ | ✅ |
| Entradas en el archivo | 185 | 95 |
| SHA-256 coincide con el informe de CI | ✅ | ✅ |

> Los dos APK van firmados con la clave de depuración, suficiente para
> instalarlos directamente. Para publicar en Google Play hay que firmar con un
> keystore propio (ver `MANUAL_TECNICO.md`, apartado 2.3).

Ambos están disponibles en el artefacto **`MateLab-APK-v1.0.0`** de la
ejecución, y descargados en `deliverables/`.

---

## 3. Historial: cómo se llegó hasta aquí

El proyecto **no se pudo compilar en la máquina de desarrollo**. Comprobado el
2026-08-24:

```
$ java -version
java version "25.0.1" 2025-10-21 LTS

$ gradle -v
bash: gradle: command not found

$ echo "$ANDROID_HOME / $ANDROID_SDK_ROOT"
 /
```

Tres bloqueos: JDK 25 (AGP 8.5.2 exige JDK 17), Gradle ausente y sin SDK de
Android. Por eso la compilación se delegó en integración continua, y por eso
hicieron falta tres intentos.

### Intento 1 — `32789065862` · ❌ falló

`testDebugUnitTest` no compiló. **8 errores de Kotlin**, con 4 causas raíz:

| Causa | Ficheros |
|---|---|
| Faltaba `import androidx.compose.runtime.getValue`, sin el cual `val x by animateFloat(...)` no funciona como delegado (generó 3 errores en cascada) | `Kubo.kt`, `Backdrop.kt` |
| `detectTapGestures` llamada con nombre completamente cualificado; Kotlin no lo admite en extensiones con receptor implícito | `AngleDialGame.kt`, `ShapeSortGame.kt` |
| `AngleEngine.normalize` recibía un `Float` y solo tiene sobrecargas `Double` e `Int` | `AngleDialGame.kt` |

Corregido en `9c0f231`. Se añadió `tools/check_delegates.py` para detectar
esos dos patrones sin compilador.

### Intento 2 — `32790227869` · ❌ falló

El código de la aplicación ya **compilaba** y **lint pasaba**. De 171 pruebas,
170 en verde y 1 en rojo:

```
ClockAndAngleTest > imantar el giro a multiplos FAILED
    java.lang.AssertionError at ClockAndAngleTest.kt:49
```

El fallo estaba **en la prueba, no en el motor**: esperaba que
`snap(87.4, paso = 1)` devolviese `90.0`, cuando con paso 1 la función redondea
al grado entero y lo correcto es `87.0` (la expectativa correspondía a un paso
de 15). Corregido en `d72aaa6`, separando ambos comportamientos en cuatro
asertos.

### Intento 3 — `32790617392` · ✅ correcto

Los seis pasos en verde y los dos APK publicados.

### Intento 4 — `32792170654` · ✅ correcto · commit `faa5738`

Compilación posterior con la corrección del clasificador de figuras, que se
detectó **probando la app en un móvil real**, no en CI.

En la misión *El faro de los polígonos* ninguna figura llegaba a la caja de
"4 lados": todas acababan en la de "3 lados". Como esa misión es requisito de
otras dos, dejaba media isla bloqueada. Eran dos fallos encadenados:

1. La bandeja tenía `clip(RoundedCornerShape)`, así que la figura arrastrada
   se recortaba y desaparecía al salir de ella.
2. La caja de destino se decidía con `localBoundingBoxOf`, que recorta por
   defecto. Con la figura recortada, el rectángulo salía vacío y su centro
   era `(0,0)` — un punto que cae dentro de la primera caja.

Se sustituyó por la posición del dedo en coordenadas de ventana
(`localToWindow` + desplazamiento del gesto) contrastada con
`boundsInWindow()` de cada caja, que no se recorta.

**Lección**: 171 pruebas y cuatro verificadores estáticos no detectan un
error de coordenadas táctiles. Hace falta probar con el dedo.

| APK de esta compilación | SHA-256 |
|---|---|
| `MateLab-v1.0.0-debug.apk` | `121aee8ccefe8ad70b42199e0f95b8c7e6ba7b96fdcd95f45dcec17d3e6a31c7` |
| `MateLab-v1.0.0-release.apk` | `b101e0fdd55b24887a38d936896205e6984e81a3c4403048fe9ef25be0e59b3f` |

> Las huellas del apartado 2 corresponden al intento 3. Cada compilación
> genera binarios nuevos, así que la referencia válida en cada momento es el
> `BUILD_RESULT.md` que acompaña a su propio artefacto o Release.

---

## 4. Verificaciones ejecutadas en local

Además de la compilación en CI, en la máquina de desarrollo se ejecutaron
cuatro comprobaciones estáticas escritas para este proyecto, sin dependencias:

```
$ python tools/check_imports.py
Ficheros Kotlin analizados: 96
Simbolos declarados: 1519
Todos los imports internos resuelven correctamente.

$ python tools/check_structure.py
Ficheros Kotlin equilibrados: 96
Referencias a recursos comprobadas: 17
GameKind: 12 valores / Payloads: 12
Sin problemas estructurales.

$ python tools/check_content.py
Misiones: 19
Retos: 95
Retos de eleccion: 19 (20 %)
Contenido dentro de los limites.

$ python tools/check_delegates.py
Ficheros Kotlin analizados: 96
Sin delegados ni extensiones mal resueltos.
```

`check_delegates.py` nació precisamente del intento 1: codifica los dos errores
que el compilador encontró para que no vuelvan a colarse.

### Recursos generados

```
$ python tools/gen_sounds.py
  sfx_tap.wav               1.9 KB
  sfx_correct.wav          10.5 KB
  sfx_wrong.wav            12.1 KB
  sfx_star.wav             15.1 KB
  sfx_unlock.wav           25.4 KB
  sfx_level.wav            32.7 KB

$ python tools/md2pdf.py
  MEMORIA_DESCRIPTIVA.pdf                   7 paginas    35.9 KB
  MANUAL_USUARIO.pdf                        5 paginas    26.4 KB
  MANUAL_TECNICO.pdf                        7 paginas    32.9 KB

$ python tools/check_pdf.py
OK     docs/pdf/MANUAL_TECNICO.pdf                  7 paginas
OK     docs/pdf/MANUAL_USUARIO.pdf                  5 paginas
OK     docs/pdf/MEMORIA_DESCRIPTIVA.pdf             7 paginas
```

---

## 5. Inventario del proyecto

| Métrica | Valor |
|---|---|
| Ficheros Kotlin de producción | 86 |
| Ficheros Kotlin de prueba | 10 |
| Líneas de Kotlin | 15 839 |
| Recursos XML | 13 |
| Efectos de sonido (WAV) | 6 |
| Pruebas unitarias | 171 (todas en verde) |
| Islas / misiones / retos | 4 / 19 / 95 |
| Tipos de mini-juego | 12 |
| Insignias / cristales | 13 / 27 |
| Tablas de base de datos | 13 |

---

## 6. SHA-256 de los demás entregables

| Fichero | Tamaño | SHA-256 |
|---|---|---|
| `MEMORIA_DESCRIPTIVA.pdf` | 36 763 B | `b1e13662f622aeb1b6a1798a9adbbdf7404910c182ef50479e6d00ff7612a482` |
| `MANUAL_USUARIO.pdf` | 27 010 B | `b6144b3aae93099bff02633d661f5530b78cd182953455de7d4ff5ab866587db` |
| `MANUAL_TECNICO.pdf` | 33 667 B | `56eb152879a58ea9f3c0767b66a224359a7baa90719914cf0dc1bf83ec7c341a` |

La huella del ZIP de fuentes está en `deliverables/SHA256SUMS.txt` y no se
reproduce aquí porque este documento viaja dentro del propio ZIP.

---

## 7. Reproducir la compilación

En cualquier máquina con **JDK 17** y **SDK de Android 34**:

```bash
gradle wrapper --gradle-version 8.9 --distribution-type bin
```

```bash
./gradlew clean testDebugUnitTest lintDebug assembleDebug assembleRelease
```

O simplemente haciendo push a `main`: el workflow lo repite entero y publica
los APK como artefacto.

---

## 8. Declaración de honestidad

Este informe cumple el punto 37 de la especificación. Los resultados de la
sección 1 proceden de la ejecución `32790617392` de GitHub Actions; los SHA-256
de la sección 2 se recalcularon sobre los APK ya descargados y coinciden con
los que emitió el runner; los bloques de consola de la sección 4 son
transcripciones literales. Los dos intentos fallidos quedan documentados en la
sección 3 en lugar de ocultarse.
