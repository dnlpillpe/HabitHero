package com.kidslab.habithero.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.kidslab.habithero.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun observar(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun obtener(): UserProfile?

    @Upsert
    suspend fun guardar(perfil: UserProfile)

    @Query("UPDATE user_profile SET monedas = :monedas, experiencia = :experiencia, nivel = :nivel WHERE id = 1")
    suspend fun actualizarRecompensas(monedas: Int, experiencia: Int, nivel: Int)

    @Query("UPDATE user_profile SET nombre = :nombre, avatar = :avatar, onboardingCompletado = 1 WHERE id = 1")
    suspend fun completarBienvenida(nombre: String, avatar: String)

    @Query("UPDATE user_profile SET monedas = :monedas WHERE id = 1")
    suspend fun actualizarMonedas(monedas: Int)

    @Query("UPDATE user_profile SET avatar = :avatar, marcoSeleccionado = :marco WHERE id = 1")
    suspend fun actualizarAvatarYMarco(avatar: String, marco: String?)
}
