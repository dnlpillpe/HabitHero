package com.kidslab.habithero.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kidslab.habithero.data.local.entity.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habit WHERE activo = 1 ORDER BY orden ASC, id ASC")
    fun observarActivos(): Flow<List<Habit>>

    @Query("SELECT * FROM habit ORDER BY orden ASC, id ASC")
    fun observarTodos(): Flow<List<Habit>>

    @Query("SELECT * FROM habit ORDER BY orden ASC, id ASC")
    suspend fun obtenerTodosUnaVez(): List<Habit>

    @Query("SELECT * FROM habit WHERE id = :id LIMIT 1")
    suspend fun obtener(id: Long): Habit?

    @Query("SELECT COUNT(*) FROM habit WHERE esPredeterminado = 0")
    suspend fun contarPersonalizados(): Int

    @Query("SELECT IFNULL(MAX(orden), 0) FROM habit")
    suspend fun ordenMaximo(): Int

    /** Habitos activos con recordatorio, para reprogramar las alarmas tras un reinicio. */
    @Query("SELECT * FROM habit WHERE activo = 1 AND horaRecordatorioMinutos IS NOT NULL")
    suspend fun conRecordatorio(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(habito: Habit): Long

    @Update
    suspend fun actualizar(habito: Habit)

    @Delete
    suspend fun eliminar(habito: Habit)
}
