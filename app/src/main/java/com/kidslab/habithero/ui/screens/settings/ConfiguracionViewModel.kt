package com.kidslab.habithero.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidslab.habithero.data.repository.HabitHeroRepository
import com.kidslab.habithero.util.Catalogos
import com.kidslab.habithero.util.TiendaCatalogo
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
    val marcoSeleccionado: String? = null,
    val avataresDesbloqueados: List<String> = emptyList(),
    val marcosDesbloqueados: List<TiendaCatalogo.ItemTienda> = emptyList(),
    val totalHabitos: Int = 0,
    val totalMarcas: Int = 0,
    val monedas: Int = 0,
    val nivel: Int = 1
) {
    val avataresDisponibles: List<String> get() = Catalogos.AVATARES + avataresDesbloqueados
}

class ConfiguracionViewModel(private val repositorio: HabitHeroRepository) : ViewModel() {

    val estado: StateFlow<EstadoConfiguracion> = combine(
        repositorio.perfil,
        repositorio.habitosActivos,
        repositorio.todasLasMarcas,
        repositorio.itemsDesbloqueados
    ) { perfil, habitos, marcas, desbloqueados ->
        val idsDesbloqueados = desbloqueados.map { it.itemId }.toSet()
        EstadoConfiguracion(
            nombre = perfil?.nombre.orEmpty(),
            avatar = perfil?.avatar ?: "🦸",
            marcoSeleccionado = perfil?.marcoSeleccionado,
            avataresDesbloqueados = TiendaCatalogo.AVATARES_EXTRA.filter { it.id in idsDesbloqueados }.map { it.emoji },
            marcosDesbloqueados = TiendaCatalogo.MARCOS.filter { it.id in idsDesbloqueados },
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

    /** El marco es opcional y solo puede ser uno ya comprado en la tienda (o ninguno). */
    fun seleccionarMarco(marco: String?) {
        viewModelScope.launch {
            repositorio.actualizarAvatarYMarco(estado.value.avatar, marco)
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
