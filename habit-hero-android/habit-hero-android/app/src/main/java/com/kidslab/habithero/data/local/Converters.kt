package com.kidslab.habithero.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

/**
 * Convertidores de tipos para Room.
 *  - LocalDate se guarda como INTEGER (dias desde 1970-01-01).
 *  - List<Int> se guarda como TEXT separado por comas.
 */
class Converters {

    @TypeConverter
    fun fechaDesdeEpoch(valor: Long?): LocalDate? = valor?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun fechaAEpoch(fecha: LocalDate?): Long? = fecha?.toEpochDay()

    @TypeConverter
    fun diasDesdeTexto(valor: String?): List<Int> =
        valor?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.filter { it in 1..7 }
            ?: emptyList()

    @TypeConverter
    fun diasATexto(dias: List<Int>?): String = dias.orEmpty().sorted().joinToString(",")
}
