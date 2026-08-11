package com.kidslab.habithero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Perfil unico del heroe. Siempre existe una sola fila con id = 1.
 *
 * [marcoSeleccionado] es el id de un [com.kidslab.habithero.util.TiendaCatalogo.ItemTienda]
 * de tipo MARCO ya comprado (ver [UserUnlock]); null significa que no hay marco equipado.
 */
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = ID_UNICO,
    val nombre: String = "Heroe",
    val avatar: String = "🦸",
    val monedas: Int = 0,
    val experiencia: Int = 0,
    val nivel: Int = 1,
    val fechaCreacion: LocalDate = LocalDate.now(),
    val onboardingCompletado: Boolean = false,
    val marcoSeleccionado: String? = null
) {
    companion object {
        const val ID_UNICO = 1
    }
}
