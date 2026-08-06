package com.kidslab.habithero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Un habito que el heroe quiere repetir. [diasSemana] usa la numeracion ISO-8601
 * (1 = lunes ... 7 = domingo) y se guarda como texto separado por comas.
 */
@Entity(tableName = "habit")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val nombre: String,
    val icono: String,
    val diasSemana: List<Int>,
    val colorIndex: Int = 0,
    val esPredeterminado: Boolean = false,
    val activo: Boolean = true,
    val orden: Int = 0,
    val fechaCreacion: LocalDate = LocalDate.now()
) {
    fun tocaHoy(fecha: LocalDate): Boolean = diasSemana.contains(fecha.dayOfWeek.value)

    companion object {
        const val MAX_NOMBRE = 30
    }
}
