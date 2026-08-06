package com.kidslab.habithero.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidslab.habithero.data.repository.HabitHeroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoConfiguracion(
    val nombre: String = "",
    val avatar: String = "🦸",
    val totalHabitos: Int = 0,
    val totalMarcas: Int = 0,
    val monedas: Int = 0,
    val nivel: Int = 1
)

class ConfiguracionViewModel(private val repositorio: HabitHeroRepository) : ViewModel() {

    val estado: StateFlow<EstadoConfiguracion> = combine(
        repositorio.perfil,
        repositorio.habitosActivos,
        repositorio.todasLasMarcas
    ) { perfil, habitos, marcas ->
        EstadoConfiguracion(
            nombre = perfil?.nombre.orEmpty(),
            avatar = perfil?.avatar ?: "🦸",
            totalHabitos = habitos.size,
            totalMarcas = marcas.size,
            monedas = perfil?.monedas ?: 0,
            nivel = perfil?.nivel ?: 1
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoConfiguracion())

    private val _reiniciando = MutableStateFlow(false)
    val reiniciando: StateFlow<Boolean> = _reiniciando.asStateFlow()

    fun guardarPerfil(nombre: String, avatar: String) {
        viewModelScope.launch {
            repositorio.completarBienvenida(nombre, avatar)
        }
    }

    fun reiniciarTodo(alTerminar: () -> Unit) {
        if (_reiniciando.value) return
        _reiniciando.value = true
        viewModelScope.launch {
            repositorio.reiniciarTodo()
            _reiniciando.update { false }
            alTerminar()
        }
    }
}
