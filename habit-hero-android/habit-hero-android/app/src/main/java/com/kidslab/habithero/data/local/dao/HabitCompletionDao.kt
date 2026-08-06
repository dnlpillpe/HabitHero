package com.kidslab.habithero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kidslab.habithero.data.local.entity.HabitCompletion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitCompletionDao {

    @Query("SELECT * FROM habit_completion ORDER BY fecha ASC")
    fun observarTodas(): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completion WHERE fecha = :fecha")
    fun observarPorFecha(fecha: LocalDate): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completion WHERE habitId = :habitId ORDER BY fecha ASC")
    suspend fun porHabito(habitId: Long): List<HabitCompletion>

    @Query("SELECT COUNT(*) FROM habit_completion WHERE habitId = :habitId AND fecha = :fecha")
    suspend fun contarEnFecha(habitId: Long, fecha: LocalDate): Int

    @Query("SELECT COUNT(*) FROM habit_completion")
    suspend fun contarTotal(): Int

    /**
     * IGNORE hace que un segundo intento en el mismo dia no cree una fila nueva:
     * devuelve -1 gracias al indice unico (habitId, fecha).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(marca: HabitCompletion): Long

    @Query("DELETE FROM habit_completion WHERE habitId = :habitId AND fecha = :fecha")
    suspend fun borrar(habitId: Long, fecha: LocalDate): Int
}
