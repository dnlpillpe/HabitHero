package com.kidslab.habithero.ui.screens.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidslab.habithero.data.repository.HabitHeroRepository
import com.kidslab.habithero.data.repository.ResultadoCompra
import com.kidslab.habithero.util.TiendaCatalogo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ItemTiendaUi(val item: TiendaCatalogo.ItemTienda, val desbloqueado: Boolean)

data class EstadoTienda(
    val cargando: Boolean = true,
    val monedas: Int = 0,
    val avatares: List<ItemTiendaUi> = emptyList(),
    val marcos: List<ItemTiendaUi> = emptyList()
)

class TiendaViewModel(private val repositorio: HabitHeroRepository) : ViewModel() {

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    val estado: StateFlow<EstadoTienda> = combine(
        repositorio.perfil,
        repositorio.itemsDesbloqueados
    ) { perfil, desbloqueados ->
        val ids = desbloqueados.map { it.itemId }.toSet()
        EstadoTienda(
            cargando = false,
            monedas = perfil?.monedas ?: 0,
            avatares = TiendaCatalogo.AVATARES_EXTRA.map { ItemTiendaUi(it, it.id in ids) },
            marcos = TiendaCatalogo.MARCOS.map { ItemTiendaUi(it, it.id in ids) }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoTienda())

    fun comprar(itemId: String) {
        viewModelScope.launch {
            when (val resultado = repositorio.comprarItem(itemId)) {
                is ResultadoCompra.Exito -> _mensaje.value = "¡Has desbloqueado ${resultado.item.nombre} ${resultado.item.emoji}!"
                ResultadoCompra.MonedasInsuficientes -> _mensaje.value = "Todavía no tienes suficientes monedas."
                ResultadoCompra.YaComprado, ResultadoCompra.ItemNoEncontrado -> Unit
            }
        }
    }

    fun cerrarMensaje() {
        _mensaje.value = null
    }
}
