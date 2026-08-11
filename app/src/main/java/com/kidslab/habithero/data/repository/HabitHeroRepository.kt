package com.kidslab.habithero.data.repository

import com.kidslab.habithero.data.local.AppDatabase
import com.kidslab.habithero.data.local.DatabaseSeeder
import com.kidslab.habithero.data.local.entity.Badge
import com.kidslab.habithero.data.local.entity.DesafioDiario
import com.kidslab.habithero.data.local.entity.Habit
import com.kidslab.habithero.data.local.entity.HabitCompletion
import com.kidslab.habithero.data.local.entity.UserBadge
import com.kidslab.habithero.data.local.entity.UserProfile
import com.kidslab.habithero.data.local.entity.UserUnlock
import com.kidslab.habithero.domain.CalculadoraRachas
import com.kidslab.habithero.domain.CalculadoraRecompensas
import com.kidslab.habithero.domain.EstadisticasHeroe
import com.kidslab.habithero.domain.EvaluadorDesafios
import com.kidslab.habithero.domain.EvaluadorInsignias
import com.kidslab.habithero.domain.EvaluadorTienda
import com.kidslab.habithero.domain.GeneradorDesafios
import com.kidslab.habithero.domain.MensajesAnimo
import com.kidslab.habithero.util.TiendaCatalogo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

/**
 * Única puerta de entrada a los datos. Los ViewModel no tocan los DAO directamente.
 */
class HabitHeroRepository(private val db: AppDatabase) {

    private val perfilDao = db.userProfileDao()
    private val habitDao = db.habitDao()
    private val marcaDao = db.habitCompletionDao()
    private val badgeDao = db.badgeDao()
    private val unlockDao = db.userUnlockDao()
    private val desafioDao = db.desafioDiarioDao()

    // ---------------------------------------------------------------- Lectura

    val perfil: Flow<UserProfile?> = perfilDao.observar()
    val habitosActivos: Flow<List<Habit>> = habitDao.observarActivos()
    val todasLasMarcas: Flow<List<HabitCompletion>> = marcaDao.observarTodas()
    val catalogoInsignias: Flow<List<Badge>> = badgeDao.observarCatalogo()
    val insigniasObtenidas: Flow<List<UserBadge>> = badgeDao.observarObtenidas()
    val itemsDesbloqueados: Flow<List<UserUnlock>> = unlockDao.observarTodas()

    suspend fun obtenerHabito(id: Long): Habit? = habitDao.obtener(id)

    /** Hábitos activos con recordatorio, para reprogramar las alarmas tras un reinicio. */
    suspend fun habitosConRecordatorio(): List<Habit> = habitDao.conRecordatorio()

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
        revisarDesafioDiario(fecha)

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

    suspend fun actualizarAvatarYMarco(avatar: String, marco: String?) =
        perfilDao.actualizarAvatarYMarco(avatar, marco)

    // ------------------------------------------------------------------ Tienda

    suspend fun comprarItem(itemId: String): ResultadoCompra {
        val item = TiendaCatalogo.obtener(itemId) ?: return ResultadoCompra.ItemNoEncontrado
        val yaComprado = unlockDao.obtener(itemId) != null
        val perfilActual = perfilDao.obtener() ?: return ResultadoCompra.MonedasInsuficientes

        if (!EvaluadorTienda.puedeComprar(perfilActual.monedas, item.precio, yaComprado)) {
            return if (yaComprado) ResultadoCompra.YaComprado else ResultadoCompra.MonedasInsuficientes
        }

        val filaId = unlockDao.insertar(UserUnlock(itemId = itemId))
        if (filaId == -1L) return ResultadoCompra.YaComprado

        perfilDao.actualizarMonedas(EvaluadorTienda.monedasTrasComprar(perfilActual.monedas, item.precio))
        return ResultadoCompra.Exito(item)
    }

    // ------------------------------------------------------------- Desafíos

    fun observarDesafio(fecha: LocalDate): Flow<DesafioDiario?> = desafioDao.observar(fecha)

    /** Devuelve el desafío de hoy, generándolo si todavía no existe. */
    suspend fun asegurarDesafioDeHoy(fecha: LocalDate = LocalDate.now()): DesafioDiario {
        desafioDao.obtener(fecha)?.let { return it }
        val nuevo = GeneradorDesafios.generarPara(fecha)
        desafioDao.insertar(nuevo)
        return desafioDao.obtener(fecha) ?: nuevo
    }

    private suspend fun revisarDesafioDiario(fecha: LocalDate) {
        val desafio = asegurarDesafioDeHoy(fecha)
        if (desafio.completado) return

        val marcasHoy = marcaDao.contarPorFecha(fecha)
        val habitosDeHoy = habitDao.obtenerTodosUnaVez().count { it.activo && it.tocaHoy(fecha) }
        val minutoActual = LocalTime.now().hour * 60 + LocalTime.now().minute

        if (!EvaluadorDesafios.cumplido(desafio, marcasHoy, habitosDeHoy, minutoActual)) return

        desafioDao.actualizar(desafio.copy(completado = true))

        val perfilActual = perfilDao.obtener() ?: return
        val nuevaExperiencia = perfilActual.experiencia + desafio.recompensaExperiencia
        perfilDao.actualizarRecompensas(
            monedas = perfilActual.monedas + desafio.recompensaMonedas,
            experiencia = nuevaExperiencia,
            nivel = CalculadoraRecompensas.nivelPara(nuevaExperiencia)
        )
    }

    // -------------------------------------------------------------- Reinicio

    /** Borra todo y vuelve a dejar la app como recién instalada. */
    suspend fun reiniciarTodo() = withContext(Dispatchers.IO) {
        db.clearAllTables()
        DatabaseSeeder.sembrar(db.openHelper.writableDatabase)
    }
}
