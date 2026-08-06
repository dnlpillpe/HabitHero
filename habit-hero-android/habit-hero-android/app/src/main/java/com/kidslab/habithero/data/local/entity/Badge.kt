package com.kidslab.habithero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Catalogo de insignias. Se llena con datos semilla al crear la base. */
@Entity(tableName = "badge")
data class Badge(
    @PrimaryKey val id: String,
    val nombre: String,
    val descripcion: String,
    val icono: String,
    val tipo: String,
    val meta: Int,
    val orden: Int = 0
) {
    companion object {
        const val TIPO_TOTAL_MARCAS = "TOTAL_MARCAS"
        const val TIPO_RACHA = "RACHA"
        const val TIPO_MONEDAS = "MONEDAS"
        const val TIPO_NIVEL = "NIVEL"
        const val TIPO_HABITO_PROPIO = "HABITO_PROPIO"
    }
}
