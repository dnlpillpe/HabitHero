# Cómo subir HabitHero a GitHub y obtener el APK

Esta guía no requiere instalar **nada** en tu ordenador: se hace entera desde el
navegador. Al terminar tendrás el repositorio publicado, GitHub Actions habrá
compilado la app y podrás descargar el APK.

---

## Paso 1 — Crear el repositorio

1. Entra en <https://github.com/new>.
2. **Repository name:** `habit-hero-android`
3. Visibilidad: pública o privada, como prefieras.
4. **No** marques «Add a README file», «Add .gitignore» ni «Choose a license»:
   el proyecto ya trae los tres.
5. Pulsa **Create repository**.

---

## Paso 2 — Subir los archivos

En la página del repositorio recién creado:

1. Pulsa el enlace **uploading an existing file**
   (o entra en `https://github.com/TU-USUARIO/habit-hero-android/upload/main`).
2. Descomprime `habit-hero-android.zip` en tu ordenador.
3. **Arrastra el contenido de la carpeta** `habit-hero-android` a la zona de
   subida. Arrastra los archivos y carpetas de dentro, no la carpeta que los
   contiene.
4. En **Commit changes**, escribe un mensaje, por ejemplo:
   `HabitHero v1.0.0 - MVP completo`
5. Pulsa **Commit changes**.

> **Importante:** el navegador de GitHub **no sube carpetas ocultas** como
> `.github`. Si tras la subida no ves la carpeta `.github`, créala a mano con el
> Paso 3. Si sí aparece, salta directamente al Paso 4.

---

## Paso 3 — Crear los flujos de trabajo a mano (solo si hace falta)

Para cada uno de los dos archivos de `.github/workflows/`:

1. En el repositorio, pulsa **Add file → Create new file**.
2. En el nombre escribe `.github/workflows/android-build.yml`
   (al escribir las barras, GitHub crea las carpetas solo).
3. Abre `.github/workflows/android-build.yml` del ZIP con cualquier editor de
   texto, copia todo su contenido y pégalo.
4. Pulsa **Commit changes**.
5. Repite lo mismo con `.github/workflows/release.yml`.

---

## Paso 4 — Ver la compilación

1. Ve a la pestaña **Actions** del repositorio.
2. Si GitHub pide confirmación para habilitar los flujos, acéptala.
3. Verás la ejecución **Android CI**. Tarda unos minutos la primera vez.
4. Ábrela para seguir los pasos: pruebas unitarias y luego compilación.

**Si sale en verde:** al final de la página, en **Artifacts**, hay un archivo
`app-debug` con el APK dentro.

**Si sale en rojo:** abre el paso que falló y lee el mensaje de error. Los fallos
más habituales están recogidos en la tabla de diagnóstico de
[`MANUAL_TECNICO.md`](MANUAL_TECNICO.md#7-integración-continua). Puedes copiar el
log y pedir ayuda para corregirlo.

---

## Paso 5 — Publicar el Release con el APK

Cuando **Android CI** esté en verde:

1. Ve a la página principal del repositorio y pulsa **Releases** (columna
   derecha) → **Create a new release**.
2. En **Choose a tag**, escribe `v1.0.0` y pulsa
   **Create new tag: v1.0.0 on publish**.
3. **Release title:** `HabitHero v1.0.0`
4. Pulsa **Publish release**.

Al crear la etiqueta `v1.0.0` se dispara el flujo **Release**, que compila de
nuevo y **adjunta automáticamente** `HabitHero-v1.0.0.apk` a ese Release. Espera
unos minutos y recarga la página del Release: el APK aparecerá en **Assets**.

A partir de ahí, cualquiera puede instalarlo siguiendo el
[`MANUAL_USUARIO.md`](MANUAL_USUARIO.md).

---

## Alternativa por línea de órdenes

Si tienes Git instalado y prefieres hacerlo así:

```bash
cd habit-hero-android
git init
git add .
git commit -m "HabitHero v1.0.0 - MVP completo"
git branch -M main
git remote add origin https://github.com/TU-USUARIO/habit-hero-android.git
git push -u origin main
git tag v1.0.0
git push origin v1.0.0
```

Este método sí sube la carpeta `.github`, así que el Paso 3 no hace falta.
