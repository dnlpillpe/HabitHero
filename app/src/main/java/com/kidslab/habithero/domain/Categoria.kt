package com.kidslab.habithero.domain

/** Categorías fijas para agrupar hábitos. Se guarda [name] como texto en [Habit.categoria]. */
enum class Categoria(val etiqueta: String, val icono: String) {
    SALUD("Salud", "❤️"),
    ESTUDIO("Estudio", "📖"),
    HOGAR("Hogar", "🏠"),
    EJERCICIO("Ejercicio", "🏃"),
    OTROS("Otros", "⭐");

    companion object {
        fun desde(valor: String): Categoria =
            entries.firstOrNull { it.name == valor } ?: OTROS
    }
}
