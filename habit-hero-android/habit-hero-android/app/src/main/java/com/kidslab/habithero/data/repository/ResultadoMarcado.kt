package com.kidslab.habithero.data.repository

import com.kidslab.habithero.data.local.entity.Badge

/** Lo que ocurre justo después de marcar un hábito, para poder celebrarlo en pantalla. */
sealed interface ResultadoMarcado {

    data class Exito(
        val nombreHabito: String,
        val monedasGanadas: Int,
        val experienciaGanada: Int,
        val rachaActual: Int,
        val subioDeNivel: Boolean,
        val nivelNuevo: Int,
        val insigniasNuevas: List<Badge>,
        val mensaje: String
    ) : ResultadoMarcado

    /** El hábito ya estaba marcado hoy: no se duplica ni se vuelve a premiar. */
    data object YaEstabaMarcado : ResultadoMarcado

    data object HabitoNoEncontrado : ResultadoMarcado
}
