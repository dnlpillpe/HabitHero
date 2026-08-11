package com.kidslab.habithero.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kidslab.habithero.data.local.AppDatabase
import com.kidslab.habithero.data.local.entity.Habit
import com.kidslab.habithero.data.local.entity.UserProfile
import com.kidslab.habithero.data.repository.HabitHeroRepository
import com.kidslab.habithero.data.repository.ResultadoCompra
import com.kidslab.habithero.data.repository.ResultadoMarcado
import com.kidslab.habithero.domain.CalculadoraRecompensas
import com.kidslab.habithero.domain.GeneradorDesafios
import com.kidslab.habithero.util.TiendaCatalogo
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

/**
 * Pruebas de la capa de datos con Room de verdad (base en memoria sobre Robolectric).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HabitHeroRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repositorio: HabitHeroRepository

    private val hoy: LocalDate = LocalDate.of(2025, 3, 12)

    @Before
    fun crearBase() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repositorio = HabitHeroRepository(db)
    }

    @After
    fun cerrarBase() {
        db.close()
    }

    private suspend fun crearHabitoDePrueba(nombre: String = "Beber agua"): Long =
        repositorio.crearHabito(
            Habit(
                nombre = nombre,
                icono = "💧",
                diasSemana = listOf(1, 2, 3, 4, 5, 6, 7),
                esPredeterminado = false
            )
        )

    @Test
    fun `marcar un habito guarda la marca y suma recompensas`() = runTest {
        repositorio.asegurarPerfil()
        val id = crearHabitoDePrueba()

        val resultado = repositorio.marcarHabito(id, hoy)

        assertTrue(resultado is ResultadoMarcado.Exito)
        resultado as ResultadoMarcado.Exito
        assertEquals(5, resultado.monedasGanadas)
        assertEquals(10, resultado.experienciaGanada)
        assertEquals(1, resultado.rachaActual)

        assertTrue(repositorio.estaMarcado(id, hoy))

        val perfil = db.userProfileDao().obtener()
        assertNotNull(perfil)
        assertEquals(5, perfil!!.monedas)
        assertEquals(10, perfil.experiencia)
    }

    @Test
    fun `un habito no se puede marcar dos veces el mismo dia`() = runTest {
        repositorio.asegurarPerfil()
        val id = crearHabitoDePrueba()

        repositorio.marcarHabito(id, hoy)
        val segundoIntento = repositorio.marcarHabito(id, hoy)

        assertEquals(ResultadoMarcado.YaEstabaMarcado, segundoIntento)
        assertEquals(1, db.habitCompletionDao().contarTotal())

        // Y tampoco se premia dos veces.
        val perfil = db.userProfileDao().obtener()!!
        assertEquals(5, perfil.monedas)
        assertEquals(10, perfil.experiencia)
    }

    @Test
    fun `el mismo habito si se puede marcar en dias distintos`() = runTest {
        repositorio.asegurarPerfil()
        val id = crearHabitoDePrueba()

        repositorio.marcarHabito(id, hoy.minusDays(1))
        val segundo = repositorio.marcarHabito(id, hoy)

        assertTrue(segundo is ResultadoMarcado.Exito)
        assertEquals(2, (segundo as ResultadoMarcado.Exito).rachaActual)
        assertEquals(2, db.habitCompletionDao().contarTotal())
    }

    @Test
    fun `desmarcar devuelve los puntos y deja el habito pendiente`() = runTest {
        repositorio.asegurarPerfil()
        val id = crearHabitoDePrueba()

        repositorio.marcarHabito(id, hoy)
        repositorio.desmarcarHabito(id, hoy)

        assertFalse(repositorio.estaMarcado(id, hoy))
        assertEquals(0, db.habitCompletionDao().contarTotal())

        val perfil = db.userProfileDao().obtener()!!
        assertEquals(0, perfil.monedas)
        assertEquals(0, perfil.experiencia)
        assertEquals(1, perfil.nivel)
    }

    @Test
    fun `las marcas y los habitos persisten en Room`() = runTest {
        repositorio.asegurarPerfil()
        val id = crearHabitoDePrueba(nombre = "Leer un rato")
        repositorio.marcarHabito(id, hoy)
        repositorio.marcarHabito(id, hoy.minusDays(1))

        // Se vuelve a leer desde la base, no desde memoria del repositorio.
        val habitoGuardado = db.habitDao().obtener(id)
        assertNotNull(habitoGuardado)
        assertEquals("Leer un rato", habitoGuardado!!.nombre)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7), habitoGuardado.diasSemana)

        val marcas = db.habitCompletionDao().porHabito(id)
        assertEquals(2, marcas.size)
        assertEquals(setOf(hoy, hoy.minusDays(1)), marcas.map { it.fecha }.toSet())
    }

    @Test
    fun `al borrar un habito se borran sus marcas en cascada`() = runTest {
        repositorio.asegurarPerfil()
        val id = crearHabitoDePrueba()
        repositorio.marcarHabito(id, hoy)

        val habito = db.habitDao().obtener(id)!!
        repositorio.eliminarHabito(habito)

        assertEquals(0, db.habitCompletionDao().contarTotal())
    }

    @Test
    fun `el nivel sube al acumular experiencia`() = runTest {
        repositorio.asegurarPerfil()
        val id = crearHabitoDePrueba()

        // Diez marcas en diez días distintos superan los 100 puntos de experiencia.
        repeat(10) { indice ->
            repositorio.marcarHabito(id, hoy.minusDays(indice.toLong()))
        }

        val perfil = db.userProfileDao().obtener()!!
        assertTrue("experiencia insuficiente", perfil.experiencia >= 100)
        assertTrue("el nivel deberia haber subido", perfil.nivel >= 2)
    }

    @Test
    fun `comprar un item de la tienda descuenta las monedas y lo desbloquea`() = runTest {
        repositorio.guardarPerfil(UserProfile(monedas = 100))
        val item = TiendaCatalogo.AVATARES_EXTRA.first()

        val resultado = repositorio.comprarItem(item.id)

        assertTrue(resultado is ResultadoCompra.Exito)
        val perfil = db.userProfileDao().obtener()!!
        assertEquals(100 - item.precio, perfil.monedas)
        assertNotNull(db.userUnlockDao().obtener(item.id))
    }

    @Test
    fun `no se puede comprar un item si no alcanzan las monedas`() = runTest {
        repositorio.guardarPerfil(UserProfile(monedas = 0))
        val item = TiendaCatalogo.AVATARES_EXTRA.first()

        val resultado = repositorio.comprarItem(item.id)

        assertEquals(ResultadoCompra.MonedasInsuficientes, resultado)
        assertEquals(0, db.userProfileDao().obtener()!!.monedas)
    }

    @Test
    fun `un item ya comprado no se puede volver a comprar ni a cobrar`() = runTest {
        repositorio.guardarPerfil(UserProfile(monedas = 200))
        val item = TiendaCatalogo.AVATARES_EXTRA.first()

        repositorio.comprarItem(item.id)
        val segundoIntento = repositorio.comprarItem(item.id)

        assertEquals(ResultadoCompra.YaComprado, segundoIntento)
        assertEquals(200 - item.precio, db.userProfileDao().obtener()!!.monedas)
    }

    @Test
    fun `el desafio diario otorga su recompensa una sola vez al cumplirse`() = runTest {
        repositorio.asegurarPerfil()
        val desafio = repositorio.asegurarDesafioDeHoy(hoy)

        when (desafio.tipo) {
            GeneradorDesafios.TIPO_TRES_HABITOS -> {
                repeat(3) { indice -> repositorio.marcarHabito(crearHabitoDePrueba("Habito $indice"), hoy) }
            }
            GeneradorDesafios.TIPO_TODOS_HOY -> {
                repositorio.marcarHabito(crearHabitoDePrueba(), hoy)
            }
            else -> {
                // TIPO_ANTES_DE_HORA depende de la hora real del reloj; solo se
                // comprueba que marcar un habito no falle, sin exigir que se cumpla.
                repositorio.marcarHabito(crearHabitoDePrueba(), hoy)
                return@runTest
            }
        }

        val perfil = db.userProfileDao().obtener()!!
        val desafioActualizado = db.desafioDiarioDao().obtener(hoy)!!
        assertTrue(desafioActualizado.completado)
        assertTrue("la recompensa del desafio deberia sumarse", perfil.monedas >= desafio.recompensaMonedas)

        // Una marca extra, tras completado = true, ya no debe volver a pagar el desafío.
        val monedasAntesDeLaExtra = perfil.monedas
        repositorio.marcarHabito(crearHabitoDePrueba("Extra"), hoy)
        val monedasTrasLaExtra = db.userProfileDao().obtener()!!.monedas
        assertEquals(CalculadoraRecompensas.monedasPorMarca(1), monedasTrasLaExtra - monedasAntesDeLaExtra)
    }
}
