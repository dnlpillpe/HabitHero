package com.kidslab.habithero.ui.screens.habitedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidslab.habithero.data.local.entity.Habit
import com.kidslab.habithero.data.repository.HabitHeroRepository
import com.kidslab.habithero.util.Catalogos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoEditor(
    val cargando: Boolean = true,
    val esNuevo: Boolean = true,
    val habitId: Long = 0L,
    val nombre: String = "",
    val icono: String = Catalogos.ICONOS_HABITO.first(),
    val colorIndex: Int = 0,
    val dias: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7),
    val esPredeterminado: Boolean = false,
    val guardado: Boolean = false
) {
    val nombreValido: Boolean get() = nombre.trim().isNotEmpty()
    val diasValidos: Boolean get() = dias.isNotEmpty()
    val puedeGuardar: Boolean get() = nombreValido && diasValidos
    val caracteresRestantes: Int get() = Habit.MAX_NOMBRE - nombre.length
}

class EditorHabitoViewModel(private val repositorio: HabitHeroRepository) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoEditor())
    val estado: StateFlow<EstadoEditor> = _estado.asStateFlow()

    private var yaCargado = false

    fun cargar(habitId: Long) {
        if (yaCargado) return
        yaCargado = true

        if (habitId <= 0L) {
            _estado.update {
                it.copy(
                    cargando = false,
                    esNuevo = true,
                    colorIndex = (0 until Catalogos.TOTAL_COLORES).random()
                )
            }
            return
        }

        viewModelScope.launch {
            val habito = repositorio.obtenerHabito(habitId)
            _estado.value = if (habito == null) {
                EstadoEditor(cargando = false, esNuevo = true)
            } else {
                EstadoEditor(
                    cargando = false,
                    esNuevo = false,
                    habitId = habito.id,
                    nombre = habito.nombre,
                    icono = habito.icono,
                    colorIndex = habito.colorIndex,
                    dias = habito.diasSemana.toSet(),
                    esPredeterminado = habito.esPredeterminado
                )
            }
        }
    }

    fun cambiarNombre(valor: String) {
        _estado.update { it.copy(nombre = valor.take(Habit.MAX_NOMBRE)) }
    }

    fun cambiarIcono(icono: String) = _estado.update { it.copy(icono = icono) }

    fun cambiarColor(indice: Int) = _estado.update { it.copy(colorIndex = indice) }

    fun cambiarDias(dias: Set<Int>) = _estado.update { it.copy(dias = dias) }

    fun aplicarAtajo(dias: Set<Int>) = cambiarDias(dias)

    fun guardar(alTerminar: () -> Unit) {
        val actual = _estado.value
        if (!actual.puedeGuardar) return

        viewModelScope.launch {
            val habito = Habit(
                id = actual.habitId,
                nombre = actual.nombre.trim(),
                icono = actual.icono,
                diasSemana = actual.dias.sorted(),
                colorIndex = actual.colorIndex,
                esPredeterminado = actual.esPredeterminado,
                activo = true
            )
            if (actual.esNuevo) {
                repositorio.crearHabito(habito)
                repositorio.revisarInsignias()
            } else {
                val original = repositorio.obtenerHabito(actual.habitId)
                repositorio.actualizarHabito(
                    habito.copy(
                        orden = original?.orden ?: 0,
                        fechaCreacion = original?.fechaCreacion ?: habito.fechaCreacion
                    )
                )
            }
            _estado.update { it.copy(guardado = true) }
            alTerminar()
        }
    }

    fun eliminar(alTerminar: () -> Unit) {
        val actual = _estado.value
        if (actual.esNuevo) {
            alTerminar()
            return
        }
        viewModelScope.launch {
            repositorio.obtenerHabito(actual.habitId)?.let { repositorio.eliminarHabito(it) }
            alTerminar()
        }
    }
}
