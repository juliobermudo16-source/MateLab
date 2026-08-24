# Entregables — MateLab v1.0.0

## Contenido de esta carpeta

| Fichero | Qué es |
|---|---|
| `MateLab-v1.0.0-source.zip` | Código fuente completo (144 ficheros) |
| `MEMORIA_DESCRIPTIVA.pdf` | Diseño, contenido y arquitectura (7 páginas) |
| `MANUAL_USUARIO.pdf` | Guía para niños, familias y profesorado (5 páginas) |
| `MANUAL_TECNICO.pdf` | Compilación y mantenimiento (7 páginas) |
| `SHA256SUMS.txt` | Huellas SHA-256 de los entregables |

---

## Sobre el APK

**El APK no está en esta carpeta.** No se pudo compilar en la máquina de
desarrollo: tenía JDK 25 (el proyecto necesita JDK 17), sin Gradle y sin SDK de
Android instalado. El detalle completo, con la salida real de los comandos,
está en `docs/BUILD_REPORT.md`.

El APK lo genera **GitHub Actions** en tres pasos:

**1. Sube el proyecto a GitHub**

```bash
git init && git add . && git commit -m "MateLab 1.0.0" && git branch -M main
```

```bash
git remote add origin https://github.com/USUARIO/REPOSITORIO.git && git push -u origin main
```

**2. Espera al workflow**

Entra en la pestaña **Actions** del repositorio. *Compilar APK de MateLab* se
lanza solo. Tarda unos 5-10 minutos: instala JDK 17 y el SDK 34, genera el
wrapper de Gradle, ejecuta las 171 pruebas, pasa lint y compila los dos APK.

**3. Descarga el resultado**

Al terminar, en la misma página aparecen dos artefactos:

- **`MateLab-APK-v1.0.0`** — contiene `MateLab-v1.0.0-debug.apk`,
  `MateLab-v1.0.0-release.apk` y un `BUILD_RESULT.md` con los SHA-256 reales.
- **`MateLab-informes`** — informe HTML de las pruebas y de lint.

### Publicar una versión

Si además creas una etiqueta, el workflow publica una *Release* con los APK
adjuntos, lista para compartir:

```bash
git tag v1.0.0 && git push origin v1.0.0
```

---

## Instalar el APK

1. Copia el `.apk` al móvil o la tablet (Android 7.0 o superior).
2. Ábrelo desde la app de Archivos.
3. Acepta el aviso de instalar aplicaciones de esta procedencia.
4. Pulsa **Instalar**.

Usa `MateLab-v1.0.0-release.apk`: va optimizado con R8 y ocupa menos. El de
depuración sirve para inspeccionar la base de datos con `adb`.

> Ambos APK van firmados con la clave de depuración, suficiente para instalar y
> repartir el fichero directamente. Para subir la app a Google Play hay que
> firmarla con un keystore propio (ver `docs/MANUAL_TECNICO.md`, apartado 2.3).

---

## Compilar en local

Requiere **JDK 17** y el **SDK de Android 34**.

```bash
gradle wrapper --gradle-version 8.9 --distribution-type bin
```

```bash
./gradlew clean testDebugUnitTest lintDebug assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

---

## Comprobar las huellas

```bash
sha256sum -c SHA256SUMS.txt
```

En Windows:

```bash
certutil -hashfile MateLab-v1.0.0-source.zip SHA256
```
