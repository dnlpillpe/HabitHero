package com.kidslab.habithero.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidslab.habithero.data.local.entity.Badge
import com.kidslab.habithero.data.local.entity.Habit
import com.kidslab.habithero.data.repository.HabitHeroRepository
import com.kidslab.habithero.data.repository.ResultadoMarcado
import com.kidslab.habithero.domain.CalculadoraRachas
import com.kidslab.habithero.domain.CalculadoraRecompensas
import com.kidslab.habithero.domain.MensajesAnimo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitoDelDia(
    val habito: Habit,
    val marcado: Boolean,
    val racha: Int
)

data class EstadoInicio(
    val cargando: Boolean = true,
    val nombre: String = "",
    val avatar: String = "🦸",
    val monedas: Int = 0,
    val nivel: Int = 1,
    val progresoNivel: Float = 0f,
    val fecha: LocalDate = LocalDate.now(),
    val habitosHoy: List<HabitoDelDia> = emptyList(),
    val habitosOtrosDias: Int = 0,
    val mensaje: String = ""
) {
    val completadosHoy: Int get() = habitosHoy.count { it.marcado }
    val totalHoy: Int get() = habitosHoy.size
    val todoListo: Boolean get() = totalHoy > 0 && completadosHoy == totalHoy
}

/** Evento puntual de celebración tras marcar un hábito. */
data class Celebracion(
    val mensaje: String,
    val monedas: Int,
    val experiencia: Int,
    val racha: Int,
    val subioDeNivel: Boolean,
    val nivelNuevo: Int,
    val insignias: List<Badge>
)

class InicioViewModel(private val repositorio: HabitHeroRepository) : ViewModel() {

    private val diaActual = MutableStateFlow(LocalDate.now())

    private val _celebracion = MutableStateFlow<Celebracion?>(null)
    val celebracion: StateFlow<Celebracion?> = _celebracion.asStateFlow()

    private val _confirmarDesmarcar = MutableStateFlow<Habit?>(null)
    val confirmarDesmarcar: StateFlow<Habit?> = _confirmarDesmarcar.asStateFlow()

    val estado: StateFlow<EstadoInicio> = combine(
        repositorio.perfil,
        repositorio.habitosActivos,
        repositorio.todasLasMarcas,
        diaActual
    ) { perfil, habitos, marcas, hoy ->
        val fechasPorHabito = marcas.groupBy { it.habitId }
            .mapValues { entrada -> entrada.value.map { it.fecha }.toSet() }

        val deHoy = habitos.filter { it.tocaHoy(hoy) }.map { habito ->
            val fechas = fechasPorHabito[habito.id].orEmpty()
            HabitoDelDia(
                habito = habito,
                marcado = hoy in fechas,
                racha = CalculadoraRachas.rachaActual(fechas, habito.diasSemana.toSet(), hoy)
            )
        }

        val experiencia = perfil?.experiencia ?: 0
        EstadoInicio(
            cargando = false,
            nombre = perfil?.nombre.orEmpty(),
            avatar = perfil?.avatar ?: "🦸",
            monedas = perfil?.monedas ?: 0,
            nivel = perfil?.nivel ?: 1,
            progresoNivel = CalculadoraRecompensas.progresoNivel(experiencia),
            fecha = hoy,
            habitosHoy = deHoy,
            habitosOtrosDias = habitos.size - deHoy.size,
            mensaje = when {
                deHoy.isEmpty() -> MensajesAnimo.animoDelDia(hoy.dayOfYear)
                deHoy.all { it.marcado } -> MensajesAnimo.sinPendientes()
                else -> MensajesAnimo.animoDelDia(hoy.dayOfYear)
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoInicio())

    /** Refresca la fecha por si la app quedó abierta durante el cambio de día. */
    fun refrescarDia() {
        diaActual.value = LocalDate.now()
    }

    fun pulsarHabito(item: HabitoDelDia) {
        if (item.marcado) {
            _confirmarDesmarcar.value = item.habito
        } else {
            marcar(item.habito)
        }
    }

    private fun marcar(habito: Habit) {
        viewModelScope.launch {
            when (val resultado = repositorio.marcarHabito(habito.id, diaActual.value)) {
                is ResultadoMarcado.Exito -> {
                    _celebracion.value = Celebracion(
                        mensaje = resultado.mensaje,
                        monedas = resultado.monedasGanadas,
                        experiencia = resultado.experienciaGanada,
                        racha = resultado.rachaActual,
                        subioDeNivel = resultado.subioDeNivel,
                        nivelNuevo = resultado.nivelNuevo,
                        insignias = resultado.insigniasNuevas
                    )
                }
                // Ya marcado o hábito inexistente: no se hace nada y no se premia dos veces.
                ResultadoMarcado.YaEstabaMarcado -> Unit
                ResultadoMarcado.HabitoNoEncontrado -> Unit
            }
        }
    }

    fun confirmarDesmarcado() {
        val habito = _confirmarDesmarcar.value ?: return
        _confirmarDesmarcar.value = null
        viewModelScope.launch {
            repositorio.desmarcarHabito(habito.id, diaActual.value)
        }
    }

    fun cancelarDesmarcado() {
        _confirmarDesmarcar.value = null
    }

    fun cerrarCelebracion() {
        _celebracion.value = null
    }
}
