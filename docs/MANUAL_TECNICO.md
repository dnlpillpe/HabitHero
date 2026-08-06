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
│   │   ├── AppDatabase.kt       @Database, singleton, callback de semilla
│   │   ├── Converters.kt        LocalDate ↔ Long, List<Int> ↔ String
│   │   ├── DatabaseSeeder.kt    Datos iniciales vía SQL directo
│   │   ├── dao/                 UserProfileDao, HabitDao,
│   │   │                        HabitCompletionDao, BadgeDao
│   │   └── entity/              UserProfile, Habit, HabitCompletion,
│   │                            Badge, UserBadge
│   └── repository/
│       ├── HabitHeroRepository.kt   Única puerta de entrada a los datos
│       └── ResultadoMarcado.kt      Resultado tipado de marcar un hábito
│
├── domain/                      Reglas puras, sin dependencias de Android
│   ├── CalculadoraRachas.kt
│   ├── CalculadoraRecompensas.kt
│   ├── EvaluadorInsignias.kt
│   └── MensajesAnimo.kt
│
├── ui/
│   ├── FabricaViewModels.kt     Fábrica única de ViewModel
│   ├── RaizViewModel.kt         Decide bienvenida vs. inicio
│   ├── theme/                   Color, Type, Theme
│   ├── navigation/              Rutas, RaizHabitHero (NavHost + barra)
│   ├── components/              Componentes reutilizables
│   └── screens/
│       ├── welcome/             Pantalla 1
│       ├── home/                Pantalla 2
│       ├── habitedit/           Pantalla 3
│       ├── progress/            Pantalla 4
│       ├── badges/              Pantalla 5
│       └── settings/            Pantalla 6
│
└── util/
    ├── Catalogos.kt             Emojis de avatares e iconos
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
- `exportSchema = true` con `room.schemaLocation` deja `app/schemas/1.json` tras
  compilar. Ese archivo es el punto de partida de cualquier migración futura.

### Cómo añadir una versión 2

1. Modifica las entidades.
2. Sube `version` en `@Database`.
3. Escribe la `Migration(1, 2)` comparando `app/schemas/1.json` y `2.json`.
4. Regístrala con `.addMigrations(MIGRATION_1_2)` y **quita**
   `.fallbackToDestructiveMigration()`.
5. Añade una prueba con `MigrationTestHelper` (`androidx.room:room-testing`, ya
   está entre las dependencias de test).

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

| Función | Por dónde empezar |
|---|---|
| Recordatorios locales | `AlarmManager` + `NotificationCompat`. Requiere el permiso `POST_NOTIFICATIONS` en API 33+, lo que rompería la promesa de «cero permisos»: conviene hacerlo opcional. |
| Varios perfiles | Añadir `userId` como clave foránea en `habit` y `user_badge`, y un selector de perfil. Implica una migración. |
| Tienda de recompensas | Nueva tabla `reward` y una pantalla para canjear monedas. |
| Modo oscuro | Añadir un `darkColorScheme` en `Theme.kt` y elegirlo con `isSystemInDarkTheme()`. |
| Vista mensual | Ampliar `ProgresoViewModel`; la consulta por rango de `fecha` ya está indexada. |
| Pruebas de interfaz | `androidx.compose.ui:ui-test-junit4` con `createAndroidComposeRule`. |
