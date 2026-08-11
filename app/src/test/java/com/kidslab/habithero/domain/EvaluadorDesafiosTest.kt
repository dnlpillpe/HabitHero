package com.kidslab.habithero.domain

import com.kidslab.habithero.data.local.entity.DesafioDiario
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class EvaluadorDesafiosTest {

    private val fecha = LocalDate.of(2025, 3, 12)

    @Test
    fun `desafio de tres habitos se cumple al llegar a la meta`() {
        val desafio = DesafioDiario(fecha, GeneradorDesafios.TIPO_TRES_HABITOS, meta = 3, recompensaMonedas = 15, recompensaExperiencia = 20)

        assertFalse(EvaluadorDesafios.cumplido(desafio, marcasHoy = 2, totalHabitosHoy = 6, minutoActual = 600))
        assertTrue(EvaluadorDesafios.cumplido(desafio, marcasHoy = 3, totalHabitosHoy = 6, minutoActual = 600))
    }

    @Test
    fun `desafio de todos los habitos necesita marcar exactamente todos los de hoy`() {
        val desafio = DesafioDiario(fecha, GeneradorDesafios.TIPO_TODOS_HOY, meta = 0, recompensaMonedas = 20, recompensaExperiencia = 25)

        assertFalse(EvaluadorDesafios.cumplido(desafio, marcasHoy = 2, totalHabitosHoy = 3, minutoActual = 600))
        assertTrue(EvaluadorDesafios.cumplido(desafio, marcasHoy = 3, totalHabitosHoy = 3, minutoActual = 600))
        // Sin habitos programados hoy no hay nada que "completar del todo".
        assertFalse(EvaluadorDesafios.cumplido(desafio, marcasHoy = 0, totalHabitosHoy = 0, minutoActual = 600))
    }

    @Test
    fun `desafio antes de hora exige al menos una marca dentro del limite`() {
        val desafio = DesafioDiario(
            fecha, GeneradorDesafios.TIPO_ANTES_DE_HORA,
            meta = GeneradorDesafios.MINUTO_LIMITE_ANTES_DE_HORA, recompensaMonedas = 10, recompensaExperiencia = 15
        )

        assertTrue(EvaluadorDesafios.cumplido(desafio, marcasHoy = 1, totalHabitosHoy = 5, minutoActual = 19 * 60))
        assertFalse(EvaluadorDesafios.cumplido(desafio, marcasHoy = 1, totalHabitosHoy = 5, minutoActual = 21 * 60))
        assertFalse(EvaluadorDesafios.cumplido(desafio, marcasHoy = 0, totalHabitosHoy = 5, minutoActual = 10 * 60))
    }
}
