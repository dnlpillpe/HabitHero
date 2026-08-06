package com.kidslab.habithero.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Marca de un habito en un dia concreto.
 *
 * El indice unico (habitId, fecha) es la garantia a nivel de base de datos de que
 * un habito solo puede marcarse una vez por dia.
 */
@Entity(
    tableName = "habit_completion",
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitId", "fecha"], unique = true),
        Index(value = ["fecha"])
    ]
)
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val habitId: Long,
    val fecha: LocalDate,
    val monedasGanadas: Int = 0,
    val experienciaGanada: Int = 0
)
