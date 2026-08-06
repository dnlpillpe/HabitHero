package com.kidslab.habithero.util

import java.time.LocalDate

/** Utilidades de fecha en español, sin depender del Locale del dispositivo. */
object FechasEs {

    /** 1 = lunes ... 7 = domingo (ISO-8601). */
    val DIAS_LARGOS = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val DIAS_CORTOS = listOf("L", "M", "X", "J", "V", "S", "D")

    val MESES = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )

    fun diaCorto(isoDayOfWeek: Int): String = DIAS_CORTOS[(isoDayOfWeek - 1).coerceIn(0, 6)]

    fun diaLargo(isoDayOfWeek: Int): String = DIAS_LARGOS[(isoDayOfWeek - 1).coerceIn(0, 6)]

    fun fechaLarga(fecha: LocalDate): String =
        "${diaLargo(fecha.dayOfWeek.value)} ${fecha.dayOfMonth} de ${MESES[fecha.monthValue - 1]}"

    /** Los últimos 7 días terminando en [hoy], del más antiguo al más reciente. */
    fun ultimos7Dias(hoy: LocalDate): List<LocalDate> = (6 downTo 0).map { hoy.minusDays(it.toLong()) }

    fun textoDias(dias: List<Int>): String = when {
        dias.size == 7 -> "Todos los días"
        dias.sorted() == listOf(1, 2, 3, 4, 5) -> "De lunes a viernes"
        dias.sorted() == listOf(6, 7) -> "Fin de semana"
        dias.isEmpty() -> "Sin días elegidos"
        else -> dias.sorted().joinToString(" ") { diaCorto(it) }
    }
}
