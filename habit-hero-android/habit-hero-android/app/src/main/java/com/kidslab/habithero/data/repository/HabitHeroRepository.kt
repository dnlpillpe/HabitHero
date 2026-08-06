package com.kidslab.habithero.data.repository

import com.kidslab.habithero.data.local.AppDatabase
import com.kidslab.habithero.data.local.DatabaseSeeder
import com.kidslab.habithero.data.local.entity.Badge
import com.kidslab.habithero.data.local.entity.Habit
import com.kidslab.habithero.data.local.entity.HabitCompletion
import com.kidslab.habithero.data.local.entity.UserBadge
import com.kidslab.habithero.data.local.entity.UserProfile
import com.kidslab.habithero.domain.CalculadoraRachas
import com.kidslab.habithero.domain.CalculadoraRecompensas
import com.kidslab.habithero.domain.EstadisticasHeroe
import com.kidslab.habithero.domain.EvaluadorInsignias
import com.kidslab.habithero.domain.MensajesAnimo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Única puerta de entrada a los datos. Los ViewModel no tocan los DAO directamente.
 */
class HabitHeroRepository(private val db: AppDatabase) {

    private val perfilDao = db.userProfileDao()
    private val habitDao = db.habitDao()
    private val marcaDao = db.habitCompletionDao()
    private val badgeDao = db.badgeDao()

    // ---------------------------------------------------------------- Lectura

    val perfil: Flow<UserProfile?> = perfilDao.observar()
    val habitosActivos: Flow<List<Habit>> = habitDao.observarActivos()
    val todasLasMarcas: Flow<List<HabitCompletion>> = marcaDao.observarTodas()
    val catalogoInsignias: Flow<List<Badge>> = badgeDao.observarCatalogo()
    val insigniasObtenidas: Flow<List<UserBadge>> = badgeDao.observarObtenidas()

    suspend fun obtenerHabito(id: Long): Habit? = habitDao.obtener(id)

    // ---------------------------------------------------------------- Hábitos

    suspend fun crearHabito(habito: Habit): Long {
        val orden = habitDao.ordenMaximo() + 1
        return habitDao.insertar(habito.copy(orden = orden))
    }

    suspend fun actualizarHabito(habito: Habit) = habitDao.actualizar(habito)

    suspend fun eliminarHabito(habito: Habit) = habitDao.eliminar(habito)

    // ----------------------------------------------------------------- Marcar

    /**
     * Marca un hábito en una fecha. Es idempotente: el índice único
     * (habitId, fecha) impide una segunda fila y, por tanto, un premio doble.
     */
    suspend fun marcarHabito(habitId: Long, fecha: LocalDate): ResultadoMarcado {
        val habito = habitDao.obtener(habitId) ?: return ResultadoMarcado.HabitoNoEncontrado

        if (marcaDao.contarEnFecha(habitId, fecha) > 0) return ResultadoMarcado.YaEstabaMarcado

        val dias = habito.diasSemana.toSet()
        val fechasTrasMarcar = marcaDao.porHabito(habitId).map { it.fecha }.toMutableSet()
        fechasTrasMarcar.add(fecha)

        val racha = CalculadoraRachas.rachaActual(fechasTrasMarcar, dias, fecha)
        val monedas = CalculadoraRecompensas.monedasPorMarca(racha)
        val experiencia = CalculadoraRecompensas.experienciaPorMarca(racha)

        val filaId = marcaDao.insertar(
            HabitCompletion(
                habitId = habitId,
                fecha = fecha,
                monedasGanadas = monedas,
                experienciaGanada = experiencia
            )
        )
        if (filaId == -1L) return ResultadoMarcado.YaEstabaMarcado

        val perfilActual = perfilDao.obtener() ?: UserProfile().also { perfilDao.guardar(it) }
        val nivelAntes = perfilActual.nivel
        val nuevaExperiencia = perfilActual.experiencia + experiencia
        val nuevasMonedas = perfilActual.monedas + monedas
        val nivelDespues = CalculadoraRecompensas.nivelPara(nuevaExperiencia)

        perfilDao.actualizarRecompensas(nuevasMonedas, nuevaExperiencia, nivelDespues)

        val nuevas = otorgarInsigniasPendientes()

        return ResultadoMarcado.Exito(
            nombreHabito = habito.nombre,
            monedasGanadas = monedas,
            experienciaGanada = experiencia,
            rachaActual = racha,
            subioDeNivel = nivelDespues > nivelAntes,
            nivelNuevo = nivelDespues,
            insigniasNuevas = nuevas,
            mensaje = MensajesAnimo.alMarcar(habitId.toInt() + racha)
        )
    }

    /** Deshace una marca puesta por error y devuelve los puntos que había dado. */
    suspend fun desmarcarHabito(habitId: Long, fecha: LocalDate) {
        val marca = marcaDao.porHabito(habitId).firstOrNull { it.fecha == fecha } ?: return
        val borradas = marcaDao.borrar(habitId, fecha)
        if (borradas <= 0) return

        val perfilActual = perfilDao.obtener() ?: return
        val monedas = (perfilActual.monedas - marca.monedasGanadas).coerceAtLeast(0)
        val experiencia = (perfilActual.experiencia - marca.experienciaGanada).coerceAtLeast(0)
        perfilDao.actualizarRecompensas(
            monedas = monedas,
            experiencia = experiencia,
            nivel = CalculadoraRecompensas.nivelPara(experiencia)
        )
        // Las insignias ya conseguidas no se retiran: en HabitHero nunca se castiga.
    }

    suspend fun estaMarcado(habitId: Long, fecha: LocalDate): Boolean =
        marcaDao.contarEnFecha(habitId, fecha) > 0

    // -------------------------------------------------------------- Insignias

    suspend fun estadisticas(): EstadisticasHeroe {
        val perfilActual = perfilDao.obtener() ?: UserProfile()
        val habitos = habitDao.obtenerTodosUnaVez()
        val mejorRacha = habitos.maxOfOrNull { habito ->
            CalculadoraRachas.mejorRacha(
                fechas = marcaDao.porHabito(habito.id).map { it.fecha }.toSet(),
                dias = habito.diasSemana.toSet()
            )
        } ?: 0

        return EstadisticasHeroe(
            totalMarcas = marcaDao.contarTotal(),
            mejorRachaGlobal = mejorRacha,
            monedas = perfilActual.monedas,
            nivel = perfilActual.nivel,
            habitosPropios = habitDao.contarPersonalizados()
        )
    }

    private suspend fun otorgarInsigniasPendientes(): List<Badge> {
        val stats = estadisticas()
        val obtenidas = badgeDao.idsObtenidas().toSet()
        val nuevas = EvaluadorInsignias.nuevas(badgeDao.catalogo(), obtenidas, stats)
        nuevas.forEach { insignia ->
            badgeDao.otorgar(UserBadge(badgeId = insignia.id, fechaObtencion = LocalDate.now()))
        }
        return nuevas
    }

    suspend fun revisarInsignias(): List<Badge> = otorgarInsigniasPendientes()

    suspend fun marcarInsigniasVistas() = badgeDao.marcarTodasVistas()

    // ----------------------------------------------------------------- Perfil

    suspend fun asegurarPerfil() {
        if (perfilDao.obtener() == null) perfilDao.guardar(UserProfile())
    }

    suspend fun completarBienvenida(nombre: String, avatar: String) {
        asegurarPerfil()
        val limpio = nombre.trim().ifBlank { "Héroe" }.take(20)
        perfilDao.completarBienvenida(limpio, avatar)
    }

    suspend fun guardarPerfil(perfil: UserProfile) = perfilDao.guardar(perfil)

    // -------------------------------------------------------------- Reinicio

    /** Borra todo y vuelve a dejar la app como recién instalada. */
    suspend fun reiniciarTodo() = withContext(Dispatchers.IO) {
        db.clearAllTables()
        DatabaseSeeder.sembrar(db.openHelper.writableDatabase)
    }
}
