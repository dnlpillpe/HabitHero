package com.kidslab.habithero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kidslab.habithero.data.local.entity.Badge
import com.kidslab.habithero.data.local.entity.UserBadge
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {

    @Query("SELECT * FROM badge ORDER BY orden ASC, id ASC")
    fun observarCatalogo(): Flow<List<Badge>>

    @Query("SELECT * FROM badge ORDER BY orden ASC, id ASC")
    suspend fun catalogo(): List<Badge>

    @Query("SELECT * FROM user_badge")
    fun observarObtenidas(): Flow<List<UserBadge>>

    @Query("SELECT badgeId FROM user_badge")
    suspend fun idsObtenidas(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun otorgar(userBadge: UserBadge): Long

    @Query("UPDATE user_badge SET vista = 1")
    suspend fun marcarTodasVistas()
}
