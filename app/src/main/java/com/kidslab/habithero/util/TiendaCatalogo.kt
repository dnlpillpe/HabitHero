package com.kidslab.habithero.util

/**
 * Catálogo fijo de la tienda de recompensas. Es contenido de la app, no dato de
 * usuario, así que se hardcodea aquí en vez de guardarse en Room (igual que
 * [Catalogos]). Lo que sí es dato real del héroe es qué items ha comprado
 * ([com.kidslab.habithero.data.local.entity.UserUnlock]) y cuál tiene equipado
 * ([com.kidslab.habithero.data.local.entity.UserProfile.marcoSeleccionado]).
 */
object TiendaCatalogo {

    enum class TipoItem { AVATAR, MARCO }

    /**
     * [emoji] es el avatar en sí para [TipoItem.AVATAR], o el glifo decorativo
     * para [TipoItem.MARCO]. [colorIndex] solo se usa en marcos, para pintar el
     * borde con [com.kidslab.habithero.ui.theme.colorHabito].
     */
    data class ItemTienda(
        val id: String,
        val tipo: TipoItem,
        val emoji: String,
        val nombre: String,
        val precio: Int,
        val colorIndex: Int = 0
    )

    // Emoji distintos a los ya disponibles gratis en Catalogos.AVATARES.
    val AVATARES_EXTRA = listOf(
        ItemTienda("avatar_dragon", TipoItem.AVATAR, "🐲", "Dragón", 40),
        ItemTienda("avatar_dino", TipoItem.AVATAR, "🦖", "Dinosaurio", 40),
        ItemTienda("avatar_lobo", TipoItem.AVATAR, "🐺", "Lobo", 60),
        ItemTienda("avatar_aguila", TipoItem.AVATAR, "🦅", "Águila", 60),
        ItemTienda("avatar_ballena", TipoItem.AVATAR, "🐳", "Ballena", 80),
        ItemTienda("avatar_mariposa", TipoItem.AVATAR, "🦋", "Mariposa", 80)
    )

    val MARCOS = listOf(
        ItemTienda("marco_azul", TipoItem.MARCO, "🔷", "Marco azul", 30, colorIndex = 0),
        ItemTienda("marco_esmeralda", TipoItem.MARCO, "💚", "Marco esmeralda", 30, colorIndex = 1),
        ItemTienda("marco_fuego", TipoItem.MARCO, "🔥", "Marco de fuego", 50, colorIndex = 2),
        ItemTienda("marco_estrellas", TipoItem.MARCO, "✨", "Marco estrellado", 70, colorIndex = 3)
    )

    val TODOS: List<ItemTienda> = AVATARES_EXTRA + MARCOS

    fun obtener(id: String): ItemTienda? = TODOS.firstOrNull { it.id == id }
}
