# Base de datos de HabitHero

Motor: **SQLite**, gestionado con **Room 2.6.1**.
Archivo en el dispositivo: `habithero.db` (carpeta privada de la app).
Versión del esquema: **1**.

Todo es local. No hay sincronización, ni servidor, ni copia en la nube más allá
de la copia de seguridad estándar de Android.

---

## 1. Convenciones

| Concepto | Cómo se guarda |
|---|---|
| Fechas | `INTEGER` = días desde 1970-01-01 (`LocalDate.toEpochDay()`) |
| Booleanos | `INTEGER`: `0` = falso, `1` = verdadero |
| Días de la semana | `TEXT` con números ISO-8601 separados por comas (`1` = lunes … `7` = domingo). Ejemplo: `'1,2,3,4,5'` |
| Iconos y avatares | `TEXT` con un emoji |

Las conversiones están en `data/local/Converters.kt`.

---

## 2. Diagrama entidad-relación

```mermaid
erDiagram
    USER_PROFILE {
        int     id PK "siempre 1"
        text    nombre
        text    avatar
        int     monedas
        int     experiencia
        int     nivel
        int     fechaCreacion "epoch day"
        int     onboardingCompletado "0/1"
    }

    HABIT {
        int     id PK "autoincremental"
        text    nombre "máx. 30 caracteres"
        text    icono "emoji"
        text    diasSemana "1..7 separados por comas"
        int     colorIndex
        int     esPredeterminado "0/1"
        int     activo "0/1"
        int     orden
        int     fechaCreacion "epoch day"
    }

    HABIT_COMPLETION {
        int     id PK "autoincremental"
        int     habitId FK
        int     fecha "epoch day"
        int     monedasGanadas
        int     experienciaGanada
    }

    BADGE {
        text    id PK "código"
        text    nombre
        text    descripcion
        text    icono "emoji"
        text    tipo "condición"
        int     meta
        int     orden
    }

    USER_BADGE {
        text    badgeId PK-FK
        int     fechaObtencion "epoch day"
        int     vista "0/1"
    }

    HABIT ||--o{ HABIT_COMPLETION : "se marca en"
    BADGE ||--o| USER_BADGE : "se consigue como"
    USER_PROFILE ||..o{ HABIT : "pertenecen al único héroe"
    USER_PROFILE ||..o{ USER_BADGE : "las gana el único héroe"
```

> Las relaciones con `USER_PROFILE` son lógicas, no claves foráneas: como solo
> existe un perfil (`id = 1`), guardar un `userId` en cada tabla sería una
> columna con el mismo valor en todas las filas.

---

## 3. Tablas

### 3.1 `user_profile`

El héroe. **Siempre hay exactamente una fila**, con `id = 1`.

| Campo | Tipo | Nulo | Clave | Descripción |
|---|---|---|---|---|
| `id` | INTEGER | No | PK | Constante `1`. |
| `nombre` | TEXT | No | | Nombre del niño. Máximo 20 caracteres. Puede quedar vacío. |
| `avatar` | TEXT | No | | Emoji elegido en la bienvenida. |
| `monedas` | INTEGER | No | | Monedas acumuladas. Nunca baja de 0. |
| `experiencia` | INTEGER | No | | Experiencia acumulada. Nunca baja de 0. |
| `nivel` | INTEGER | No | | Derivado: `experiencia / 100 + 1`. |
| `fechaCreacion` | INTEGER | No | | Día en que se creó el perfil. |
| `onboardingCompletado` | INTEGER | No | | `0` mientras no se haya visto la bienvenida. |

**Reglas**

- `nivel` se recalcula en cada cambio de `experiencia`; no se edita a mano.
- Al desmarcar un hábito se restan las recompensas que dio esa marca concreta,
  con suelo en 0.

### 3.2 `habit`

| Campo | Tipo | Nulo | Clave | Descripción |
|---|---|---|---|---|
| `id` | INTEGER | No | PK, autoincremental | |
| `nombre` | TEXT | No | | Máximo 30 caracteres (validado en la interfaz). |
| `icono` | TEXT | No | | Emoji del catálogo. |
| `diasSemana` | TEXT | No | | Días programados. No puede quedar vacío. |
| `colorIndex` | INTEGER | No | | Índice en la paleta de 6 colores. |
| `esPredeterminado` | INTEGER | No | | `1` para los seis hábitos semilla. |
| `activo` | INTEGER | No | | `1` si aparece en la lista. |
| `orden` | INTEGER | No | | Orden de presentación. |
| `fechaCreacion` | INTEGER | No | | |

**Reglas**

- Un hábito solo aparece en Inicio si `activo = 1` **y** hoy está en `diasSemana`.
- Borrar un hábito borra sus marcas en cascada, pero no toca monedas ni insignias.

### 3.3 `habit_completion`

Una fila = un hábito marcado un día.

| Campo | Tipo | Nulo | Clave | Descripción |
|---|---|---|---|---|
| `id` | INTEGER | No | PK, autoincremental | |
| `habitId` | INTEGER | No | FK → `habit.id` (ON DELETE CASCADE) | |
| `fecha` | INTEGER | No | | Día de la marca. |
| `monedasGanadas` | INTEGER | No | | Lo que dio esta marca, para poder devolverlo. |
| `experienciaGanada` | INTEGER | No | | Ídem. |

**Índices**

| Índice | Columnas | Único |
|---|---|---|
| `index_habit_completion_habitId_fecha` | (`habitId`, `fecha`) | **Sí** |
| `index_habit_completion_fecha` | (`fecha`) | No |

**Reglas**

- El índice único es la garantía real de *una marca por hábito y día*: la
  inserción usa `OnConflictStrategy.IGNORE`, así que un segundo intento devuelve
  `-1` y la aplicación lo trata como «ya estaba marcado», sin premiar dos veces.
- Se guardan las recompensas de cada marca en la propia fila para que desmarcar
  devuelva exactamente lo que se dio, aunque las reglas cambien en el futuro.

### 3.4 `badge`

Catálogo de insignias, poblado con datos semilla.

| Campo | Tipo | Nulo | Clave | Descripción |
|---|---|---|---|---|
| `id` | TEXT | No | PK | Código estable, p. ej. `racha_7`. |
| `nombre` | TEXT | No | | Nombre visible. |
| `descripcion` | TEXT | No | | Cómo se consigue. |
| `icono` | TEXT | No | | Emoji. |
| `tipo` | TEXT | No | | Tipo de condición (ver abajo). |
| `meta` | INTEGER | No | | Valor a alcanzar. |
| `orden` | INTEGER | No | | Orden de presentación. |

**Tipos de condición**

| `tipo` | Se compara con |
|---|---|
| `TOTAL_MARCAS` | Número total de marcas registradas |
| `RACHA` | Mejor racha conseguida en cualquier hábito |
| `MONEDAS` | Monedas acumuladas |
| `NIVEL` | Nivel actual |
| `HABITO_PROPIO` | Número de hábitos creados por el niño |

**Insignias incluidas**

| id | Icono | Nombre | Condición |
|---|---|---|---|
| `primer_paso` | 🌟 | Primer paso | 1 marca |
| `constante_10` | ✅ | Diez marcas | 10 marcas |
| `racha_3` | 🔥 | Tres seguidos | racha de 3 |
| `racha_7` | 🏆 | Semana heroica | racha de 7 |
| `cofre_100` | 💰 | Cofre lleno | 100 monedas |
| `nivel_5` | 🚀 | Nivel 5 | nivel 5 |
| `creador` | 🎨 | Inventor de hábitos | 1 hábito propio |

### 3.5 `user_badge`

| Campo | Tipo | Nulo | Clave | Descripción |
|---|---|---|---|---|
| `badgeId` | TEXT | No | PK y FK → `badge.id` (ON DELETE CASCADE) | |
| `fechaObtencion` | INTEGER | No | | Día en que se consiguió. |
| `vista` | INTEGER | No | | `1` cuando el niño ya la vio en la pantalla de premios. |

**Reglas**

- `badgeId` como clave primaria hace imposible conseguir dos veces la misma
  insignia.
- **Las insignias no se retiran nunca.** Si al desmarcar una actividad las
  estadísticas bajan por debajo de la meta, la insignia se conserva. Es una
  decisión de producto: en HabitHero no hay castigos.

---

## 4. Reglas de negocio que dependen de los datos

| Regla | Dónde vive |
|---|---|
| Una marca por hábito y día | Índice único + `OnConflictStrategy.IGNORE` |
| Racha actual | `domain/CalculadoraRachas.kt` |
| Mejor racha | `domain/CalculadoraRachas.kt` |
| Monedas y experiencia por marca | `domain/CalculadoraRecompensas.kt` |
| Nivel a partir de la experiencia | `domain/CalculadoraRecompensas.kt` |
| Concesión de insignias | `domain/EvaluadorInsignias.kt` |

**Cálculo de rachas.** Solo cuentan los días programados del hábito: si es de
lunes a viernes, no marcarlo un domingo no rompe nada. Además, si hoy toca y
todavía no se ha marcado, el día no cuenta como fallo, porque la jornada aún no
ha terminado.

**Recompensas.** Base: 5 monedas y 10 de experiencia. Con la racha resultante
en 3 o más: +5 de cada. Con 7 o más: +10 de cada. El nivel es
`experiencia / 100 + 1`.

---

## 5. Creación, semilla y migraciones

- La base se crea la primera vez que se abre la app.
- En `RoomDatabase.Callback.onCreate` se ejecuta `DatabaseSeeder.sembrar()`, que
  inserta el perfil, los seis hábitos predeterminados, el catálogo de insignias
  y unas marcas de ejemplo de días anteriores, para que la app tenga contenido
  visible desde el primer arranque.
- «Reiniciar todos los datos» (Ajustes) llama a `clearAllTables()` y vuelve a
  sembrar: la app queda como recién instalada.
- Estrategia de migración en la versión 1: `fallbackToDestructiveMigration()`.
  Con una sola versión publicada no hay nada que migrar, y así un cambio de
  esquema no deja la app inutilizable.
- `exportSchema = true` deja el esquema real en `app/schemas/1.json` tras
  compilar. Ese archivo es el punto de partida para escribir `Migration(1, 2)`
  cuando exista una versión 2.

---

## 6. Consultas de ejemplo

```sql
-- Hábitos que tocan hoy (lunes = 1)
SELECT * FROM habit
WHERE activo = 1 AND (',' || diasSemana || ',') LIKE '%,1,%'
ORDER BY orden;

-- ¿Está marcado el hábito 3 hoy?
SELECT COUNT(*) FROM habit_completion
WHERE habitId = 3 AND fecha = CAST(julianday('now') - 2440587.5 AS INTEGER);

-- Marcas de los últimos 7 días, por día
SELECT fecha, COUNT(*) AS marcas
FROM habit_completion
WHERE fecha > CAST(julianday('now', '-7 days') - 2440587.5 AS INTEGER)
GROUP BY fecha
ORDER BY fecha;

-- Insignias conseguidas
SELECT b.nombre, b.icono, ub.fechaObtencion
FROM user_badge ub
JOIN badge b ON b.id = ub.badgeId
ORDER BY ub.fechaObtencion;
```
