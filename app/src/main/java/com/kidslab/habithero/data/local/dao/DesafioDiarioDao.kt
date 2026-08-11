package com.kidslab.habithero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kidslab.habithero.data.local.entity.DesafioDiario
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DesafioDiarioDao {

    @Query("SELECT * FROM daily_challenge WHERE fecha = :fecha LIMIT 1")
    fun observar(fecha: LocalDate): Flow<DesafioDiario?>

    @Query("SELECT * FROM daily_challenge WHERE fecha = :fecha LIMIT 1")
    suspend fun obtener(fecha: LocalDate): DesafioDiario?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(desafio: DesafioDiario): Long

    @Update
    suspend fun actualizar(desafio: DesafioDiario)
}
