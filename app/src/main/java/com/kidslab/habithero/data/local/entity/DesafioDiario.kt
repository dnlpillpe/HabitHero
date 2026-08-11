package com.kidslab.habithero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Desafio sorpresa del dia, generado por
 * [com.kidslab.habithero.domain.GeneradorDesafios] y evaluado por
 * [com.kidslab.habithero.domain.EvaluadorDesafios]. Se guarda para que
 * [completado] no se pueda "reiniciar" cerrando y abriendo la app el mismo dia.
 */
@Entity(tableName = "daily_challenge")
data class DesafioDiario(
    @PrimaryKey val fecha: LocalDate,
    val tipo: String,
    val meta: Int,
    val completado: Boolean = false,
    val recompensaMonedas: Int,
    val recompensaExperiencia: Int
)
