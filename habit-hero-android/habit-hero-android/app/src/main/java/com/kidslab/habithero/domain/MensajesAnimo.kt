package com.kidslab.habithero.domain

/** Mensajes positivos. En HabitHero nunca hay castigos ni reproches. */
object MensajesAnimo {

    private val ALMARCAR = listOf(
        "¡Muy bien! Sigue así 💪",
        "¡Genial! Un paso más de héroe ⭐",
        "¡Lo lograste! 🎉",
        "¡Buen trabajo! 🚀",
        "¡Increíble! Tu poder crece 🔥"
    )

    private val PANTALLA_VACIA = listOf(
        "Hoy es un buen día para empezar",
        "Elige tu primera misión del día",
        "Tu capa está lista, ¿empezamos?"
    )

    fun alMarcar(semilla: Int): String = ALMARCAR[Math.floorMod(semilla, ALMARCAR.size)]

    fun sinPendientes(): String = "¡Completaste todo lo de hoy! 🌟"

    fun animoDelDia(semilla: Int): String = PANTALLA_VACIA[Math.floorMod(semilla, PANTALLA_VACIA.size)]

    fun paraRacha(racha: Int): String = when {
        racha >= 7 -> "¡$racha días seguidos! Eres imparable 🏆"
        racha >= 3 -> "¡$racha días seguidos! 🔥"
        racha == 1 -> "¡Racha iniciada!"
        racha > 1 -> "$racha días seguidos"
        else -> "Cada día cuenta"
    }
}
