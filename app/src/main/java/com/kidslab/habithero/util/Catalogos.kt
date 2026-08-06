package com.kidslab.habithero.util

/**
 * Catálogos de emojis usados como iconos. Se guardan como texto en la base de
 * datos para que la app no dependa de recursos gráficos externos.
 */
object Catalogos {

    val AVATARES = listOf("🦸", "🦹", "🐯", "🦊", "🐼", "🐨", "🦉", "🐸", "🦄", "🤖", "🐙", "🦁")

    val ICONOS_HABITO = listOf(
        "🦷", "💧", "📚", "🧹", "🎒", "😴", "🥕", "🏃", "🎨", "🧘",
        "🎹", "🐶", "🍎", "🧼", "✏️", "⚽"
    )

    /** Paleta de tarjetas; el índice se guarda en Habit.colorIndex. */
    const val TOTAL_COLORES = 6
}
