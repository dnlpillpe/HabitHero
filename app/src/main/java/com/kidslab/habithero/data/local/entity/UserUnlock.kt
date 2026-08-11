package com.kidslab.habithero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Un item de la tienda (avatar o marco) ya comprado por el heroe.
 * [itemId] corresponde a un id de [com.kidslab.habithero.util.TiendaCatalogo].
 */
@Entity(tableName = "user_unlock")
data class UserUnlock(
    @PrimaryKey val itemId: String,
    val fechaAdquirido: LocalDate = LocalDate.now()
)
