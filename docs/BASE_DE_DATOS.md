# Base de datos

## MateLab — Islas del Ingenio

Motor: **SQLite** a través de **Room 2.6.1**.
Fichero: `matelab.db`, en el almacenamiento privado de la app.
Versión de esquema: **1**.

Scripts: [`database/schema.sql`](../database/schema.sql) y
[`database/sample_data.sql`](../database/sample_data.sql).

---

## 1. Visión general

13 tablas repartidas en dos bloques con ciclos de vida distintos:

```
CATÁLOGO (se puede reescribir)          JUGADOR (nunca se reescribe solo)
┌──────────────────────────┐            ┌────────────────────────────────┐
│ world                    │            │ profile                        │
│   └── mission            │            │ settings                       │
│         └── challenge    │            │ mission_progress               │
│ badge                    │            │ attempt                        │
│ collectible              │            │ badge_unlock                   │
└──────────────────────────┘            │ collectible_unlock             │
                                        │ review_item                    │
                                        │ meta                           │
                                        └────────────────────────────────┘
```

El catálogo se vuelve a escribir cuando sube `Catalog.VERSION`; el progreso solo
se borra si el niño pulsa *Reiniciar mi progreso*.

### Relaciones

```
world (1) ──< mission (1) ──< challenge
   │                              │
   │ ON DELETE CASCADE            │ ON DELETE CASCADE
   │
mission.reward_collectible_id ──> collectible.id   (referencia lógica)
badge.param ─────────────────────> world.id        (referencia lógica)
```

Las dos últimas son referencias lógicas, no claves foráneas: `badge.param` es
polivalente (puede contener un id de isla o quedar a nulo según la regla) y el
premio de misión puede apuntar a un cristal que aún no se haya insertado
durante la siembra. La integridad se garantiza con
`Catalog.integrityProblems()` y su prueba unitaria.

---

## 2. Tablas del catálogo

### 2.1 `world` — islas

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT PK | `w_formas`, `w_medida`, `w_fraccion`, `w_numeros` |
| `order_index` | INTEGER | Orden en el mapa |
| `name` | TEXT | "Bahía de las Formas" |
| `subtitle` | TEXT | "Polígonos, ángulos y simetría" |
| `description` | TEXT | Ambientación mostrada en la cabecera de la isla |
| `theme` | TEXT | `FORMAS` \| `MEDIDA` \| `FRACCION` \| `NUMEROS` |
| `xp_to_unlock` | INTEGER | 0, 120, 320, 560 |

4 filas.

### 2.2 `mission` — misiones

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT PK | `m_f1`, `m_m3`, … |
| `world_id` | TEXT FK → `world.id` | Cascada |
| `order_index` | INTEGER | Orden dentro de la isla |
| `name`, `goal`, `briefing` | TEXT | Título, objetivo y frase de Kubo |
| `difficulty` | TEXT | `EXPLORADOR` \| `AVENTURERO` \| `MAESTRO` |
| `requires` | TEXT | Ids separados por coma; vacío si no hay requisitos |
| `reward_collectible_id` | TEXT NULL | Cristal que entrega |

19 filas. Índice en `world_id`.

> `requires` se guarda como cadena y no como tabla puente porque casi siempre
> tiene 0, 1 o 2 elementos y solo se lee entera. El mapper la convierte en
> `List<String>`.

### 2.3 `challenge` — retos

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT PK | `c_f1_1`, … |
| `mission_id` | TEXT FK → `mission.id` | Cascada |
| `order_index` | INTEGER | Orden dentro de la misión |
| `kind` | TEXT | `GEOBOARD`, `RULER`, `CLOCK`, … (12 valores) |
| `prompt` | TEXT | Enunciado (≤ 130 caracteres) |
| `explanation` | TEXT | Explicación educativa (≤ 260) |
| `hint` | TEXT | Pista (≤ 130) |
| `payload_json` | TEXT | Configuración del mini-juego en JSON |
| `xp` | INTEGER | Valor base del reto |

95 filas. Índice en `mission_id`.

#### El campo `payload_json`

JSON polimórfico de kotlinx.serialization con discriminador `"type"`:

```json
{"type":"geoboard","grid":6,"objective":"AREA","target":4.0,
 "minVertices":3,"unitLabel":"cuadraditos"}

{"type":"balance","leftGrams":300,"leftLabel":"Saco de arroz",
 "weights":[500,200,100,50],"maxPerWeight":3}

{"type":"clock","startHour":12,"startMinute":0,"mode":"PONER_HORA",
 "deltaMinutes":0,"targetHour":3,"targetMinute":15}
```

| `type` | Campos propios |
|---|---|
| `geoboard` | `grid`, `objective`, `target`, `minVertices`, `unitLabel` |
| `shape_sort` | `shapes[]`, `buckets[]` |
| `angle_dial` | `targetDegrees`, `tolerance`, `askClassification`, `showProtractor` |
| `symmetry` | `rows`, `cols`, `axis`, `given[]` |
| `ruler` | `objectMm`, `toleranceMm`, `answerUnit`, `objectKind` |
| `balance` | `leftGrams`, `leftLabel`, `weights[]`, `maxPerWeight` |
| `clock` | `startHour`, `startMinute`, `mode`, `deltaMinutes`, `targetHour`, `targetMinute` |
| `fraction_pie` | `shape`, `parts`, `targetNumerator`, `targetDenominator`, `mode` |
| `fraction_line` | `denominator`, `numerator`, `wholes`, `toleranceSteps`, `decimalLabels` |
| `place_value` | `target`, `pieces[]`, `maxPerPiece` |
| `pattern` | `sequence[]`, `holeIndex`, `options[]`, `answerIndex`, `rule` |
| `quiz` | `options[]`, `answerIndex`, `art` |

Se eligió JSON frente a una columna por parámetro porque doce mini-juegos con
configuraciones dispares producirían una tabla ancha y llena de nulos, y porque
añadir un juego nuevo no requiere migrar el esquema.

### 2.4 `badge` — insignias

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT PK | `b_primer_paso`, … |
| `name`, `description` | TEXT | |
| `rule` | TEXT | 10 reglas posibles (ver más abajo) |
| `threshold` | INTEGER | Umbral que interpreta la regla |
| `param` | TEXT NULL | Id de isla para reglas por isla |
| `art_seed` | INTEGER | Semilla de dibujo, única |

13 filas.

Reglas: `FIRST_MISSION`, `WORLD_COMPLETE`, `PERFECT_MISSION`, `TOTAL_STARS`,
`TOTAL_XP`, `STREAK_DAYS`, `COLLECTION_SIZE`, `REVIEW_CLEARED`,
`NO_HINT_MISSION`, `TOPIC_MASTER`.

### 2.5 `collectible` — cristales

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT PK | `cr_faro`, `cr_hito_nivel5`, … |
| `name` | TEXT | |
| `fact` | TEXT | Dato matemático que se revela al conseguirlo |
| `world_id` | TEXT | Isla temática |
| `rarity` | TEXT | `COMUN` \| `RARO` \| `LEGENDARIO` |
| `art_seed` | INTEGER | Semilla de dibujo, única |

27 filas (19 de misión + 8 de hito). Índice en `world_id`.

---

## 3. Tablas del jugador

### 3.1 `profile` — perfil

Fila única con `id = 1`.

| Columna | Tipo | Notas |
|---|---|---|
| `id` | INTEGER PK | Siempre 1 |
| `alias` | TEXT | Apodo, ≤ 14 caracteres. **Nunca el nombre real** |
| `avatar_id` | INTEGER | 0-7 |
| `xp` | INTEGER | Experiencia acumulada |
| `streak_days` | INTEGER | Días seguidos jugando |
| `last_played_day` | INTEGER | Día epoch (ms / 86 400 000) |
| `onboarding_done` | INTEGER | 0/1 |
| `profile_done` | INTEGER | 0/1 |
| `review_sessions_cleared` | INTEGER | Repasos superados sin fallos |

El nivel **no se almacena**: se deriva del XP con `ProgressEngine.levelFor`, para
que no puedan quedar desincronizados.

### 3.2 `settings` — ajustes

Fila única con `id = 1`: `sound_enabled`, `haptics_enabled`,
`animations_enabled`, `big_text_enabled` (todos 0/1).

### 3.3 `mission_progress` — progreso por misión

| Columna | Tipo | Notas |
|---|---|---|
| `mission_id` | TEXT PK | |
| `status` | TEXT | `BLOQUEADA` \| `DISPONIBLE` \| `EMPEZADA` \| `COMPLETADA` \| `DOMINADA` |
| `stars` | INTEGER | 0-3, **máximo histórico** |
| `best_percent` | INTEGER | Mejor porcentaje, nunca baja |
| `times_played` | INTEGER | Veces jugada |
| `no_hint_run` | INTEGER | 1 si alguna vez se superó sin pistas |
| `last_played_at` | INTEGER | Epoch en ms |

Solo existen filas de misiones ya jugadas. El estado de las demás se calcula.

### 3.4 `attempt` — historial de intentos

| Columna | Tipo | Notas |
|---|---|---|
| `id` | INTEGER PK AUTOINCREMENT | |
| `challenge_id`, `mission_id`, `world_id` | TEXT | Desnormalizado a propósito |
| `correct`, `used_hint` | INTEGER | 0/1 |
| `elapsed_ms` | INTEGER | Tiempo del reto |
| `timestamp` | INTEGER | Epoch en ms |

Índices en `mission_id`, `world_id` y `timestamp`.

Es la **única fuente** de la pantalla de progreso: ningún porcentaje está
escrito a mano. Se guardan `mission_id` y `world_id` en la propia fila para que
la agregación por isla sea un `GROUP BY` sin joins.

### 3.5 `badge_unlock` y `collectible_unlock`

Dos tablas idénticas en forma: identificador como clave primaria y
`unlocked_at` en epoch. La ausencia de fila significa "no conseguido".

### 3.6 `review_item` — taller de repaso

| Columna | Tipo | Notas |
|---|---|---|
| `challenge_id` | TEXT PK | |
| `mission_id`, `world_id` | TEXT | |
| `wrong_count` | INTEGER | Veces fallado; ordena la sesión |
| `last_wrong_at` | INTEGER | Desempate |
| `resolved` | INTEGER | 1 cuando se acierta |

Las filas resueltas se conservan para no volver a crearlas si el reto se falla
otra vez más adelante.

### 3.7 `meta` — clave/valor

Solo `catalog_version` por ahora. Permite detectar que el contenido embebido es
más nuevo que el sembrado y reescribirlo sin tocar el progreso.

---

## 4. Consultas principales

**Aciertos por isla** (pantalla de progreso):

```sql
SELECT world_id AS worldId,
       COUNT(*) AS total,
       SUM(CASE WHEN correct = 1 THEN 1 ELSE 0 END) AS correct
FROM attempt
GROUP BY world_id;
```

**Estrellas totales** (mapa e insignias):

```sql
SELECT COALESCE(SUM(stars), 0) FROM mission_progress;
```

**Misiones completadas y perfectas**:

```sql
SELECT COUNT(*) FROM mission_progress WHERE stars >= 1;
SELECT COUNT(*) FROM mission_progress WHERE stars >= 3;
```

**Retos pendientes de repaso** (se observa como `Flow<Int>`):

```sql
SELECT COUNT(*) FROM review_item WHERE resolved = 0;
```

**Sesión de repaso**:

```sql
SELECT * FROM review_item ORDER BY wrong_count DESC, last_wrong_at ASC;
```

**Actividad de los últimos 7 días**: se leen los `timestamp` posteriores al
corte y el reparto por día se hace en Kotlin, para no depender de las funciones
de fecha de SQLite ni de la zona horaria del dispositivo.

---

## 5. Ciclo de vida

### Primera apertura (`onCreate`)

1. Room crea las 13 tablas.
2. `DatabaseSeeder.seedCatalog` inserta 4 islas, 19 misiones, 95 retos, 13
   insignias y 27 cristales, y escribe `meta.catalog_version`.
3. `DatabaseSeeder.seedPlayerDefaults` crea el perfil y los ajustes por defecto
   con `INSERT OR IGNORE`.

### Aperturas siguientes (`onOpen`)

Si `meta.catalog_version` difiere o `world` está vacía, se reescribe el
catálogo. El progreso no se toca nunca.

### Actualización de esquema

`fallbackToDestructiveMigration()` está activo. Es una decisión consciente: el
contenido se regenera desde la semilla y una migración fallida no puede dejar
la app inservible. El coste es perder el progreso local en un cambio de
esquema, asumible en una app sin cuenta ni sincronización.

### Reinicio de progreso

`ProgressRepository.resetProgress()` vacía `mission_progress`, `attempt`,
`review_item`, `badge_unlock` y `collectible_unlock`, y pone a cero XP, racha y
repasos superados. El catálogo, el apodo y el avatar se conservan.

---

## 6. Privacidad de los datos

Ninguna tabla contiene datos personales:

- No hay nombre real, edad, correo, teléfono, dirección ni ubicación.
- `alias` es un apodo libre, opcional y limitado a 14 caracteres.
- No hay identificadores de dispositivo ni de publicidad.
- Los `timestamp` son locales y solo alimentan el gráfico de 7 días.
- La base vive en el sandbox de la app y desaparece al desinstalarla.

La copia de seguridad de Android está limitada en `res/xml/backup_rules.xml` y
`res/xml/data_extraction_rules.xml` a `matelab.db`, para que el progreso pueda
sobrevivir a un cambio de móvil sin exponer nada más.

---

## 7. Inspeccionar la base

```bash
adb shell "run-as com.matelab.islas cat databases/matelab.db" > matelab.db
sqlite3 matelab.db ".tables"
sqlite3 matelab.db "SELECT id, name, xp_to_unlock FROM world ORDER BY order_index;"
```

Requiere una compilación depurable. Para crear una base de prueba desde cero:

```bash
sqlite3 prueba.db < database/schema.sql
sqlite3 prueba.db < database/sample_data.sql
```
