# Manual de usuario — HabitHero 🦸

Esta guía está escrita para familias. Explica cómo instalar HabitHero y cómo
usarla día a día.

HabitHero **funciona sin internet**. No hay que crear ninguna cuenta, no
muestra publicidad, no hace compras reales y no envía información a ningún
sitio. Todo se guarda únicamente en el móvil o la tableta donde se instala.
Los únicos permisos que puede llegar a pedir son, de forma **totalmente
opcional**, los necesarios para avisar con una notificación cuando se activa
un recordatorio de algún hábito; si no se conceden, la app funciona igual,
solo que sin ese aviso.

---

## 1. Qué necesitas

- Un móvil o tableta con **Android 7.0 o superior**.
- Unos 15 MB libres.
- Nada más: ni cuenta, ni conexión permanente, ni tarjeta.

---

## 2. Instalación desde GitHub Releases

1. Abre el navegador del móvil y entra en el repositorio del proyecto.
2. Pulsa en **Releases**, en la columna de la derecha (o en el menú, si estás en
   la versión móvil de GitHub).
3. Abre la versión más reciente.
4. En la sección **Assets**, pulsa sobre el archivo **`.apk`** para
   descargarlo.
5. Cuando termine la descarga, ábrelo desde la notificación o desde la carpeta
   *Descargas*.
6. Android preguntará si permites instalar aplicaciones de esta procedencia.
   Pulsa **Ajustes** y activa **Permitir de esta fuente**. Es el aviso normal
   para cualquier app que no venga de Google Play.
7. Vuelve atrás y pulsa **Instalar**.
8. Cuando acabe, pulsa **Abrir**.

> **Sobre el aviso de seguridad.** El APK está firmado con la clave de
> depuración de Android, que es la habitual en proyectos de código abierto no
> publicados en Google Play. Por eso el sistema pide confirmación explícita. El
> código fuente completo está en el repositorio y puede revisarse.

### Alternativa: descargar desde GitHub Actions

Si no hay ningún Release publicado todavía, entra en la pestaña **Actions**,
abre la ejecución más reciente del flujo **Android CI** y descarga el artefacto
llamado `app-debug`. Dentro está el mismo APK con el nombre `app-debug.apk`.

---

## 3. Primer uso

Al abrir la app por primera vez aparece la pantalla de bienvenida.

1. **Elige un avatar.** Hay doce disponibles desde el principio: superhéroes,
   animales y un robot. Se pueden desbloquear más en la Tienda (ver §8). Toca
   el que más te guste; se muestra en grande arriba.
2. **Escribe tu nombre.** Es opcional y se puede cambiar después.
3. Pulsa **¡Empezar!**

Ya está. La app viene con **seis hábitos listos para usar**, cada uno con su
categoría:

| Hábito | Categoría | Días |
|---|---|---|
| 🦷 Cepillarse los dientes | ❤️ Salud | Todos |
| 💧 Beber agua | ❤️ Salud | Todos |
| 📚 Leer un rato | 📖 Estudio | De lunes a viernes |
| 🧹 Ordenar el dormitorio | 🏠 Hogar | Todos |
| 🎒 Preparar la mochila | 📖 Estudio | De lunes a viernes |
| 😴 Dormir a tiempo | ❤️ Salud | Todos |

También trae algunas marcas de días anteriores, para que la pantalla de progreso
tenga algo que enseñar desde el principio. Se pueden borrar todas desde Ajustes.

---

## 4. La pantalla Hoy

La pantalla **Hoy** muestra los hábitos que tocan hoy, con algunas novedades:

- **Filtro por categoría.** Justo debajo de la tarjeta de desafío hay chips
  como "Todas", "❤️ Salud", "📖 Estudio"... Tocar uno muestra solo esos
  hábitos; tocarlo otra vez (o tocar "Todas") quita el filtro. Es solo una
  forma de mirar la lista: no afecta a las marcas ni a las recompensas.
- **Para marcar un hábito, tócalo.** La tarjeta se rellena de color y aparece
  una celebración con las monedas y la experiencia ganadas.
- **Cada hábito se marca una sola vez al día.** Si se toca otra vez, no se
  duplica ni se dan más puntos.
- **Para quitar una marca puesta por error**, toca la tarjeta ya marcada y
  confirma. Los puntos que dio se devuelven.
- **Para editar un hábito**, mantén el dedo pulsado sobre su tarjeta.
- Si un hábito tiene un recordatorio activado, su tarjeta muestra una
  campanita 🔔 (ver §6).

En la cabecera azul se ve el avatar (con su marco, si tiene uno equipado), el
nombre, el nivel, la barra de progreso hacia el siguiente nivel y las
monedas.

### Rachas

Debajo del nombre de cada hábito aparece 🔥 y un número: los días seguidos que
se lleva cumpliendo.

Dos detalles importantes, pensados para no desanimar:

- **Los días que el hábito no toca no rompen la racha.** Si «Leer un rato» es de
  lunes a viernes, el fin de semana no cuenta.
- **Si hoy todavía no se ha marcado, la racha no se ha perdido.** El día no ha
  terminado.

---

## 5. El desafío sorpresa del día

Cada día, encima de la lista de hábitos, aparece una tarjeta con un
**desafío opcional** distinto. Puede ser, por ejemplo:

- 🎯 Marcar un cierto número de hábitos hoy.
- 🏁 Completar todos los hábitos programados para hoy.
- ⏰ Marcar alguno antes de las 20:00.

El desafío del día es siempre el mismo aunque se cierre y se vuelva a abrir la
app: no cambia hasta el día siguiente. Al cumplirlo, la tarjeta se pone verde
con un ✅ y se suman unas monedas y experiencia extra, **una sola vez**. No
hacerlo no tiene ninguna consecuencia negativa: es solo un extra para quien
quiera un pequeño reto.

---

## 6. Recordatorios (opcionales)

Al crear o editar un hábito se puede activar un **recordatorio**: una hora del
día en la que, si ese hábito todavía no se ha marcado, el móvil muestra una
notificación con su nombre.

1. En el editor del hábito, activa el interruptor **Recordatorio**.
2. Elige la hora con los botones **+** y **−** (los minutos avanzan de 5 en 5).
3. La primera vez que se activa, Android puede pedir permiso para mostrar
   notificaciones. Si se concede, el aviso llegará normalmente; si no, el
   hábito y la hora se guardan igual, solo que sin notificación (se puede
   conceder el permiso más tarde desde los Ajustes del sistema).

El recordatorio **no molesta si ya está hecho**: solo avisa si a esa hora el
hábito todavía toca y sigue sin marcar. Si se desactiva el interruptor, el
aviso se cancela.

---

## 7. Crear un hábito propio

1. En la pantalla **Hoy**, pulsa el botón rosa **Nuevo hábito**.
2. Escribe el nombre. Máximo 30 letras; debajo se ve cuántas quedan.
3. Elige un **icono** entre los dieciséis disponibles.
4. Elige un **color** entre los seis.
5. Elige los **días**: toca las letras L M X J V S D, o usa los atajos
   **Todos**, **L a V** y **Finde**.
6. Elige una **categoría**: Salud, Estudio, Hogar, Ejercicio u Otros.
7. Activa el **recordatorio** si quieres (ver §6).
8. Arriba hay una vista previa que cambia mientras eliges.
9. Pulsa **Crear hábito**.

Crear el primer hábito propio desbloquea la insignia 🎨 **Inventor de hábitos**.

**Para editar o borrar**, mantén pulsada la tarjeta del hábito en la pantalla
Hoy. El icono de la papelera, arriba a la derecha, lo borra junto con sus marcas.
Las monedas y las insignias no se pierden.

---

## 8. La tienda de recompensas

Pulsa **Tienda** en la barra inferior.

Ahí se pueden gastar las monedas ganadas marcando hábitos en:

- **Avatares nuevos** — dragón, dinosaurio, lobo, águila, ballena, mariposa...
- **Marcos** — un borde de color que rodea el avatar del héroe.

Cada item muestra su precio en monedas. El botón **Comprar** se activa solo
cuando hay monedas suficientes; una vez comprado, queda marcado con ✅ para
siempre (nunca hay que volver a pagarlo).

Los avatares y marcos comprados se eligen después desde **Ajustes** (§9): el
avatar nuevo aparece junto a los doce originales, y el marco se puede
equipar o quitar tocándolo. El avatar del héroe también gana pequeños
detalles según el nivel alcanzado (una estrella a partir de nivel 5, una
corona a partir de nivel 10): eso es automático y no cuesta monedas.

---

## 9. Ver el progreso

Pulsa **Semana** en la barra inferior.

Arriba hay tres datos: las marcas de los últimos siete días, la mejor racha
conseguida y el porcentaje de lo previsto que se ha cumplido.

Debajo, los hábitos aparecen agrupados por categoría, y cada uno tiene una
fila con siete casillas, de hace seis días a hoy:

| Símbolo | Significado |
|---|---|
| Círculo de color con ✓ | Marcado |
| Círculo vacío | Tocaba y no se marcó |
| Punto pequeño y gris | Ese día no tocaba |

Un día sin marcar no resta nada. Solo indica dónde se puede mejorar.

---

## 10. Recompensas e insignias

Pulsa **Premios** en la barra inferior.

**Monedas y experiencia.** Cada marca da 5 monedas y 10 de experiencia. Con una
racha de 3 días o más, 10 monedas y 15 de experiencia. Con 7 días o más, 15
monedas y 20 de experiencia. El desafío diario (§5) suma un extra aparte.

**Niveles.** Cada 100 puntos de experiencia se sube un nivel. La barra dorada de
la pantalla Hoy indica cuánto falta para el siguiente.

**Insignias.** Hay siete. Las conseguidas aparecen en dorado con la fecha; las
demás muestran un candado y una barra con lo que falta.

| Insignia | Cómo se consigue |
|---|---|
| 🌟 Primer paso | Marcar el primer hábito |
| ✅ Diez marcas | Completar 10 hábitos en total |
| 🔥 Tres seguidos | Racha de 3 días |
| 🏆 Semana heroica | Racha de 7 días |
| 💰 Cofre lleno | Juntar 100 monedas |
| 🚀 Nivel 5 | Llegar al nivel 5 |
| 🎨 Inventor de hábitos | Crear un hábito propio |

**Las insignias no se pierden nunca.** Una vez conseguidas, se quedan.

---

## 11. Ajustes y reinicio

Pulsa **Ajustes** en la barra inferior.

**Cambiar nombre y avatar.** Edita el nombre, elige otro avatar (incluidos los
desbloqueados en la Tienda) y pulsa **Guardar cambios**.

**Marco del avatar.** Si se ha comprado al menos un marco en la Tienda,
aparece una fila para elegirlo o quitarlo; el cambio se aplica al tocarlo, sin
necesidad de pulsar "Guardar cambios".

**Reiniciar todos los datos.** Borra hábitos, marcas, monedas, experiencia,
nivel, insignias, compras de la tienda y el desafío del día, y deja la app
como recién instalada, con los seis hábitos predeterminados otra vez.

Para evitar accidentes, la app pide **dos confirmaciones**:

1. Pulsa **Reiniciar todos los datos**.
2. Pulsa **Continuar**.
3. Pulsa **Sí, borrar todo**.

En cualquiera de los dos pasos se puede cancelar. **Esta acción no se puede
deshacer.**

---

## 12. Preguntas frecuentes

**¿Hace falta internet?**
No. La app funciona igual en modo avión.

**¿Se recogen datos del niño?**
No. No hay cuentas, ni conexión a ningún servidor. Todo se guarda en el propio
dispositivo. El único permiso posible (notificaciones, para los recordatorios)
es opcional y no envía ni recibe nada por internet.

**¿Se puede usar en varias tabletas?**
Sí, pero los datos no se comparten entre ellas. Cada dispositivo lleva su cuenta.

**¿Y si el niño marca un hábito por error?**
Toca la tarjeta marcada, confirma, y la marca desaparece y devuelve los puntos.

**¿Puede haber dos niños en el mismo dispositivo?**
En esta versión no. Hay un solo perfil por instalación.

**¿Qué pasa si se pierde un día?**
Nada malo. La racha vuelve a empezar, pero no se resta ninguna moneda ni se
pierde ninguna insignia, y la app no lo reprocha.

**¿Se pueden gastar las monedas?**
Sí, en la pantalla **Tienda**, para desbloquear avatares y marcos nuevos (§8).
No es dinero real ni se puede comprar nada fuera de la app.

**¿El recordatorio funciona sin dar el permiso de notificaciones?**
El hábito y la hora se guardan igual, pero el aviso no se mostrará hasta que
se conceda el permiso desde los Ajustes del sistema del teléfono.

**¿Qué pasa si no se cumple el desafío del día?**
Nada. Es un extra opcional; simplemente no se suma su recompensa, y al día
siguiente aparece uno nuevo.

**¿Cómo se desinstala?**
Como cualquier otra app. Al desinstalarla se borran todos sus datos.
