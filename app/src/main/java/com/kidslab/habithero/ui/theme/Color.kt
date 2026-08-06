package com.kidslab.habithero.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta HabitHero: cómic de superhéroes, alto contraste, nada apagado.
val AzulHeroe = Color(0xFF3A5BD9)
val AzulProfundo = Color(0xFF1E2A6B)
val AzulSuave = Color(0xFFDCE4FF)

val OroMedalla = Color(0xFFFFB020)
val OroSuave = Color(0xFFFFF0D2)

val RosaEnergia = Color(0xFFFF5C8A)
val RosaSuave = Color(0xFFFFE1EA)

val MentaLogro = Color(0xFF2FBF71)
val MentaSuave = Color(0xFFD9F7E6)

val Fondo = Color(0xFFEEF2FF)
val Superficie = Color(0xFFFFFFFF)
val Tinta = Color(0xFF16204A)
val TintaSuave = Color(0xFF5B668F)
val Borde = Color(0xFFD5DDF7)

/** Colores de tarjeta de hábito; el índice se guarda en Habit.colorIndex. */
val ColoresHabito = listOf(
    Color(0xFF3A5BD9),
    Color(0xFF2FBF71),
    Color(0xFFFF8A3D),
    Color(0xFFA855F7),
    Color(0xFFFF5C8A),
    Color(0xFF16A6C9)
)

fun colorHabito(indice: Int): Color = ColoresHabito[Math.floorMod(indice, ColoresHabito.size)]

val Color_Error = Color(0xFFD64550)
