package com.kidslab.habithero.ui.screens.badges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidslab.habithero.data.local.entity.Badge
import com.kidslab.habithero.data.repository.HabitHeroRepository
import com.kidslab.habithero.domain.CalculadoraRachas
import com.kidslab.habithero.domain.EstadisticasHeroe
import com.kidslab.habithero.domain.EvaluadorInsignias
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class InsigniaUi(
    val insignia: Badge,
    val conseguida: Boolean,
    val fecha: LocalDate?,
    val progreso: Float
)

data class EstadoInsignias(
    val cargando: Boolean = true,
    val insignias: List<InsigniaUi> = emptyList(),
    val monedas: Int = 0,
    val nivel: Int = 1,
    val totalMarcas: Int = 0
) {
    val conseguidas: Int get() = insignias.count { it.conseguida }
    val total: Int get() = insignias.size
}

class InsigniasViewModel(private val repositorio: HabitHeroRepository) : ViewModel() {

    val estado: StateFlow<EstadoInsignias> = combine(
        repositorio.catalogoInsignias,
        repositorio.insigniasObtenidas,
        repositorio.perfil,
        repositorio.habitosActivos,
        repositorio.todasLasMarcas
    ) { catalogo, obtenidas, perfil, habitos, marcas ->

        val fechasPorHabito = marcas.groupBy { it.habitId }
            .mapValues { entrada -> entrada.value.map { it.fecha }.toSet() }

        val mejorRacha = habitos.maxOfOrNull { habito ->
            CalculadoraRachas.mejorRacha(
                fechas = fechasPorHabito[habito.id].orEmpty(),
                dias = habito.diasSemana.toSet()
            )
        } ?: 0

        val stats = EstadisticasHeroe(
            totalMarcas = marcas.size,
            mejorRachaGlobal = mejorRacha,
            monedas = perfil?.monedas ?: 0,
            nivel = perfil?.nivel ?: 1,
            habitosPropios = habitos.count { !it.esPredeterminado }
        )

        val porId = obtenidas.associateBy { it.badgeId }

        EstadoInsignias(
            cargando = false,
            insignias = catalogo.map { insignia ->
                val ganada = porId[insignia.id]
                InsigniaUi(
                    insignia = insignia,
                    conseguida = ganada != null,
                    fecha = ganada?.fechaObtencion,
                    progreso = if (ganada != null) 1f else EvaluadorInsignias.progreso(insignia, stats)
                )
            },
            monedas = stats.monedas,
            nivel = stats.nivel,
            totalMarcas = stats.totalMarcas
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoInsignias())

    init {
        // Por si alguna insignia se cumplió fuera del flujo normal de marcado.
        viewModelScope.launch {
            repositorio.revisarInsignias()
            repositorio.marcarInsigniasVistas()
        }
    }
}
