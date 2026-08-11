package com.kidslab.habithero.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluadorTiendaTest {

    @Test
    fun `se puede comprar si alcanzan las monedas y no se tiene ya`() {
        assertTrue(EvaluadorTienda.puedeComprar(monedas = 50, precio = 30, yaComprado = false))
        assertFalse(EvaluadorTienda.puedeComprar(monedas = 20, precio = 30, yaComprado = false))
    }

    @Test
    fun `no se puede volver a comprar un item ya adquirido`() {
        assertFalse(EvaluadorTienda.puedeComprar(monedas = 1000, precio = 30, yaComprado = true))
    }

    @Test
    fun `las monedas nunca quedan negativas`() {
        assertEquals(0, EvaluadorTienda.monedasTrasComprar(monedas = 30, precio = 30))
        assertEquals(20, EvaluadorTienda.monedasTrasComprar(monedas = 50, precio = 30))
    }
}
