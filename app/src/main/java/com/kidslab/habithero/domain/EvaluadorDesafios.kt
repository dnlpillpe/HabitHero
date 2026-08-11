package com.kidslab.habithero.domain

import com.kidslab.habithero.data.local.entity.DesafioDiario

/** Decide si el desafio sorpresa del dia ya se cumplio. */
object EvaluadorDesafios {

    fun cumplido(
        desafio: DesafioDiario,
        marcasHoy: Int,
        totalHabitosHoy: Int,
        minutoActual: Int
    ): Boolean = when (desafio.tipo) {
        GeneradorDesafios.TIPO_TRES_HABITOS -> marcasHoy >= desafio.meta
        GeneradorDesafios.TIPO_TODOS_HOY -> totalHabitosHoy > 0 && marcasHoy >= totalHabitosHoy
        GeneradorDesafios.TIPO_ANTES_DE_HORA -> marcasHoy > 0 && minutoActual <= desafio.meta
        else -> false
    }
}
