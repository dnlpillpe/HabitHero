package com.kidslab.habithero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kidslab.habithero.domain.Categoria
import java.time.LocalDate

/**
 * Un habito que el heroe quiere repetir. [diasSemana] usa la numeracion ISO-8601
 * (1 = lunes ... 7 = domingo) y se guarda como texto separado por comas.
 *
 * [horaRecordatorioMinutos] es el minuto del dia (0..1439) en el que se avisa con
 * una notificacion local; null significa que el habito no tiene recordatorio.
 * [categoria] guarda el nombre de una constante de [Categoria].
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
    val fechaCreacion: LocalDate = LocalDate.now(),
    val horaRecordatorioMinutos: Int? = null,
    val categoria: String = Categoria.OTROS.name
) {
    fun tocaHoy(fecha: LocalDate): Boolean = diasSemana.contains(fecha.dayOfWeek.value)

    fun categoriaEnum(): Categoria = Categoria.desde(categoria)

    companion object {
        const val MAX_NOMBRE = 30
    }
}
