# MateLab — Islas del Ingenio

Aplicación Android educativa para **niños de 8 a 12 años**. Geometría, medidas,
fracciones y sentido numérico a través de un archipiélago que se explora
resolviendo retos con las manos: geoplanos, transportadores, reglas, balanzas,
relojes, mosaicos simétricos, recta numérica y bloques de base diez.

- **100 % offline.** No usa Internet, no declara ningún permiso, no envía datos.
- **Sin cuentas.** Solo un apodo y un avatar guardados en el propio móvil.
- **Contenido real:** 4 islas, 19 misiones, 95 retos, 13 insignias y 27 cristales
  coleccionables.
- **Interfaz ilustrada:** todas las imágenes son vectoriales, dibujadas con
  Compose Canvas o vector drawables. No hay ni un solo PNG descargado.

---

## Cómo obtener el APK

El repositorio incluye un workflow de GitHub Actions que compila la app y
publica el APK automáticamente.

1. Sube el proyecto a un repositorio de GitHub:

```bash
git init && git add . && git commit -m "MateLab 1.0.0" && git branch -M main
```

2. Añade tu remoto y empuja:

```bash
git remote add origin https://github.com/USUARIO/REPOSITORIO.git && git push -u origin main
```

3. Entra en la pestaña **Actions** del repositorio. El workflow
   *Compilar APK de MateLab* se lanza solo con cada push a `main`.
4. Cuando termine, descarga el artefacto **MateLab-APK-v1.0.0**. Contiene
   `MateLab-v1.0.0-debug.apk`, `MateLab-v1.0.0-release.apk` y un
   `BUILD_RESULT.md` con los SHA-256 reales.

Si además creas una etiqueta `v1.0.0`, el workflow publica una *Release* de
GitHub con los APK adjuntos:

```bash
git tag v1.0.0 && git push origin v1.0.0
```

### Compilación local

Requiere **JDK 17** y el **SDK de Android 34**. El repositorio no versiona el
binario `gradle-wrapper.jar`, así que la primera vez hay que generarlo:

```bash
gradle wrapper --gradle-version 8.9 --distribution-type bin
```

Después:

```bash
./gradlew clean testDebugUnitTest lintDebug assembleDebug
```

El APK aparece en `app/build/outputs/apk/debug/app-debug.apk`.

---

## Qué hace el niño

```
Entra al archipiélago  ->  Kubo propone una misión  ->  Resuelve 5 retos
manipulando objetos    ->  Recibe explicación y estrellas  ->  Gana XP y un
Cristal de Ingenio     ->  Emerge una isla nueva  ->  Vuelve a explorar
```

Los primeros 30 segundos: el mapa del archipiélago con Kubo señalando la
siguiente misión y un botón grande de **Continuar**. En un toque ya está
tendiendo gomas en el geoplano.

## Las cuatro islas

| Isla | Contenido | Mini-juegos |
|---|---|---|
| **Bahía de las Formas** | Polígonos, ángulos, simetría, área y perímetro | Clasificador, geoplano, transportador, mosaicos |
| **Puerto Medida** | Longitud, masa, capacidad, tiempo y conversiones | Regla arrastrable, balanza, reloj de manecillas |
| **Volcán Fracción** | Partes, equivalencias, comparación y decimales | Reparto de porciones, recta numérica |
| **Cueva de los Números** | Valor posicional, patrones, multiplicar y dividir | Bloques de base diez, secuencias |

## Los doce mini-juegos

`GEOBOARD` · `SHAPE_SORT` · `ANGLE_DIAL` · `SYMMETRY` · `RULER` · `BALANCE` ·
`CLOCK` · `FRACTION_PIE` · `FRACTION_LINE` · `PLACE_VALUE` · `PATTERN` · `QUIZ`

Solo el 20 % del contenido son retos de elección; el resto es manipulación
directa. Cada mini-juego tiene detrás un motor de dominio puro y probado: el
área del geoplano se calcula con la fórmula del zapato, la balanza compara
masas reales, el reloj deriva la hora del ángulo de las manecillas.

## Tecnología

Kotlin · Jetpack Compose · Material 3 · Navigation Compose · MVVM + Repository ·
Room · Coroutines/Flow · Gradle Kotlin DSL · JDK 17 · minSdk 24 · targetSdk 34.

Sin Firebase, sin backend, sin analítica, sin anuncios, sin login.

## Estructura

```
app/          Código de la aplicación (data / domain / ui)
database/     schema.sql y sample_data.sql
docs/         Memoria, manuales e informe de compilación
deliverables/ Entregables (APK, ZIP y PDF)
.github/      Workflow de compilación automática
```

## Documentación

- [Memoria descriptiva](docs/MEMORIA_DESCRIPTIVA.md)
- [Manual de usuario](docs/MANUAL_USUARIO.md)
- [Manual técnico](docs/MANUAL_TECNICO.md)
- [Base de datos](docs/BASE_DE_DATOS.md)
- [Informe de compilación](docs/BUILD_REPORT.md)

## Privacidad

MateLab no pide permisos, no accede a Internet, ni a la cámara, ni al
micrófono, ni a la ubicación, ni a los contactos. El apodo, el avatar y el
progreso viven en una base de datos SQLite dentro de la app y desaparecen al
desinstalarla.
