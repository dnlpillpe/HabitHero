# Manual técnico — HabitHero

Documento para quien tenga que compilar, mantener o extender el proyecto.

---

## 1. Entorno

| Elemento | Versión |
|---|---|
| JDK | 17 (Temurin) |
| Gradle | 8.9 |
| Android Gradle Plugin | 8.5.2 |
| Kotlin | 2.0.21 |
| KSP | 2.0.21-1.0.25 |
| Compose BOM | 2024.09.03 |
| Room | 2.6.1 |
| compileSdk / targetSdk | 34 |
| minSdk | 24 (Android 7.0) |

Con `minSdk 24` se usa `java.time` mediante *core library desugaring*
(`desugar_jdk_libs 2.0.4`), configurado en `app/build.gradle.kts`.

---

## 2. Estructura del código

```
app/src/main/java/com/kidslab/habithero/
├── HabitHeroApp.kt              Application: construye base y repositorio
├── MainActivity.kt              Única Activity; solo monta Compose
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt       @Database (v2), singleton, callback de semilla
│   │   ├── Converters.kt        LocalDate ↔ Long, List<Int> ↔ String
│   │   ├── Migraciones.kt       MIGRACION_1_2
│   │   ├── DatabaseSeeder.kt    Datos iniciales vía SQL directo
│   │   ├── dao/                 UserProfileDao, HabitDao, HabitCompletionDao,
│   │   │                        BadgeDao, UserUnlockDao, DesafioDiarioDao
│   │   └── entity/              UserProfile, Habit, HabitCompletion, Badge,
│   │                            UserBadge, UserUnlock, DesafioDiario
│   └── repository/
│       ├── HabitHeroRepository.kt   Única puerta de entrada a los datos
│       ├── ResultadoMarcado.kt      Resultado tipado de marcar un hábito
│       └── ResultadoCompra.kt       Resultado tipado de comprar en la tienda
│
├── domain/                      Reglas puras, sin dependencias de Android
│   ├── CalculadoraRachas.kt
│   ├── CalculadoraRecompensas.kt
│   ├── EvaluadorInsignias.kt
│   ├── MensajesAnimo.kt
│   ├── Categoria.kt             Categorías fijas de hábito
│   ├── EvaluadorTienda.kt       Reglas de compra en la tienda
│   ├── GeneradorDesafios.kt     Elige el desafío sorpresa del día
│   └── EvaluadorDesafios.kt     Evalúa si el desafío del día se cumplió
│
├── notifications/                Recordatorios locales (AlarmManager)
│   ├── RecordatorioScheduler.kt  Programa/cancela alarmas por hábito
│   ├── ReminderBroadcastReceiver.kt  Muestra la notificación si corresponde
│   ├── BootReceiver.kt           Reprograma alarmas tras reiniciar el equipo
│   └── CanalNotificaciones.kt    Crea el NotificationChannel
│
├── ui/
│   ├── FabricaViewModels.kt     Fábrica única de ViewModel
│   ├── RaizViewModel.kt         Decide bienvenida vs. inicio
│   ├── theme/                   Color, Type, Theme
│   ├── navigation/              Rutas, RaizHabitHero (NavHost + barra)
│   ├── components/              Componentes reutilizables (incluye AvatarConMarco)
│   └── screens/
│       ├── welcome/             Pantalla 1
│       ├── home/                Pantalla 2
│       ├── habitedit/           Pantalla 3
│       ├── progress/            Pantalla 4
│       ├── shop/                Pantalla 5 (tienda de recompensas)
│       ├── badges/              Pantalla 6
│       └── settings/            Pantalla 7
│
└── util/
    ├── Catalogos.kt             Emojis de avatares e iconos gratuitos
    ├── TiendaCatalogo.kt        Catálogo fijo de la tienda (avatares y marcos)
    └── FechasEs.kt              Días y meses en español, sin Locale
```

Cada pantalla es una carpeta con dos archivos: `PantallaX.kt` (Compose, sin
lógica de negocio) y `XViewModel.kt` (estado y acciones).

---

## 3. Flujo de datos

```
Compose ──(evento)──▶ ViewModel ──(suspend)──▶ Repositorio ──▶ DAO ──▶ SQLite
   ▲                                                                     │
   └──────────── StateFlow ◀── Flow ◀── Flow<List<…>> ◀───────────────────┘
```

- Los DAO devuelven `Flow` para todo lo que se observa. Room reemite
  automáticamente cuando cambian las tablas implicadas: no hace falta refrescar
  nada a mano.
- Los ViewModel combinan esos `Flow` con `combine(...)` y los exponen como
  `StateFlow` con `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), …)`.
- Las acciones son funciones `suspend` del repositorio, lanzadas desde
  `viewModelScope`.
- Las pantallas no conocen entidades de Room más allá de leerlas: nunca escriben
  directamente.

### Inyección de dependencias

No se usa ninguna librería. `HabitHeroApp` crea la base y el repositorio de forma
perezosa, y `FabricaViewModels` los obtiene desde `CreationExtras`:

```kotlin
private fun CreationExtras.repositorio(): HabitHeroRepository =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HabitHeroApp)
        .repositorio
```

Para añadir un ViewModel nuevo basta con una línea `initializer { … }` más.

---

## 4. Reglas de negocio

Todas viven en `domain/`, en objetos sin estado y sin dependencias de Android,
que es lo que las hace comprobables en la JVM.

**Rachas** (`CalculadoraRachas`)

- `rachaActual(fechas, dias, hoy)`: recorre hacia atrás desde hoy y cuenta días
  programados consecutivos con marca. Si hoy toca y no está marcado, empieza
  desde ayer, porque el día aún no ha terminado. Tope de seguridad: 730
  iteraciones.
- `mejorRacha(fechas, dias)`: recorre de la primera a la última marca contando
  solo días programados.

**Recompensas** (`CalculadoraRecompensas`)

| Racha resultante | Monedas | Experiencia |
|---|---|---|
| 1–2 | 5 | 10 |
| 3–6 | 10 | 15 |
| 7 o más | 15 | 20 |

`nivel = experiencia / 100 + 1`.

**Insignias** (`EvaluadorInsignias`). Cinco tipos de condición —`TOTAL_MARCAS`,
`RACHA`, `MONEDAS`, `NIVEL`, `HABITO_PROPIO`— se comparan con un
`EstadisticasHeroe`. Las insignias se conceden pero **nunca se retiran**.

**Categorías** (`Categoria`). Enum fijo (`SALUD`, `ESTUDIO`, `HOGAR`,
`EJERCICIO`, `OTROS`) con etiqueta e icono. Puramente descriptivo: solo se usa
para filtrar y agrupar en la interfaz, nunca cambia recompensas ni rachas.

**Tienda** (`EvaluadorTienda`). `puedeComprar(monedas, precio, yaComprado)` y
`monedasTrasComprar(...)` (con suelo en 0). El catálogo de items en sí
(`util/TiendaCatalogo.kt`) está hardcodeado, igual que `Catalogos.kt`: es
contenido fijo de la app, no dato de usuario. Lo único que persiste en Room es
la compra (`UserUnlock`) y el marco equipado (`UserProfile.marcoSeleccionado`).

**Desafíos diarios** (`GeneradorDesafios`, `EvaluadorDesafios`).
`GeneradorDesafios.generarPara(fecha)` elige una de tres plantillas
(`TRES_HABITOS`, `TODOS_HOY`, `ANTES_DE_HORA`) con una semilla determinista
basada en `fecha.toEpochDay()`, para que la misma fecha siempre produzca el
mismo desafío. `EvaluadorDesafios.cumplido(...)` decide si ya se cumplió, y se
llama desde `HabitHeroRepository.marcarHabito()` después de cada marca; la
recompensa solo se otorga una vez gracias al flag `completado` guardado en
`DesafioDiario`.

**Recordatorios locales** (`notifications/`). No es una regla de dominio pura
(usa `AlarmManager`, por eso vive en su propio paquete y no en `domain/`), pero
sigue el mismo espíritu: `RecordatorioScheduler` programa una alarma diaria
*inexacta* (`AlarmManager.setRepeating`, sin permiso de alarmas exactas) por
cada hábito con `horaRecordatorioMinutos != null`; `ReminderBroadcastReceiver`
solo notifica si, a esa hora, el hábito sigue sin marcar hoy; `BootReceiver`
reprograma todo tras un reinicio del dispositivo, porque el sistema borra las
alarmas al apagarse.

---

## 5. Base de datos

Detalle completo en [`BASE_DE_DATOS.md`](BASE_DE_DATOS.md). Puntos clave para
quien toque el código:

- La regla «una marca por hábito y día» está en el **índice único**
  `(habitId, fecha)` de `habit_completion`, no solo en la interfaz. La inserción
  usa `OnConflictStrategy.IGNORE`, así que un duplicado devuelve `-1` y el
  repositorio lo traduce a `ResultadoMarcado.YaEstabaMarcado`.
- Las fechas se guardan como `epochDay` (`INTEGER`). Comparar fechas es comparar
  enteros, y los índices funcionan sin trucos.
- La semilla se ejecuta en `RoomDatabase.Callback.onCreate` con SQL directo, no
  con los DAO: dentro de `onCreate` la base todavía no está disponible para
  Room.
- `exportSchema = true` con `room.schemaLocation` deja `app/schemas/2.json` tras
  compilar. Ese archivo es el punto de partida de cualquier migración futura
  (a partir de la 2, ya sí habrá un JSON exportado real).
- Desde la versión 2 el esquema **ya no usa** `fallbackToDestructiveMigration()`:
  cualquier cambio de esquema necesita su propia `Migration`, para no borrar
  los datos de un héroe real al actualizar la app.

### Cómo se hizo la migración 1 → 2 (y cómo hacer la siguiente)

La versión 1 de este proyecto nunca llegó a compilarse, así que no existe un
`app/schemas/1.json` exportado del que partir con el procedimiento habitual de
`MigrationTestHelper`. Por eso `MIGRACION_1_2` se escribió y se probó así:

1. Se modificaron las entidades (`Habit.horaRecordatorioMinutos`,
   `Habit.categoria`, `UserProfile.marcoSeleccionado`) y se añadieron las
   entidades nuevas (`UserUnlock`, `DesafioDiario`).
2. Se subió `version` a `2` en `@Database` y se registraron las entidades y
   DAO nuevos.
3. Se escribió `MIGRACION_1_2` (`data/local/Migraciones.kt`) a mano, con
   `ALTER TABLE ... ADD COLUMN` (todas las columnas nuevas tienen valor por
   defecto o admiten `NULL`, así que no hace falta reconstruir ninguna tabla)
   y `CREATE TABLE` para las dos tablas nuevas.
4. Se registró con `.addMigrations(MIGRACION_1_2)` y se quitó
   `.fallbackToDestructiveMigration()`.
5. Como no había `1.json`, la prueba (`MigracionTest.kt`) construye a mano una
   base con el esquema v1 exacto (copiado de `database/schema.sql` tal como
   estaba antes de este cambio), inserta filas de ejemplo, la reabre con Room
   aplicando `MIGRACION_1_2`, y comprueba que no lance excepción (Room valida
   el esquema resultante contra las entidades en cada apertura) y que los
   datos previos sigan intactos.

**Para la próxima migración (2 → 3)**, ya sí existirá `app/schemas/2.json`
tras la primera compilación en CI, así que se puede volver al procedimiento
estándar de Room: comparar `2.json` con `3.json` y usar `MigrationTestHelper`
(`androidx.room:room-testing`, ya está entre las dependencias de test) en vez
de construir el esquema anterior a mano.

---

## 6. Compilación

### En local

```bash
gradle wrapper --gradle-version 8.9 --distribution-type bin   # solo la 1.ª vez
./gradlew testDebugUnitTest    # pruebas unitarias
./gradlew assembleDebug        # APK de depuración
./gradlew clean                # limpiar
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

### Gradle Wrapper

El repositorio versiona únicamente `gradle/wrapper/gradle-wrapper.properties`,
que fija Gradle 8.9. Los scripts `gradlew` y `gradlew.bat` y el binario
`gradle-wrapper.jar` son artefactos generados y no están incluidos. Ambos flujos
de CI los crean antes de compilar:

```bash
gradle wrapper --gradle-version 8.9 --distribution-type bin
```

Para trabajar en local ejecuta ese mismo comando una vez (necesitas Gradle
instalado, o Android Studio, que trae el suyo). Si prefieres tenerlos
versionados, haz commit de los tres archivos: el `.gitignore` ya contempla la
excepción con `!gradle/wrapper/gradle-wrapper.jar`.

---

## 7. Integración continua

### `.github/workflows/android-build.yml`

Se dispara en `push` a `main`, en *pull request* y a mano. Pasos: descargar
código → JDK 17 → Gradle 8.9 → generar el wrapper si falta → `testDebugUnitTest`
→ `assembleDebug` → subir `app-debug.apk` como artefacto → subir los informes de
pruebas (también si algo falla, gracias a `if: always()`).

### `.github/workflows/release.yml`

Se dispara al empujar una etiqueta `v*` o a mano indicando la versión. Repite la
compilación, renombra el APK a `HabitHero-vX.Y.Z.apk` y publica el Release con
`softprops/action-gh-release@v2`.

```bash
git tag v1.0.0
git push origin v1.0.0
```

Requiere `permissions: contents: write`, que ya está declarado.

### Diagnóstico de fallos

| Síntoma | Causa habitual |
|---|---|
| `Unsupported class file major version` | JDK distinto de 17 |
| `KSP … is not compatible with Kotlin …` | Versiones de KSP y Kotlin desalineadas; el sufijo de KSP debe coincidir con la versión de Kotlin |
| `Cannot find a Gradle wrapper` | Falló el paso que genera el wrapper |
| Robolectric tarda mucho la primera vez | Descarga los *jars* de Android; se cachea después |
| `Schema export directory is not provided` | Falta el argumento `room.schemaLocation` en el bloque `ksp` |

---

## 8. Firma para distribución

El APK publicado es de depuración. Para una versión firmada:

1. Genera un almacén de claves con `keytool`.
2. Añade un `signingConfigs { create("release") { … } }` en
   `app/build.gradle.kts`, leyendo las credenciales de variables de entorno.
3. Guarda el almacén codificado en base64 y las contraseñas como *secrets* del
   repositorio.
4. En el flujo de release, decodifica el almacén y ejecuta `assembleRelease`.

Nunca subas el almacén de claves ni las contraseñas al repositorio.

---

## 9. Ideas para extender

Recordatorios locales, categorías, tienda de recompensas, desafíos diarios y
el avatar que evoluciona con el nivel ya están implementados (ver §4 y
`docs/MEMORIA_DESCRIPTIVA.md`). Ideas razonables para seguir:

| Función | Por dónde empezar |
|---|---|
| Varios perfiles | Añadir `userId` como clave foránea en `habit`, `user_badge`, `user_unlock` y `daily_challenge`, y un selector de perfil. Implica otra migración (3). |
| Modo oscuro | Añadir un `darkColorScheme` en `Theme.kt` y elegirlo con `isSystemInDarkTheme()`, con opción manual en Ajustes. |
| Vista mensual/anual | Ampliar `ProgresoViewModel` más allá de `FechasEs.ultimos7Dias`; la consulta por rango de `fecha` ya está indexada. |
| Más desafíos y más items de tienda | Añadir plantillas a `GeneradorDesafios.PLANTILLAS` y items a `TiendaCatalogo`; ninguno de los dos requiere migración porque son contenido hardcodeado. |
| Recordatorios exactos | Migrar de `setRepeating` a `setExactAndAllowWhileIdle`, lo que en API 31+ exige pedir el permiso especial `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM` desde Ajustes del sistema. |
| Pruebas de interfaz | `androidx.compose.ui:ui-test-junit4` con `createAndroidComposeRule`. |
