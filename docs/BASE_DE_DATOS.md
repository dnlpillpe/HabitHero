# Base de datos de HabitHero

Motor: **SQLite**, gestionado con **Room 2.6.1**.
Archivo en el dispositivo: `habithero.db` (carpeta privada de la app).
Versión del esquema: **2**.

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
        text    marcoSeleccionado "FK logica a user_unlock, nullable"
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
        int     horaRecordatorioMinutos "0..1439, nullable"
        text    categoria "SALUD/ESTUDIO/HOGAR/EJERCICIO/OTROS"
    }

    USER_UNLOCK {
        text    itemId PK "id del catalogo de la tienda"
        int     fechaAdquirido "epoch day"
    }

    DAILY_CHALLENGE {
        int     fecha PK "epoch day"
        text    tipo "TRES_HABITOS/TODOS_HOY/ANTES_DE_HORA"
        int     meta
        int     completado "0/1"
        int     recompensaMonedas
        int     recompensaExperiencia
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
    USER_PROFILE ||..o{ USER_UNLOCK : "compra items de la tienda"
    USER_PROFILE ||..o{ DAILY_CHALLENGE : "cumple el desafío del día"
```

> Las relaciones con `USER_PROFILE` son lógicas, no claves foráneas: como solo
> existe un perfil (`id = 1`), guardar un `userId` en cada tabla sería una
> columna con el mismo valor en todas las filas. Lo mismo aplica a la relación
> entre `USER_PROFILE.marcoSeleccionado` y `USER_UNLOCK.itemId`.

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
| `marcoSeleccionado` | TEXT | Sí | | Id de un item de tipo MARCO ya comprado (tabla `user_unlock`); `NULL` = sin marco equipado. |

**Reglas**

- `nivel` se recalcula en cada cambio de `experiencia`; no se edita a mano.
- Al desmarcar un hábito se restan las recompensas que dio esa marca concreta,
  con suelo en 0.
- `marcoSeleccionado` solo puede apuntar a un item ya presente en `user_unlock`;
  la app nunca deja elegir un marco no comprado.

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
| `horaRecordatorioMinutos` | INTEGER | Sí | | Minuto del día (0..1439) del recordatorio local; `NULL` = sin recordatorio. |
| `categoria` | TEXT | No | | Nombre de una constante de `domain/Categoria.kt`: `SALUD`, `ESTUDIO`, `HOGAR`, `EJERCICIO` u `OTROS`. Por defecto `OTROS`. |

**Reglas**

- Un hábito solo aparece en Inicio si `activo = 1` **y** hoy está en `diasSemana`.
- Borrar un hábito borra sus marcas en cascada, pero no toca monedas ni insignias.
- Si `horaRecordatorioMinutos` no es `NULL`, se programa una alarma diaria
  (`notifications/RecordatorioScheduler.kt`) que solo notifica si, a esa hora,
  el hábito todavía toca hoy y sigue sin marcar.
- `categoria` es puramente descriptiva: se usa para filtrar y agrupar en la
  interfaz, no cambia recompensas ni rachas.

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

### 3.6 `user_unlock`

Items de la tienda (avatares o marcos) que el héroe ya compró con sus monedas.
**El catálogo en sí no vive en la base de datos**: nombre, precio y emoji de
cada item están hardcodeados en `util/TiendaCatalogo.kt`, igual que los
avatares e iconos gratuitos de `util/Catalogos.kt`. Esta tabla solo guarda el
hecho de la compra, que sí es un dato real del héroe.

| Campo | Tipo | Nulo | Clave | Descripción |
|---|---|---|---|---|
| `itemId` | TEXT | No | PK | Id del item en `TiendaCatalogo` (p. ej. `avatar_dragon`, `marco_fuego`). |
| `fechaAdquirido` | INTEGER | No | | Día de la compra. |

**Reglas**

- `itemId` como clave primaria impide comprar dos veces el mismo item: la
  inserción usa `OnConflictStrategy.IGNORE`.
- Comprar descuenta el precio de `user_profile.monedas`, sin bajar de 0
  (`domain/EvaluadorTienda.kt`).

### 3.7 `daily_challenge`

El desafío sorpresa del día. Se genera de forma determinista la primera vez
que se consulta esa fecha (misma fecha ⇒ mismo desafío, aunque se regenere) y
se guarda para que `completado` no se pueda "reiniciar" cerrando y abriendo la
app el mismo día.

| Campo | Tipo | Nulo | Clave | Descripción |
|---|---|---|---|---|
| `fecha` | INTEGER | No | PK | Día al que pertenece el desafío. |
| `tipo` | TEXT | No | | `TRES_HABITOS`, `TODOS_HOY` o `ANTES_DE_HORA`. |
| `meta` | INTEGER | No | | Umbral a superar (número de hábitos o minuto límite, según `tipo`). |
| `completado` | INTEGER | No | | `1` en cuanto se cumple; ya no se vuelve a evaluar. |
| `recompensaMonedas` | INTEGER | No | | Bonus que se otorga al cumplirse. |
| `recompensaExperiencia` | INTEGER | No | | Ídem. |

**Reglas**

- Elegido por `domain/GeneradorDesafios.kt`, con una semilla basada en
  `fecha.toEpochDay()`.
- Evaluado por `domain/EvaluadorDesafios.kt` después de cada marca de hábito,
  dentro de `HabitHeroRepository.marcarHabito()`.
- La recompensa se otorga una única vez, igual que las insignias.

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
| Categorías de hábito | `domain/Categoria.kt` |
| Recordatorios locales | `notifications/RecordatorioScheduler.kt` |
| Compra en la tienda | `domain/EvaluadorTienda.kt` |
| Desafío diario | `domain/GeneradorDesafios.kt`, `domain/EvaluadorDesafios.kt` |

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
  visible desde el primer arranque. Una instalación nueva se crea directamente
  con el esquema v2: nunca pasa por la migración.
- «Reiniciar todos los datos» (Ajustes) llama a `clearAllTables()` y vuelve a
  sembrar: la app queda como recién instalada.
- **Migración 1 → 2** (`data/local/Migraciones.kt`, `MIGRACION_1_2`): añade
  `horaRecordatorioMinutos` y `categoria` a `habit`, `marcoSeleccionado` a
  `user_profile`, y crea las tablas `user_unlock` y `daily_challenge`. Son
  todo `ALTER TABLE ADD COLUMN` con valores por defecto y `CREATE TABLE`, así
  que no hace falta reconstruir ninguna tabla existente ni se pierde ningún
  dato real de un héroe que ya tuviera la app instalada. Reemplaza a la
  estrategia de la versión 1 (`fallbackToDestructiveMigration()`), que ya no
  se usa porque borraría los datos de quien actualice la app.
- `exportSchema = true` deja el esquema real en `app/schemas/2.json` tras
  compilar. Como este proyecto nunca llegó a compilarse en versión 1, no
  existe un `1.json` exportado; la prueba de la migración
  (`app/src/test/.../data/local/MigracionTest.kt`) por eso construye a mano
  una base con el esquema v1 (tomado de este mismo `schema.sql`) en vez de
  usar `MigrationTestHelper` con esquemas exportados. Ver
  `docs/MANUAL_TECNICO.md` para el procedimiento a seguir en la próxima
  migración, cuando sí exista un `2.json` real.

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
