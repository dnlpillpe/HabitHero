-- =====================================================================
-- HabitHero - esquema de la base de datos local (SQLite / Room v2)
-- ---------------------------------------------------------------------
-- Este archivo es la versión legible del esquema que Room genera en
-- tiempo de compilación a partir de las entidades anotadas.
-- El esquema real exportado por Room queda en app/schemas/2.json
-- después de compilar (1.json solo existiría si el proyecto se hubiera
-- compilado alguna vez en versión 1; la migración 1→2 no depende de él,
-- ver data/local/Migraciones.kt).
--
-- Convenciones:
--   * Las fechas se guardan como INTEGER = días desde 1970-01-01
--     (LocalDate.toEpochDay). Ver data/local/Converters.kt.
--   * Los booleanos se guardan como INTEGER (0 = falso, 1 = verdadero).
--   * diasSemana se guarda como TEXT con números ISO-8601 separados por
--     comas: 1 = lunes ... 7 = domingo. Ejemplo: '1,2,3,4,5'.
--   * horaRecordatorioMinutos es el minuto del día (0..1439); NULL
--     significa que el hábito no tiene recordatorio.
--   * categoria guarda el nombre de una constante de domain/Categoria.kt
--     (SALUD, ESTUDIO, HOGAR, EJERCICIO, OTROS).
-- =====================================================================

PRAGMA foreign_keys = ON;

-- ---------------------------------------------------------------------
-- user_profile: el héroe. Siempre existe una única fila, con id = 1.
-- marcoSeleccionado es el id de un item de la tienda (tabla user_unlock)
-- de tipo MARCO ya comprado; NULL si no hay ninguno equipado.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_profile (
    id                    INTEGER NOT NULL PRIMARY KEY,
    nombre                TEXT    NOT NULL,
    avatar                TEXT    NOT NULL,
    monedas               INTEGER NOT NULL DEFAULT 0,
    experiencia           INTEGER NOT NULL DEFAULT 0,
    nivel                 INTEGER NOT NULL DEFAULT 1,
    fechaCreacion         INTEGER NOT NULL,
    onboardingCompletado  INTEGER NOT NULL DEFAULT 0,
    marcoSeleccionado     TEXT
);

-- ---------------------------------------------------------------------
-- habit: cada hábito que el héroe quiere repetir.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS habit (
    id                        INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    nombre                    TEXT    NOT NULL,          -- máximo 30 caracteres (validado en la app)
    icono                     TEXT    NOT NULL,          -- emoji
    diasSemana                TEXT    NOT NULL,          -- '1,2,3,4,5,6,7'
    colorIndex                INTEGER NOT NULL DEFAULT 0,
    esPredeterminado          INTEGER NOT NULL DEFAULT 0,
    activo                    INTEGER NOT NULL DEFAULT 1,
    orden                     INTEGER NOT NULL DEFAULT 0,
    fechaCreacion             INTEGER NOT NULL,
    horaRecordatorioMinutos   INTEGER,                   -- minuto del día (0..1439); NULL = sin recordatorio
    categoria                 TEXT    NOT NULL DEFAULT 'OTROS'
);

-- ---------------------------------------------------------------------
-- habit_completion: una marca de un hábito en un día.
-- El índice único (habitId, fecha) es la regla que impide marcar dos
-- veces el mismo hábito el mismo día.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS habit_completion (
    id                  INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    habitId             INTEGER NOT NULL,
    fecha               INTEGER NOT NULL,
    monedasGanadas      INTEGER NOT NULL DEFAULT 0,
    experienciaGanada   INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (habitId) REFERENCES habit (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS index_habit_completion_habitId_fecha
    ON habit_completion (habitId, fecha);

CREATE INDEX IF NOT EXISTS index_habit_completion_fecha
    ON habit_completion (fecha);

-- ---------------------------------------------------------------------
-- badge: catálogo de insignias. Se rellena con datos semilla.
-- tipo ∈ ('TOTAL_MARCAS', 'RACHA', 'MONEDAS', 'NIVEL', 'HABITO_PROPIO')
-- meta = valor que hay que alcanzar para conseguirla.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS badge (
    id           TEXT    NOT NULL PRIMARY KEY,
    nombre       TEXT    NOT NULL,
    descripcion  TEXT    NOT NULL,
    icono        TEXT    NOT NULL,
    tipo         TEXT    NOT NULL,
    meta         INTEGER NOT NULL,
    orden        INTEGER NOT NULL DEFAULT 0
);

-- ---------------------------------------------------------------------
-- user_badge: insignias ya conseguidas por el héroe.
-- Una insignia solo puede conseguirse una vez: badgeId es la clave.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_badge (
    badgeId         TEXT    NOT NULL PRIMARY KEY,
    fechaObtencion  INTEGER NOT NULL,
    vista           INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (badgeId) REFERENCES badge (id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- user_unlock: items de la tienda (avatares y marcos) ya comprados.
-- El catálogo en sí (nombre, precio, emoji) NO vive en la base de
-- datos: es contenido fijo de la app, hardcodeado en
-- util/TiendaCatalogo.kt. Esta tabla solo guarda qué compró el héroe.
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_unlock (
    itemId          TEXT    NOT NULL PRIMARY KEY,
    fechaAdquirido  INTEGER NOT NULL
);

-- ---------------------------------------------------------------------
-- daily_challenge: el desafío sorpresa del día. Se genera de forma
-- determinista (misma fecha = mismo desafío) la primera vez que se
-- consulta ese día, y se guarda para que "completado" no se pueda
-- reiniciar cerrando y abriendo la app.
-- tipo ∈ ('TRES_HABITOS', 'TODOS_HOY', 'ANTES_DE_HORA')
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS daily_challenge (
    fecha                  INTEGER NOT NULL PRIMARY KEY,
    tipo                   TEXT    NOT NULL,
    meta                   INTEGER NOT NULL,
    completado             INTEGER NOT NULL DEFAULT 0,
    recompensaMonedas      INTEGER NOT NULL,
    recompensaExperiencia  INTEGER NOT NULL
);
