-- =====================================================================
--  MateLab - Islas del Ingenio
--  Esquema de la base de datos local (SQLite / Room, version 1)
--
--  Este fichero reproduce el esquema que Room genera a partir de las
--  entidades de app/src/main/java/com/matelab/islas/data/local/entity.
--  Sirve como documentacion y para inspeccionar la base con cualquier
--  visor de SQLite.
--
--  Todos los datos son locales y anonimos. No hay ninguna columna que
--  permita identificar a una persona real.
-- =====================================================================

PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------------
-- CATALOGO DE CONTENIDO
-- Se rellena con la semilla al crear la base y se reescribe si sube la
-- version del catalogo. El progreso del nino no se toca nunca.
-- ---------------------------------------------------------------------

-- Islas del archipielago.
CREATE TABLE IF NOT EXISTS world (
    id            TEXT    NOT NULL PRIMARY KEY,
    order_index   INTEGER NOT NULL,
    name          TEXT    NOT NULL,
    subtitle      TEXT    NOT NULL,
    description   TEXT    NOT NULL,
    theme         TEXT    NOT NULL,   -- FORMAS | MEDIDA | FRACCION | NUMEROS
    xp_to_unlock  INTEGER NOT NULL
);

-- Misiones (sesiones cortas de 5 a 8 minutos).
CREATE TABLE IF NOT EXISTS mission (
    id                     TEXT    NOT NULL PRIMARY KEY,
    world_id               TEXT    NOT NULL,
    order_index            INTEGER NOT NULL,
    name                   TEXT    NOT NULL,
    goal                   TEXT    NOT NULL,
    briefing               TEXT    NOT NULL,
    difficulty             TEXT    NOT NULL,  -- EXPLORADOR | AVENTURERO | MAESTRO
    requires               TEXT    NOT NULL,  -- ids separados por coma
    reward_collectible_id  TEXT,
    FOREIGN KEY (world_id) REFERENCES world (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS index_mission_world_id ON mission (world_id);

-- Retos concretos. payload_json guarda la configuracion del mini-juego.
CREATE TABLE IF NOT EXISTS challenge (
    id            TEXT    NOT NULL PRIMARY KEY,
    mission_id    TEXT    NOT NULL,
    order_index   INTEGER NOT NULL,
    kind          TEXT    NOT NULL,  -- GEOBOARD, RULER, CLOCK, FRACTION_PIE...
    prompt        TEXT    NOT NULL,
    explanation   TEXT    NOT NULL,
    hint          TEXT    NOT NULL,
    payload_json  TEXT    NOT NULL,
    xp            INTEGER NOT NULL,
    FOREIGN KEY (mission_id) REFERENCES mission (id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS index_challenge_mission_id ON challenge (mission_id);

-- Insignias disponibles.
CREATE TABLE IF NOT EXISTS badge (
    id           TEXT    NOT NULL PRIMARY KEY,
    name         TEXT    NOT NULL,
    description  TEXT    NOT NULL,
    rule         TEXT    NOT NULL,  -- FIRST_MISSION, TOTAL_STARS, WORLD_COMPLETE...
    threshold    INTEGER NOT NULL,
    param        TEXT,
    art_seed     INTEGER NOT NULL
);

-- Cristales de Ingenio (coleccionables).
CREATE TABLE IF NOT EXISTS collectible (
    id        TEXT    NOT NULL PRIMARY KEY,
    name      TEXT    NOT NULL,
    fact      TEXT    NOT NULL,
    world_id  TEXT    NOT NULL,
    rarity    TEXT    NOT NULL,  -- COMUN | RARO | LEGENDARIO
    art_seed  INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS index_collectible_world_id ON collectible (world_id);

-- ---------------------------------------------------------------------
-- ESTADO DEL JUGADOR
-- ---------------------------------------------------------------------

-- Perfil unico (siempre id = 1). Solo apodo y avatar, nada personal.
CREATE TABLE IF NOT EXISTS profile (
    id                       INTEGER NOT NULL PRIMARY KEY,
    alias                    TEXT    NOT NULL,
    avatar_id                INTEGER NOT NULL,
    xp                       INTEGER NOT NULL,
    streak_days              INTEGER NOT NULL,
    last_played_day          INTEGER NOT NULL,
    onboarding_done          INTEGER NOT NULL,
    profile_done             INTEGER NOT NULL,
    review_sessions_cleared  INTEGER NOT NULL
);

-- Ajustes (siempre id = 1).
CREATE TABLE IF NOT EXISTS settings (
    id                  INTEGER NOT NULL PRIMARY KEY,
    sound_enabled       INTEGER NOT NULL,
    haptics_enabled     INTEGER NOT NULL,
    animations_enabled  INTEGER NOT NULL,
    big_text_enabled    INTEGER NOT NULL
);

-- Progreso por mision.
CREATE TABLE IF NOT EXISTS mission_progress (
    mission_id      TEXT    NOT NULL PRIMARY KEY,
    status          TEXT    NOT NULL,  -- BLOQUEADA | DISPONIBLE | EMPEZADA | COMPLETADA | DOMINADA
    stars           INTEGER NOT NULL,
    best_percent    INTEGER NOT NULL,
    times_played    INTEGER NOT NULL,
    no_hint_run     INTEGER NOT NULL,
    last_played_at  INTEGER NOT NULL
);

-- Historico de intentos. Es la fuente de las estadisticas.
CREATE TABLE IF NOT EXISTS attempt (
    id            INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    challenge_id  TEXT    NOT NULL,
    mission_id    TEXT    NOT NULL,
    world_id      TEXT    NOT NULL,
    correct       INTEGER NOT NULL,
    used_hint     INTEGER NOT NULL,
    elapsed_ms    INTEGER NOT NULL,
    timestamp     INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS index_attempt_mission_id ON attempt (mission_id);
CREATE INDEX IF NOT EXISTS index_attempt_world_id   ON attempt (world_id);
CREATE INDEX IF NOT EXISTS index_attempt_timestamp  ON attempt (timestamp);

-- Insignias conseguidas.
CREATE TABLE IF NOT EXISTS badge_unlock (
    badge_id     TEXT    NOT NULL PRIMARY KEY,
    unlocked_at  INTEGER NOT NULL
);

-- Cristales conseguidos.
CREATE TABLE IF NOT EXISTS collectible_unlock (
    collectible_id  TEXT    NOT NULL PRIMARY KEY,
    unlocked_at     INTEGER NOT NULL
);

-- Retos pendientes de repasar.
CREATE TABLE IF NOT EXISTS review_item (
    challenge_id   TEXT    NOT NULL PRIMARY KEY,
    mission_id     TEXT    NOT NULL,
    world_id       TEXT    NOT NULL,
    wrong_count    INTEGER NOT NULL,
    last_wrong_at  INTEGER NOT NULL,
    resolved       INTEGER NOT NULL
);

-- Pares clave/valor internos (version del catalogo, contadores).
CREATE TABLE IF NOT EXISTS meta (
    key    TEXT NOT NULL PRIMARY KEY,
    value  TEXT NOT NULL
);

-- ---------------------------------------------------------------------
-- CONSULTAS DE APOYO USADAS POR LA APP
-- ---------------------------------------------------------------------

-- Aciertos por isla (pantalla de progreso).
--   SELECT world_id,
--          COUNT(*) AS total,
--          SUM(CASE WHEN correct = 1 THEN 1 ELSE 0 END) AS correct
--   FROM attempt
--   GROUP BY world_id;

-- Estrellas totales (insignias y mapa).
--   SELECT COALESCE(SUM(stars), 0) FROM mission_progress;

-- Retos pendientes de repaso (taller).
--   SELECT COUNT(*) FROM review_item WHERE resolved = 0;
