package com.kidslab.habithero.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidslab.habithero.data.local.entity.Habit
import com.kidslab.habithero.data.repository.HabitHeroRepository
import com.kidslab.habithero.domain.CalculadoraRachas
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/** Estado de un hábito en un día concreto de la semana mostrada. */
enum class EstadoDia { MARCADO, PENDIENTE, NO_TOCABA }

data class FilaProgreso(
    val habito: Habit,
    val dias: List<EstadoDia>,
    val rachaActual: Int,
    val mejorRacha: Int
)

data class EstadoProgreso(
    val cargando: Boolean = true,
    val fechas: List<LocalDate> = emptyList(),
    val filas: List<FilaProgreso> = emptyList(),
    val marcasSemana: Int = 0,
    val objetivoSemana: Int = 0,
    val mejorRachaGlobal: Int = 0
) {
    val porcentajeSemana: Float
        get() = if (objetivoSemana <= 0) 0f else (marcasSemana.toFloat() / objetivoSemana).coerceIn(0f, 1f)
}

class ProgresoViewModel(repositorio: HabitHeroRepository) : ViewModel() {

    val estado: StateFlow<EstadoProgreso> = combine(
        repositorio.habitosActivos,
        repositorio.todasLasMarcas
    ) { habitos, marcas ->
        val hoy = LocalDate.now()
        val semana = com.kidslab.habithero.util.FechasEs.ultimos7Dias(hoy)

        val fechasPorHabito = marcas.groupBy { it.habitId }
            .mapValues { entrada -> entrada.value.map { it.fecha }.toSet() }

        var marcasSemana = 0
        var objetivo = 0

        val filas = habitos.map { habito ->
            val fechas = fechasPorHabito[habito.id].orEmpty()
            val dias = semana.map { dia ->
                when {
                    !habito.tocaHoy(dia) -> EstadoDia.NO_TOCABA
                    dia in fechas -> EstadoDia.MARCADO
                    else -> EstadoDia.PENDIENTE
                }
            }
            marcasSemana += dias.count { it == EstadoDia.MARCADO }
            objetivo += dias.count { it != EstadoDia.NO_TOCABA }

            FilaProgreso(
                habito = habito,
                dias = dias,
                rachaActual = CalculadoraRachas.rachaActual(fechas, habito.diasSemana.toSet(), hoy),
                mejorRacha = CalculadoraRachas.mejorRacha(fechas, habito.diasSemana.toSet())
            )
        }

        EstadoProgreso(
            cargando = false,
            fechas = semana,
            filas = filas,
            marcasSemana = marcasSemana,
            objetivoSemana = objetivo,
            mejorRachaGlobal = filas.maxOfOrNull { it.mejorRacha } ?: 0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoProgreso())
}
