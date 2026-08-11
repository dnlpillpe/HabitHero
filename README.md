# HabitHero 🦸

Aplicación Android nativa para que **niños de 8 a 12 años** construyan hábitos
diarios: marcar, ver la racha, ganar monedas y experiencia, subir de nivel,
conseguir insignias, cumplir un desafío sorpresa cada día y gastar sus monedas
en la tienda de avatares y marcos. Todo en español, con botones grandes,
iconos y muy poco texto.

**Funciona completamente sin conexión.** No tiene registro ni login, no
muestra publicidad, no hace compras reales, no usa mapas ni analítica y no
envía datos a ningún servidor. Los únicos permisos que pide son, de forma
opcional, los necesarios para mostrar el recordatorio local de un hábito
(notificaciones y volver a programarlas tras reiniciar el móvil); sin ellos,
la app funciona igual. Toda la información vive en el dispositivo, en una
base de datos SQLite gestionada con Room.

---

## Tecnología

| Elemento | Elección |
|---|---|
| Lenguaje | Kotlin 2.0.21 |
| Interfaz | Jetpack Compose + Material Design 3 |
| Arquitectura | MVVM sencilla (UI → ViewModel → Repositorio → Room) |
| Persistencia | Room 2.6.1 (SQLite local) |
| Build | Gradle 8.9 + AGP 8.5.2, JDK 17 |
| SDK mínimo | 24 (Android 7.0 Nougat) |
| SDK objetivo | 34 (Android 14) |
| Package ID | `com.kidslab.habithero` |

No se usa Flutter, React Native, Expo, Firebase, ni ninguna API externa.

---

## Las siete pantallas

1. **Bienvenida** — elección de avatar y nombre. Solo aparece la primera vez.
2. **Inicio** — los hábitos de hoy filtrables por categoría, el desafío
   sorpresa del día, nivel, monedas y progreso.
3. **Crear o editar hábito** — nombre, icono, color, categoría, días de la
   semana y recordatorio local opcional.
4. **Progreso** — resumen visual de los últimos siete días y rachas,
   agrupado por categoría.
5. **Tienda** — gastar las monedas ganadas en avatares y marcos nuevos.
6. **Insignias** — premios conseguidos y el camino hacia los siguientes.
7. **Ajustes** — nombre, avatar y marco del héroe, y reinicio de datos con
   confirmación.

---

## Cómo obtener el APK

### Opción A — desde GitHub Releases

1. Ve a la pestaña **Releases** de este repositorio.
2. Descarga `HabitHero-v1.0.0.apk`.
3. Instálalo en el móvil (hay que permitir orígenes desconocidos).

El manual de usuario explica el proceso paso a paso:
[`docs/MANUAL_USUARIO.md`](docs/MANUAL_USUARIO.md).

### Opción B — desde GitHub Actions

Cada ejecución del flujo **Android CI** guarda `app-debug.apk` como artefacto
descargable, sin necesidad de instalar nada en tu ordenador.

### Opción C — compilando en local

```bash
gradle wrapper --gradle-version 8.9 --distribution-type bin   # solo la 1.ª vez
./gradlew testDebugUnitTest   # pruebas
./gradlew assembleDebug       # APK en app/build/outputs/apk/debug/
```

> El APK publicado es de **depuración**, firmado con la clave de depuración de
> Android. Sirve para instalar y probar la app; no está firmado para Google Play.

---

## Automatización

| Flujo | Archivo | Qué hace |
|---|---|---|
| Android CI | `.github/workflows/android-build.yml` | Ejecuta las pruebas, compila `assembleDebug` y guarda `app-debug.apk` como artefacto. |
| Release | `.github/workflows/release.yml` | Al empujar una etiqueta `v*`, compila y publica un Release con `HabitHero-vX.Y.Z.apk`. |

Publicar la versión 1.0.0:

```bash
git tag v1.0.0
git push origin v1.0.0
```

### Sobre el Gradle Wrapper

El repositorio versiona `gradle/wrapper/gradle-wrapper.properties`, que fija
Gradle 8.9. Los scripts `gradlew`, `gradlew.bat` y el binario
`gradle-wrapper.jar` **no** están incluidos, porque son artefactos generados y el
`.jar` es un binario.

Ambos flujos de trabajo los generan automáticamente antes de compilar, así que en
GitHub Actions no hay que hacer nada. Para tenerlos también en el repositorio,
ejecuta una vez en local:

```bash
gradle wrapper --gradle-version 8.9 --distribution-type bin
git add gradle/wrapper/gradle-wrapper.jar gradlew gradlew.bat
```

El `.gitignore` ya contempla la excepción para el `.jar`.

---

## Documentación

| Documento | Contenido |
|---|---|
| [`docs/MEMORIA_DESCRIPTIVA.md`](docs/MEMORIA_DESCRIPTIVA.md) | Problema, objetivos, alcance, requisitos, arquitectura, pruebas, limitaciones, conclusiones. |
| [`docs/MANUAL_USUARIO.md`](docs/MANUAL_USUARIO.md) | Instalación y uso, escrito para familias. |
| [`docs/MANUAL_TECNICO.md`](docs/MANUAL_TECNICO.md) | Estructura del código, compilación, CI/CD, extensión. |
| [`docs/BASE_DE_DATOS.md`](docs/BASE_DE_DATOS.md) | Tablas, campos, claves, relaciones, reglas y diagrama entidad-relación. |
| [`docs/SUBIR_A_GITHUB.md`](docs/SUBIR_A_GITHUB.md) | Cómo publicar el proyecto y obtener el APK sin instalar nada. |
| [`database/schema.sql`](database/schema.sql) | Esquema SQL legible. |
| [`database/sample_data.sql`](database/sample_data.sql) | Datos semilla. |

---

## Estructura del proyecto

```
habit-hero-android/
├── app/src/main/java/com/kidslab/habithero/
│   ├── data/local/          # Entidades, DAO, convertidores, base, semilla y migración
│   ├── data/repository/     # HabitHeroRepository
│   ├── domain/              # Rachas, recompensas, insignias, categorías, tienda, desafíos
│   ├── notifications/       # Recordatorios locales (AlarmManager)
│   ├── ui/                  # Tema, navegación, componentes y 7 pantallas
│   └── util/                # Catálogos de emojis, tienda y fechas en español
├── app/src/test/            # Pruebas unitarias (JVM + Robolectric)
├── database/                # schema.sql y sample_data.sql
├── docs/                    # Documentación
└── .github/workflows/       # Android CI y Release
```

---

## Licencia

MIT. Ver [`LICENSE`](LICENSE).
