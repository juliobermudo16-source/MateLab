# Informe de compilación

## MateLab — Islas del Ingenio · v1.0.0

Fecha del informe: **2026-08-24**

---

## 1. Resultado

> ## COMPILACIÓN NO VERIFICADA EN LOCAL
>
> El APK **no se ha compilado** en la máquina donde se desarrolló el proyecto.
> Ninguna cifra de este informe está simulada: lo que no se pudo ejecutar
> aparece marcado como no ejecutado.

| Tarea | Estado | Dónde se ejecuta |
|---|---|---|
| `./gradlew clean` | **NO EJECUTADO** | GitHub Actions |
| `./gradlew testDebugUnitTest` | **NO EJECUTADO** | GitHub Actions |
| `./gradlew lintDebug` | **NO EJECUTADO** | GitHub Actions |
| `./gradlew assembleDebug` | **NO EJECUTADO** | GitHub Actions |
| `./gradlew assembleRelease` | **NO EJECUTADO** | GitHub Actions |

---

## 2. Por qué

El entorno de desarrollo no reunía los requisitos mínimos para compilar una
aplicación Android. Comprobado el 2026-08-24:

```
$ java -version
java version "25.0.1" 2025-10-21 LTS
Java(TM) SE Runtime Environment (build 25.0.1+8-LTS-27)

$ gradle -v
bash: gradle: command not found

$ echo "$ANDROID_HOME / $ANDROID_SDK_ROOT"
 /
```

Tres bloqueos, cada uno suficiente por sí solo:

1. **JDK 25.** El Android Gradle Plugin 8.5.2 requiere **JDK 17**. JDK 25 no
   está soportado y falla al arrancar el daemon.
2. **Gradle no instalado**, y el repositorio no versiona
   `gradle-wrapper.jar`, así que tampoco había forma de arrancar el wrapper.
3. **Sin SDK de Android.** `ANDROID_HOME` y `ANDROID_SDK_ROOT` están vacíos y no
   hay ninguna plataforma instalada, así que no existe `android.jar` contra el
   que compilar.

Instalar el SDK habría requerido descargas de red que quedan fuera de lo
acordado para este trabajo. La compilación se delega, por tanto, al workflow de
integración continua, que sí dispone de un entorno completo.

---

## 3. Qué SÍ se ejecutó y verificó

Todo lo que sigue se ejecutó de verdad en la máquina de desarrollo, con la
salida real reproducida a continuación.

### 3.1 Verificación de imports internos

```
$ python tools/check_imports.py
Ficheros Kotlin analizados: 96
Simbolos declarados: 1519

Todos los imports internos resuelven correctamente.
```

Comprueba que cada `import com.matelab.islas.*` apunta a un símbolo declarado
en el paquete indicado. Es el error más probable al escribir mucho código
seguido y el compilador lo rechazaría.

### 3.2 Verificación estructural

```
$ python tools/check_structure.py
Ficheros Kotlin equilibrados: 96
Referencias a recursos comprobadas: 17
GameKind: 12 valores / Payloads: 12

Sin problemas estructurales.
```

Cubre tres cosas: ningún fichero tiene llaves o paréntesis descuadrados (lo que
delataría un fichero truncado), todo `R.drawable/raw/string/color/xml` y todo
`@recurso/nombre` referenciado existe realmente en `res/`, y los 12 payloads de
mini-juego tienen rama en el despachador `ChallengeGame` y etiqueta en
`activityLabel`.

### 3.3 Verificación del contenido educativo

```
$ python tools/check_content.py
Misiones: 19
Retos: 95
Retos de eleccion: 19 (20 %)

Contenido dentro de los limites.
```

Reproduce fuera de Kotlin los límites que verifica `CatalogIntegrityTest`:
longitud de enunciados, explicaciones y pistas, ausencia de textos vacíos, ids
sin duplicar y proporción de retos de elección por debajo del 50 %.

### 3.4 Generación de recursos de audio

```
$ python tools/gen_sounds.py
Generando efectos de sonido de MateLab en app\src\main\res\raw
  sfx_tap.wav               1.9 KB
  sfx_correct.wav          10.5 KB
  sfx_wrong.wav            12.1 KB
  sfx_star.wav             15.1 KB
  sfx_unlock.wav           25.4 KB
  sfx_level.wav            32.7 KB
Listo.
```

Seis ficheros WAV PCM de 16 bits a 22 050 Hz, generados con la biblioteca
estándar de Python. No se descargó ningún recurso.

### 3.5 Generación y validación de los PDF

```
$ python tools/md2pdf.py
Generando PDF de MateLab
  MEMORIA_DESCRIPTIVA.pdf                   7 paginas    35.9 KB
  MANUAL_USUARIO.pdf                        5 paginas    26.4 KB
  MANUAL_TECNICO.pdf                        7 paginas    32.9 KB
Listo.

$ python tools/check_pdf.py
OK     docs/pdf/MANUAL_TECNICO.pdf                  7 paginas
OK     docs/pdf/MANUAL_USUARIO.pdf                  5 paginas
OK     docs/pdf/MEMORIA_DESCRIPTIVA.pdf             7 paginas
```

`check_pdf.py` valida la cabecera, que `startxref` apunte realmente a la tabla
`xref`, que cada desplazamiento caiga sobre su objeto y que el catálogo y las
páginas estén enlazados. Además, los tres PDF se abrieron en un visor y se
revisaron visualmente: portada, tipografía, tablas, listas y acentos.

### 3.6 Empaquetado

```
$ python tools/package.py
Ficheros dentro del ZIP: 144
Estructura del ZIP correcta (raiz directa, sin anidamiento).
```

---

## 4. Inventario del proyecto

Recuentos reales, obtenidos con `find` y `grep` sobre el árbol de fuentes:

| Métrica | Valor |
|---|---|
| Ficheros Kotlin de producción | 86 |
| Ficheros Kotlin de prueba | 10 |
| Líneas de Kotlin | 15 839 |
| Recursos XML | 13 |
| Efectos de sonido (WAV) | 6 |
| Pruebas unitarias (`@Test`) | **171** |
| Islas | 4 |
| Misiones | 19 |
| Retos | 95 |
| Tipos de mini-juego | 12 |
| Insignias | 13 |
| Cristales coleccionables | 27 |
| Tablas de base de datos | 13 |

### Pruebas por clase

| Clase | `@Test` |
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

**Pruebas aprobadas: sin determinar. Pruebas fallidas: sin determinar.**
Se sabrá al ejecutar `testDebugUnitTest` en GitHub Actions.

---

## 5. SHA-256 de los entregables generados

Calculados el 2026-08-24 sobre los ficheros reales de `deliverables/`:

| Fichero | Tamaño | SHA-256 |
|---|---|---|
| `MEMORIA_DESCRIPTIVA.pdf` | 36 763 B | `b1e13662f622aeb1b6a1798a9adbbdf7404910c182ef50479e6d00ff7612a482` |
| `MANUAL_USUARIO.pdf` | 27 010 B | `b6144b3aae93099bff02633d661f5530b78cd182953455de7d4ff5ab866587db` |
| `MANUAL_TECNICO.pdf` | 33 667 B | `56eb152879a58ea9f3c0767b66a224359a7baa90719914cf0dc1bf83ec7c341a` |

La huella de `MateLab-v1.0.0-source.zip` **no se reproduce aquí a propósito**:
este documento viaja dentro del propio ZIP, así que anotarla lo dejaría
desactualizado en cuanto se regenerase. Está en `deliverables/SHA256SUMS.txt`,
que `tools/package.py` escribe después de crear el paquete.

**SHA-256 del APK: no disponible.** Lo calculará el workflow y quedará en
`deliverables/BUILD_RESULT.md` dentro del artefacto de la ejecución.

---

## 6. Cómo obtener el informe real

1. Sube el proyecto a GitHub y haz push a `main`.
2. Abre la pestaña **Actions** → *Compilar APK de MateLab*.
3. El resumen de la ejecución muestra los SHA-256 y el tamaño de los APK.
4. Descarga los artefactos:
   - `MateLab-APK-v1.0.0`: los dos APK y `BUILD_RESULT.md`.
   - `MateLab-informes`: informe HTML de pruebas y de lint.
5. Sustituye las secciones 1 y 4 de este documento con los resultados reales.

### Qué esperar

- **Pruebas**: 171 casos, todos de JVM pura, sin emulador. Las de
  `CatalogIntegrityTest` fallarían si algún reto del catálogo estuviera mal
  configurado; se han verificado previamente con `tools/check_content.py`.
- **Lint**: configurado con `abortOnError = false`, así que no detiene la
  compilación. Es previsible que señale avisos de recursos no usados y de
  APIs de Compose marcadas como experimentales.
- **Riesgo principal**: al no haber pasado nunca por el compilador de Kotlin,
  pueden aparecer errores de tipos o de firmas de Compose que las
  comprobaciones estáticas no detectan. Si ocurre, el log de Actions indica
  fichero y línea exactos.

---

## 7. Reproducir la compilación en local

```bash
# 1. JDK 17 (imprescindible; con JDK 25 falla)
java -version   # debe decir 17.x

# 2. SDK de Android con la plataforma 34
export ANDROID_HOME=/ruta/al/sdk

# 3. Generar el wrapper la primera vez
gradle wrapper --gradle-version 8.9 --distribution-type bin

# 4. Compilar
./gradlew clean testDebugUnitTest lintDebug assembleDebug
```

---

## 8. Declaración de honestidad

Este informe cumple el punto 37 de la especificación. No se ha declarado
ninguna compilación correcta sin evidencia, no se ha inventado ningún número de
pruebas aprobadas y no se ha simulado ninguna salida de consola. Los bloques de
código de la sección 3 son transcripciones literales de ejecuciones reales; los
de la sección 1 están marcados como no ejecutados.
