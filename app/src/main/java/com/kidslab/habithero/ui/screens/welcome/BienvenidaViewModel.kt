package com.kidslab.habithero.ui.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidslab.habithero.data.repository.HabitHeroRepository
import com.kidslab.habithero.util.Catalogos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoBienvenida(
    val nombre: String = "",
    val avatar: String = Catalogos.AVATARES.first(),
    val guardando: Boolean = false
)

class BienvenidaViewModel(private val repositorio: HabitHeroRepository) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoBienvenida())
    val estado: StateFlow<EstadoBienvenida> = _estado.asStateFlow()

    fun cambiarNombre(valor: String) {
        _estado.update { it.copy(nombre = valor.take(20)) }
    }

    fun elegirAvatar(avatar: String) {
        _estado.update { it.copy(avatar = avatar) }
    }

    fun empezar(alTerminar: () -> Unit) {
        if (_estado.value.guardando) return
        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val actual = _estado.value
            repositorio.completarBienvenida(actual.nombre, actual.avatar)
            _estado.update { it.copy(guardando = false) }
            alTerminar()
        }
    }
}
