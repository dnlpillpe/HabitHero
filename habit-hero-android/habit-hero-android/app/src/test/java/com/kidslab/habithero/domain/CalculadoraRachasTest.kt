package com.kidslab.habithero.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CalculadoraRachasTest {

    private val todosLosDias = setOf(1, 2, 3, 4, 5, 6, 7)
    private val lunesAViernes = setOf(1, 2, 3, 4, 5)

    // Miércoles 2025-01-15, para tener días laborables alrededor.
    private val miercoles = LocalDate.of(2025, 1, 15)

    @Test
    fun `sin marcas la racha es cero`() {
        assertEquals(0, CalculadoraRachas.rachaActual(emptySet(), todosLosDias, miercoles))
    }

    @Test
    fun `tres dias seguidos incluido hoy dan racha de tres`() {
        val fechas = setOf(miercoles, miercoles.minusDays(1), miercoles.minusDays(2))
        assertEquals(3, CalculadoraRachas.rachaActual(fechas, todosLosDias, miercoles))
    }

    @Test
    fun `hoy sin marcar todavia no rompe la racha`() {
        val fechas = setOf(miercoles.minusDays(1), miercoles.minusDays(2))
        assertEquals(2, CalculadoraRachas.rachaActual(fechas, todosLosDias, miercoles))
    }

    @Test
    fun `un dia saltado corta la racha`() {
        val fechas = setOf(miercoles, miercoles.minusDays(2), miercoles.minusDays(3))
        assertEquals(1, CalculadoraRachas.rachaActual(fechas, todosLosDias, miercoles))
    }

    @Test
    fun `los dias no programados no rompen la racha`() {
        // Hábito de lunes a viernes: el sábado y el domingo no cuentan.
        val lunes = LocalDate.of(2025, 1, 13)
        val fechas = setOf(
            lunes,                    // lunes 13
            lunes.minusDays(3),       // viernes 10
            lunes.minusDays(4)        // jueves 9
        )
        assertEquals(3, CalculadoraRachas.rachaActual(fechas, lunesAViernes, lunes))
    }

    @Test
    fun `la mejor racha mira todo el historial`() {
        val base = LocalDate.of(2025, 1, 1)
        val fechas = setOf(
            base, base.plusDays(1), base.plusDays(2), base.plusDays(3), // racha de 4
            base.plusDays(6), base.plusDays(7)                          // racha de 2
        )
        assertEquals(4, CalculadoraRachas.mejorRacha(fechas, todosLosDias))
    }

    @Test
    fun `la mejor racha con historial vacio es cero`() {
        assertEquals(0, CalculadoraRachas.mejorRacha(emptySet(), todosLosDias))
    }
}
