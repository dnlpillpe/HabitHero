package com.kidslab.habithero.domain

import com.kidslab.habithero.data.local.entity.Badge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluadorInsigniasTest {

    private val primerPaso = Badge("primer_paso", "Primer paso", "", "🌟", Badge.TIPO_TOTAL_MARCAS, 1)
    private val racha7 = Badge("racha_7", "Semana heroica", "", "🏆", Badge.TIPO_RACHA, 7)
    private val cofre = Badge("cofre_100", "Cofre lleno", "", "💰", Badge.TIPO_MONEDAS, 100)

    private fun stats(
        marcas: Int = 0,
        racha: Int = 0,
        monedas: Int = 0,
        nivel: Int = 1,
        propios: Int = 0
    ) = EstadisticasHeroe(marcas, racha, monedas, nivel, propios)

    @Test
    fun `la primera marca desbloquea la primera insignia`() {
        assertTrue(EvaluadorInsignias.cumple(primerPaso, stats(marcas = 1)))
        assertFalse(EvaluadorInsignias.cumple(primerPaso, stats(marcas = 0)))
    }

    @Test
    fun `la insignia de racha necesita la racha completa`() {
        assertFalse(EvaluadorInsignias.cumple(racha7, stats(racha = 6)))
        assertTrue(EvaluadorInsignias.cumple(racha7, stats(racha = 7)))
    }

    @Test
    fun `no se vuelve a entregar una insignia ya conseguida`() {
        val catalogo = listOf(primerPaso, cofre)
        val nuevas = EvaluadorInsignias.nuevas(
            catalogo = catalogo,
            yaObtenidas = setOf("primer_paso"),
            stats = stats(marcas = 10, monedas = 120)
        )
        assertEquals(listOf("cofre_100"), nuevas.map { it.id })
    }

    @Test
    fun `el progreso se corta en uno`() {
        assertEquals(0.5f, EvaluadorInsignias.progreso(cofre, stats(monedas = 50)), 0.001f)
        assertEquals(1f, EvaluadorInsignias.progreso(cofre, stats(monedas = 500)), 0.001f)
    }
}
