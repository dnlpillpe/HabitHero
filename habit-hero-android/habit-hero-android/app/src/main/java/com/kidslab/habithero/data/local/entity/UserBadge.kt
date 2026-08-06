package com.kidslab.habithero.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

/** Insignia ya conseguida por el heroe. */
@Entity(
    tableName = "user_badge",
    foreignKeys = [
        ForeignKey(
            entity = Badge::class,
            parentColumns = ["id"],
            childColumns = ["badgeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserBadge(
    @PrimaryKey val badgeId: String,
    val fechaObtencion: LocalDate,
    val vista: Boolean = false
)
