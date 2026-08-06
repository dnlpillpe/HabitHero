package com.kidslab.habithero.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculadoraRecompensasTest {

    @Test
    fun `una marca normal da la recompensa base`() {
        assertEquals(5, CalculadoraRecompensas.monedasPorMarca(1))
        assertEquals(10, CalculadoraRecompensas.experienciaPorMarca(1))
    }

    @Test
    fun `a partir de tres dias hay bonus`() {
        assertEquals(10, CalculadoraRecompensas.monedasPorMarca(3))
        assertEquals(15, CalculadoraRecompensas.experienciaPorMarca(3))
    }

    @Test
    fun `a partir de siete dias el bonus es mayor`() {
        assertEquals(15, CalculadoraRecompensas.monedasPorMarca(7))
        assertEquals(20, CalculadoraRecompensas.experienciaPorMarca(9))
    }

    @Test
    fun `el nivel sube cada cien puntos de experiencia`() {
        assertEquals(1, CalculadoraRecompensas.nivelPara(0))
        assertEquals(1, CalculadoraRecompensas.nivelPara(99))
        assertEquals(2, CalculadoraRecompensas.nivelPara(100))
        assertEquals(5, CalculadoraRecompensas.nivelPara(430))
    }

    @Test
    fun `el progreso de nivel siempre esta entre cero y uno`() {
        listOf(0, 45, 100, 199, 1234).forEach { xp ->
            val progreso = CalculadoraRecompensas.progresoNivel(xp)
            assertTrue("progreso fuera de rango para $xp", progreso in 0f..1f)
        }
        assertEquals(0f, CalculadoraRecompensas.progresoNivel(200), 0.001f)
        assertEquals(0.5f, CalculadoraRecompensas.progresoNivel(250), 0.001f)
    }

    @Test
    fun `la experiencia negativa no rompe el calculo`() {
        assertEquals(1, CalculadoraRecompensas.nivelPara(-50))
        assertEquals(0, CalculadoraRecompensas.experienciaEnNivel(-50))
    }
}
