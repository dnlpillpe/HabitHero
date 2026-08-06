package com.kidslab.habithero.domain

import java.time.LocalDate

/**
 * Cálculo de rachas. Solo cuentan los días en los que el hábito estaba programado:
 * si un hábito es de lunes a viernes, no marcarlo un domingo no rompe nada.
 */
object CalculadoraRachas {

    private const val LIMITE_BUSQUEDA = 730 // dos años hacia atrás, tope de seguridad

    /**
     * Racha actual en días programados consecutivos.
     * Si hoy toca y todavía no se marcó, el día no cuenta como fallo:
     * la jornada aún no ha terminado.
     */
    fun rachaActual(fechas: Set<LocalDate>, dias: Set<Int>, hoy: LocalDate): Int {
        if (dias.isEmpty() || fechas.isEmpty()) return 0

        var cursor = hoy
        if (dias.contains(cursor.dayOfWeek.value) && !fechas.contains(cursor)) {
            cursor = cursor.minusDays(1)
        }

        var racha = 0
        var pasos = 0
        while (pasos < LIMITE_BUSQUEDA) {
            pasos++
            if (dias.contains(cursor.dayOfWeek.value)) {
                if (fechas.contains(cursor)) racha++ else break
            }
            cursor = cursor.minusDays(1)
        }
        return racha
    }

    /** Mejor racha histórica, recorriendo desde la primera hasta la última marca. */
    fun mejorRacha(fechas: Set<LocalDate>, dias: Set<Int>): Int {
        if (dias.isEmpty() || fechas.isEmpty()) return 0

        var cursor = fechas.min()
        val fin = fechas.max()
        var mejor = 0
        var actual = 0

        while (!cursor.isAfter(fin)) {
            if (dias.contains(cursor.dayOfWeek.value)) {
                if (fechas.contains(cursor)) {
                    actual++
                    if (actual > mejor) mejor = actual
                } else {
                    actual = 0
                }
            }
            cursor = cursor.plusDays(1)
        }
        return mejor
    }
}
