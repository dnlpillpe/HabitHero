package com.kidslab.habithero.ui.navigation

object Rutas {
    const val BIENVENIDA = "bienvenida"
    const val INICIO = "inicio"
    const val PROGRESO = "progreso"
    const val INSIGNIAS = "insignias"
    const val CONFIGURACION = "configuracion"

    const val ARG_HABITO = "habitId"
    const val EDITOR = "editor"
    const val EDITOR_PATRON = "$EDITOR?$ARG_HABITO={$ARG_HABITO}"

    fun editor(habitId: Long? = null): String =
        if (habitId == null) "$EDITOR?$ARG_HABITO=0" else "$EDITOR?$ARG_HABITO=$habitId"

    /** Rutas que muestran la barra inferior. */
    val CON_BARRA = setOf(INICIO, PROGRESO, INSIGNIAS, CONFIGURACION)
}
