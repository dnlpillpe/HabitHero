-- =====================================================================
-- HabitHero - datos semilla
-- ---------------------------------------------------------------------
-- Equivalente legible de lo que hace DatabaseSeeder.kt la primera vez
-- que se crea la base de datos (RoomDatabase.Callback.onCreate) y cada
-- vez que se usa "Reiniciar todos los datos" en Ajustes.
--
-- Las fechas van como días desde 1970-01-01. Aquí se usan valores
-- calculados con la función date() de SQLite para que el archivo se
-- pueda ejecutar a mano en cualquier momento.
-- =====================================================================

-- El héroe. onboardingCompletado = 0 hace que la app muestre la
-- pantalla de bienvenida en el primer arranque.
INSERT OR IGNORE INTO user_profile
    (id, nombre, avatar, monedas, experiencia, nivel, fechaCreacion, onboardingCompletado)
VALUES
    (1, 'Héroe', '🦸', 0, 0, 1, CAST(julianday('now') - 2440587.5 AS INTEGER), 0);

-- Los seis hábitos predeterminados.
INSERT OR IGNORE INTO habit
    (id, nombre, icono, diasSemana, colorIndex, esPredeterminado, activo, orden, fechaCreacion)
VALUES
    (1, 'Cepillarse los dientes', '🦷', '1,2,3,4,5,6,7', 0, 1, 1, 0, CAST(julianday('now') - 2440587.5 AS INTEGER)),
    (2, 'Beber agua',             '💧', '1,2,3,4,5,6,7', 1, 1, 1, 1, CAST(julianday('now') - 2440587.5 AS INTEGER)),
    (3, 'Leer un rato',           '📚', '1,2,3,4,5',     2, 1, 1, 2, CAST(julianday('now') - 2440587.5 AS INTEGER)),
    (4, 'Ordenar el dormitorio',  '🧹', '1,2,3,4,5,6,7', 3, 1, 1, 3, CAST(julianday('now') - 2440587.5 AS INTEGER)),
    (5, 'Preparar la mochila',    '🎒', '1,2,3,4,5',     4, 1, 1, 4, CAST(julianday('now') - 2440587.5 AS INTEGER)),
    (6, 'Dormir a tiempo',        '😴', '1,2,3,4,5,6,7', 5, 1, 1, 5, CAST(julianday('now') - 2440587.5 AS INTEGER));

-- Catálogo de insignias.
INSERT OR IGNORE INTO badge (id, nombre, descripcion, icono, tipo, meta, orden) VALUES
    ('primer_paso',  'Primer paso',         'Marca tu primer hábito',        '🌟', 'TOTAL_MARCAS',  1,   0),
    ('constante_10', 'Diez marcas',         'Completa 10 hábitos en total',  '✅', 'TOTAL_MARCAS',  10,  1),
    ('racha_3',      'Tres seguidos',       'Consigue una racha de 3 días',  '🔥', 'RACHA',         3,   2),
    ('racha_7',      'Semana heroica',      'Consigue una racha de 7 días',  '🏆', 'RACHA',         7,   3),
    ('cofre_100',    'Cofre lleno',         'Junta 100 monedas',             '💰', 'MONEDAS',       100, 4),
    ('nivel_5',      'Nivel 5',             'Alcanza el nivel 5',            '🚀', 'NIVEL',         5,   5),
    ('creador',      'Inventor de hábitos', 'Crea un hábito propio',         '🎨', 'HABITO_PROPIO', 1,   6);

-- Marcas de ejemplo de los días anteriores, para que la app tenga
-- contenido visible en Progreso desde el primer arranque.
INSERT OR IGNORE INTO habit_completion (habitId, fecha, monedasGanadas, experienciaGanada) VALUES
    (1, CAST(julianday('now', '-1 day')  - 2440587.5 AS INTEGER), 5, 10),
    (1, CAST(julianday('now', '-2 days') - 2440587.5 AS INTEGER), 5, 10),
    (1, CAST(julianday('now', '-3 days') - 2440587.5 AS INTEGER), 5, 10),
    (2, CAST(julianday('now', '-1 day')  - 2440587.5 AS INTEGER), 5, 10),
    (2, CAST(julianday('now', '-2 days') - 2440587.5 AS INTEGER), 5, 10),
    (3, CAST(julianday('now', '-2 days') - 2440587.5 AS INTEGER), 5, 10),
    (6, CAST(julianday('now', '-1 day')  - 2440587.5 AS INTEGER), 5, 10);

-- Nota: user_badge empieza vacía a propósito. Las insignias se conceden
-- desde la app cuando se cumplen las condiciones, nunca de antemano.
