# Memoria descriptiva — HabitHero

**Proyecto:** HabitHero
**Repositorio:** `habit-hero-android`
**Package ID:** `com.kidslab.habithero`
**Versión:** 1.0.0
**Plataforma:** Android nativo (Kotlin + Jetpack Compose)

---

## 1. Problema

Entre los 8 y los 12 años, los niños empiezan a asumir rutinas propias
(higiene, orden, estudio, descanso), pero todavía no tienen la noción de
continuidad que hace que un hábito se sostenga. En la práctica, la constancia la
sostiene un adulto que recuerda, insiste y a veces regaña, lo cual convierte la
rutina en un conflicto y hace que el niño no se apropie de ella.

Las herramientas habituales no encajan con esta edad:

- Las agendas y listas de tareas para adultos son densas y exigen leer y escribir
  mucho.
- Las apps de hábitos del mercado suelen requerir cuenta, conexión y publicidad,
  y con frecuencia usan mecánicas de castigo (rachas que se pierden, puntuaciones
  negativas, recordatorios que culpabilizan).
- Muchas recogen datos personales, lo cual es especialmente delicado con menores.

Falta una herramienta que el niño pueda usar solo, que le devuelva una señal
visible de su propio avance, y que no dependa de internet ni de datos personales.

## 2. Objetivos

**Objetivo general**

Construir una aplicación Android nativa que ayude a niños de 8 a 12 años a
desarrollar hábitos diarios mediante marcación visual, rachas, monedas, niveles e
insignias, funcionando por completo sin conexión.

**Objetivos específicos**

1. Permitir marcar hábitos con un solo toque, una vez por día, y deshacer una
   marca puesta por error.
2. Mostrar el progreso de forma visual: racha actual, mejor racha y los últimos
   siete días.
3. Motivar con recompensas acumulativas (monedas, experiencia, niveles) y al
   menos cinco insignias.
4. Permitir crear hábitos propios con nombre, icono, color y días de la semana.
5. Guardar toda la información en el dispositivo con Room, sin cuentas ni
   servidores.
6. Mantener un tono siempre positivo: nunca castigos, reproches ni pérdidas.
7. Automatizar pruebas, compilación y publicación del APK mediante GitHub
   Actions.

## 3. Alcance

**Incluido**

- Seis pantallas: bienvenida, inicio, crear/editar hábito, progreso, insignias y
  ajustes.
- Seis hábitos predeterminados listos para usar.
- Hábitos personalizados con nombre de hasta 30 caracteres, icono, color y días.
- Marcado y desmarcado con una marca máxima por hábito y día.
- Racha actual y mejor racha por hábito.
- Monedas, experiencia y niveles.
- Siete insignias con progreso visible.
- Resumen visual de los últimos siete días.
- Reinicio total de datos con doble confirmación.
- Datos semilla para poder probar la aplicación desde el primer arranque.
- Interfaz completa en español.

**Excluido deliberadamente**

Registro, login, servidor, API externa, Firebase, publicidad, compras dentro de
la app, chat, mapas, analítica, rastreo, notificaciones push y permisos de
internet. El `AndroidManifest.xml` no declara **ningún** permiso.

**Fuera del alcance de esta versión**

Recordatorios locales, varios perfiles en un mismo dispositivo, exportación de
datos, modo oscuro, tienda para gastar las monedas, y estadísticas de más de
siete días.

## 4. Requisitos

### 4.1 Requisitos funcionales

| ID | Requisito |
|---|---|
| RF-01 | Elegir avatar y nombre en la primera apertura. |
| RF-02 | Mostrar en Inicio solo los hábitos programados para hoy. |
| RF-03 | Marcar un hábito con un toque. |
| RF-04 | Impedir marcar el mismo hábito dos veces el mismo día. |
| RF-05 | Desmarcar una actividad marcada por error, con confirmación. |
| RF-06 | Crear un hábito con nombre (≤ 30 caracteres), icono, color y días. |
| RF-07 | Editar y borrar un hábito existente. |
| RF-08 | Calcular y mostrar la racha actual de cada hábito. |
| RF-09 | Calcular y mostrar la mejor racha de cada hábito. |
| RF-10 | Otorgar monedas y experiencia al marcar. |
| RF-11 | Subir de nivel al acumular experiencia. |
| RF-12 | Ofrecer al menos cinco insignias con condiciones claras. |
| RF-13 | Mostrar el progreso hacia las insignias no conseguidas. |
| RF-14 | Mostrar el resumen de los últimos siete días. |
| RF-15 | Editar nombre y avatar desde Ajustes. |
| RF-16 | Reiniciar todos los datos con confirmación. |
| RF-17 | Conservar la información al cerrar y volver a abrir la app. |
| RF-18 | Incluir datos iniciales para poder probar de inmediato. |

### 4.2 Requisitos no funcionales

| ID | Requisito |
|---|---|
| RNF-01 | Funcionamiento completamente offline, sin ningún permiso declarado. |
| RNF-02 | Android 7.0 (API 24) o superior. |
| RNF-03 | Interfaz íntegramente en español. |
| RNF-04 | Botones grandes, iconos y tarjetas; mínimo texto escrito. |
| RNF-05 | Ningún mensaje de castigo o culpa. |
| RNF-06 | Persistencia local real con Room, no simulada. |
| RNF-07 | Arquitectura MVVM con separación de capas. |
| RNF-08 | Compilación reproducible con Gradle Wrapper y JDK 17. |
| RNF-09 | Pruebas automáticas ejecutadas en integración continua. |
| RNF-10 | Código bajo licencia MIT. |

## 5. Pantallas

| # | Pantalla | Contenido | Salidas |
|---|---|---|---|
| 1 | **Bienvenida** | Rejilla de 12 avatares, campo de nombre opcional, botón «¡Empezar!». | Inicio |
| 2 | **Inicio** | Cabecera con avatar, nombre, nivel, barra de experiencia y monedas. Lista de hábitos de hoy en tarjetas grandes. Botón «Nuevo hábito». | Editor, Progreso, Insignias, Ajustes |
| 3 | **Crear o editar hábito** | Vista previa en vivo, nombre con contador, 16 iconos, 6 colores, selector de días con atajos, botón guardar, borrar. | Vuelve atrás |
| 4 | **Progreso** | Marcas de la semana, mejor racha, porcentaje cumplido, rejilla de 7 días por hábito. | Barra inferior |
| 5 | **Insignias** | Monedas, nivel, insignias conseguidas y barra de progreso de las pendientes. | Barra inferior |
| 6 | **Ajustes** | Resumen, edición de nombre y avatar, reinicio con doble confirmación, información de la app. | Barra inferior |

La navegación entre las cuatro pantallas principales es una barra inferior con
cuatro destinos: Hoy, Semana, Premios y Ajustes.

## 6. Arquitectura

MVVM en tres capas, con dependencias en una sola dirección:

```
┌──────────────────────────────────────────────┐
│  UI (Jetpack Compose)                        │
│  6 pantallas + componentes + tema + rutas    │
└───────────────────┬──────────────────────────┘
                    │ StateFlow / eventos
┌───────────────────▼──────────────────────────┐
│  ViewModel                                   │
│  Bienvenida, Inicio, Editor, Progreso,       │
│  Insignias, Configuración, Raíz              │
└───────────────────┬──────────────────────────┘
                    │ suspend / Flow
┌───────────────────▼──────────────────────────┐
│  HabitHeroRepository                         │
│  única puerta de entrada a los datos         │
└──────┬────────────────────────────┬──────────┘
       │                            │
┌──────▼───────────┐   ┌────────────▼──────────┐
│  domain/         │   │  Room                 │
│  reglas puras    │   │  entidades, DAO,      │
│  sin Android     │   │  convertidores, base  │
└──────────────────┘   └───────────────────────┘
```

**Decisiones y su motivo**

- **Sin librería de inyección de dependencias.** El grafo es pequeño: la clase
  `HabitHeroApp` construye la base y el repositorio, y una única
  `FabricaViewModels` los entrega. Añadir Hilt habría multiplicado el andamiaje
  sin resolver ningún problema real.
- **Reglas de negocio en objetos puros de Kotlin** (`domain/`), sin dependencias
  de Android. Es lo que permite probarlas en la JVM, rápido y sin emulador.
- **Iconos como emojis en lugar de recursos gráficos.** Son coloridos,
  reconocibles para un niño, no pesan y eliminan una fuente entera de errores de
  recursos.
- **Un único esquema de color claro**, sin seguir el tema del sistema: el color
  es parte de la identidad de la app y no debe cambiar bajo el niño.

## 7. Funcionamiento

**Primer arranque.** Room crea `habithero.db` y ejecuta la semilla: perfil, seis
hábitos, siete insignias y unas marcas de días anteriores. Como
`onboardingCompletado` es 0, se abre la pantalla de bienvenida.

**Uso diario.** Inicio filtra los hábitos cuyo `diasSemana` contiene el día de
hoy. Al tocar una tarjeta:

1. Se comprueba si ya hay una marca de hoy. Si la hay, no ocurre nada.
2. Se calcula la racha resultante y, con ella, monedas y experiencia.
3. Se inserta la marca. El índice único es la última barrera contra duplicados.
4. Se actualizan monedas, experiencia y nivel del perfil.
5. Se revisan las insignias y se conceden las nuevas.
6. Se muestra un diálogo de celebración con lo ganado.

Tocar una tarjeta ya marcada abre la confirmación para deshacer. Al deshacer se
devuelven exactamente las recompensas que dio esa marca, con suelo en 0; las
insignias ya conseguidas se conservan.

**Cambio de día.** Al volver a Inicio se recalcula la fecha actual, de modo que
la app abierta durante la medianoche se actualiza sola.

## 8. Modelo de datos

Cinco tablas: `user_profile`, `habit`, `habit_completion`, `badge` y
`user_badge`. La restricción central es el índice único
`(habitId, fecha)` sobre `habit_completion`, que traduce a nivel de base de datos
la regla «un hábito solo se marca una vez por día».

El detalle completo —campos, tipos, claves, relaciones, reglas y diagrama
entidad-relación en Mermaid— está en [`BASE_DE_DATOS.md`](BASE_DE_DATOS.md).

## 9. Pruebas

Las pruebas viven en `app/src/test/` y se ejecutan en la JVM, sin emulador. Las
que necesitan Room usan Robolectric con una base de datos en memoria: es Room de
verdad, con SQL real y las mismas restricciones.

| Área | Qué se comprueba |
|---|---|
| `CalculadoraRachasTest` | Racha vacía; tres días seguidos; hoy sin marcar no rompe la racha; un día saltado sí la corta; los días no programados no cuentan; mejor racha en todo el historial. |
| `CalculadoraRecompensasTest` | Recompensa base; bonus a partir de 3 y de 7 días; nivel cada 100 puntos; progreso siempre entre 0 y 1; experiencia negativa no rompe nada. |
| `EvaluadorInsigniasTest` | Condición de primera marca; condición de racha exacta; no reentregar insignias ya conseguidas; progreso acotado a 1. |
| `HabitHeroRepositoryTest` | Marcar guarda y premia; no se puede marcar dos veces el mismo día ni premiar dos veces; sí se puede marcar en días distintos; desmarcar devuelve puntos; los datos persisten al releer desde Room; borrar un hábito borra sus marcas en cascada; el nivel sube al acumular experiencia. |

El flujo `android-build.yml` ejecuta `testDebugUnitTest` antes de compilar, de
modo que un fallo en cualquiera de estas pruebas detiene la generación del APK.

**Criterios de aceptación manual**

- Las seis pantallas se abren y se vuelve atrás sin cierres inesperados.
- Al cerrar la app por completo y volver a abrirla, se conservan hábitos, marcas,
  monedas, nivel e insignias.
- Marcar dos veces el mismo hábito el mismo día no cambia el contador de monedas.
- «Reiniciar todos los datos» deja la app como recién instalada.

## 10. Limitaciones

1. **APK de depuración.** El artefacto que se publica está firmado con la clave
   de depuración de Android. Se instala y funciona, pero no sirve para Google
   Play; para eso haría falta una `release` firmada con un almacén de claves
   propio.
2. **Un solo perfil por dispositivo.** No hay soporte para varios hermanos en la
   misma tableta.
3. **Sin recordatorios.** No hay notificaciones; la app no avisa de nada.
4. **Sin pruebas de interfaz automatizadas.** Los tests cubren la lógica y los
   datos, no la navegación en Compose, que se verifica a mano.
5. **La ventana de progreso es de siete días.** No hay vista mensual ni anual.
6. **Las monedas no se gastan.** Son un marcador de progreso; no existe tienda.
7. **Sin exportación ni copia manual.** Los datos dependen de la copia de
   seguridad estándar de Android; si se desinstala la app, se pierden.
8. **El cálculo de mejor racha ignora las marcas de días no programados.** Si un
   hábito se edita y cambian sus días, las marcas antiguas fuera del nuevo
   calendario dejan de contar para la racha, aunque se conservan en la base.

## 11. Conclusiones

HabitHero cumple lo que se propuso: una aplicación Android nativa, en español,
completamente offline y sin permisos, que permite a un niño de 8 a 12 años
gestionar sus hábitos diarios por sí mismo y ver su avance de un vistazo.

Las decisiones que más pesaron en el resultado fueron tres. La primera, poner la
regla de «una marca por día» en el propio índice de la base de datos en lugar de
solo en la interfaz: es la única forma de que la regla se sostenga aunque la app
se toque rápido o dos veces seguidas. La segunda, mantener las reglas de negocio
en objetos puros de Kotlin, lo que permite probarlas en segundos y sin emulador.
La tercera, el compromiso de que la app nunca castigue: no hay rachas que se
«pierdan» con un aviso, no hay puntuaciones negativas, y las insignias no se
retiran jamás.

El siguiente paso natural sería una versión firmada para distribución y unos
recordatorios locales opcionales, ambos posibles sin romper el compromiso de
funcionar sin conexión.
