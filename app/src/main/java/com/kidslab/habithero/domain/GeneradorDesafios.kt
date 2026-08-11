package com.kidslab.habithero.domain

import com.kidslab.habithero.data.local.entity.DesafioDiario
import java.time.LocalDate
import kotlin.random.Random

/**
 * Elige el desafio sorpresa del dia de forma determinista: la misma fecha
 * siempre produce el mismo desafio, aunque se regenere varias veces.
 */
object GeneradorDesafios {

    const val TIPO_TRES_HABITOS = "TRES_HABITOS"
    const val TIPO_TODOS_HOY = "TODOS_HOY"
    const val TIPO_ANTES_DE_HORA = "ANTES_DE_HORA"

    /** Minuto del dia que representa las 20:00, meta de [TIPO_ANTES_DE_HORA]. */
    const val MINUTO_LIMITE_ANTES_DE_HORA = 20 * 60

    private data class Plantilla(val tipo: String, val meta: Int, val monedas: Int, val experiencia: Int)

    private val PLANTILLAS = listOf(
        Plantilla(TIPO_TRES_HABITOS, meta = 3, monedas = 15, experiencia = 20),
        Plantilla(TIPO_TODOS_HOY, meta = 0, monedas = 20, experiencia = 25),
        Plantilla(TIPO_ANTES_DE_HORA, meta = MINUTO_LIMITE_ANTES_DE_HORA, monedas = 10, experiencia = 15)
    )

    fun generarPara(fecha: LocalDate): DesafioDiario {
        val plantilla = PLANTILLAS[Random(fecha.toEpochDay()).nextInt(PLANTILLAS.size)]
        return DesafioDiario(
            fecha = fecha,
            tipo = plantilla.tipo,
            meta = plantilla.meta,
            completado = false,
            recompensaMonedas = plantilla.monedas,
            recompensaExperiencia = plantilla.experiencia
        )
    }
}
