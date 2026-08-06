package com.kidslab.habithero.domain

/**
 * Monedas, experiencia y niveles. Reglas simples y siempre positivas:
 * nunca se descuentan puntos por fallar, solo se ganan por avanzar.
 */
object CalculadoraRecompensas {

    const val MONEDAS_BASE = 5
    const val EXPERIENCIA_BASE = 10
    const val EXPERIENCIA_POR_NIVEL = 100

    /** Bonus de constancia: la racha que queda tras marcar el hábito. */
    fun monedasPorMarca(rachaResultante: Int): Int = when {
        rachaResultante >= 7 -> MONEDAS_BASE + 10
        rachaResultante >= 3 -> MONEDAS_BASE + 5
        else -> MONEDAS_BASE
    }

    fun experienciaPorMarca(rachaResultante: Int): Int = when {
        rachaResultante >= 7 -> EXPERIENCIA_BASE + 10
        rachaResultante >= 3 -> EXPERIENCIA_BASE + 5
        else -> EXPERIENCIA_BASE
    }

    fun nivelPara(experiencia: Int): Int =
        (experiencia.coerceAtLeast(0) / EXPERIENCIA_POR_NIVEL) + 1

    /** Experiencia acumulada dentro del nivel actual (0..99). */
    fun experienciaEnNivel(experiencia: Int): Int =
        experiencia.coerceAtLeast(0) % EXPERIENCIA_POR_NIVEL

    /** Progreso 0f..1f hacia el siguiente nivel. */
    fun progresoNivel(experiencia: Int): Float =
        experienciaEnNivel(experiencia).toFloat() / EXPERIENCIA_POR_NIVEL
}
