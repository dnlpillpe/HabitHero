package com.kidslab.habithero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kidslab.habithero.data.local.entity.UserUnlock
import kotlinx.coroutines.flow.Flow

@Dao
interface UserUnlockDao {

    @Query("SELECT * FROM user_unlock")
    fun observarTodas(): Flow<List<UserUnlock>>

    @Query("SELECT * FROM user_unlock WHERE itemId = :itemId LIMIT 1")
    suspend fun obtener(itemId: String): UserUnlock?

    /**
     * IGNORE hace que comprar dos veces el mismo item no duplique la fila:
     * devuelve -1 si ya existia.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(unlock: UserUnlock): Long
}
