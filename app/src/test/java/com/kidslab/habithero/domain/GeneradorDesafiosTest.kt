package com.kidslab.habithero.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GeneradorDesafiosTest {

    @Test
    fun `la misma fecha siempre genera el mismo desafio`() {
        val fecha = LocalDate.of(2025, 3, 12)
        val primero = GeneradorDesafios.generarPara(fecha)
        val segundo = GeneradorDesafios.generarPara(fecha)

        assertEquals(primero.tipo, segundo.tipo)
        assertEquals(primero.meta, segundo.meta)
        assertEquals(primero.recompensaMonedas, segundo.recompensaMonedas)
    }

    @Test
    fun `fechas distintas pueden generar desafios distintos pero siempre validos`() {
        val tipos = setOf(
            GeneradorDesafios.TIPO_TRES_HABITOS,
            GeneradorDesafios.TIPO_TODOS_HOY,
            GeneradorDesafios.TIPO_ANTES_DE_HORA
        )
        (0..30L).forEach { dias ->
            val desafio = GeneradorDesafios.generarPara(LocalDate.of(2025, 1, 1).plusDays(dias))
            assertTrue("tipo inesperado: ${desafio.tipo}", desafio.tipo in tipos)
            assertTrue(desafio.recompensaMonedas > 0)
            assertTrue(desafio.recompensaExperiencia > 0)
            assertEquals(false, desafio.completado)
        }
    }
}
