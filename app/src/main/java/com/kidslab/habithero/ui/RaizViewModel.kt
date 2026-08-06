package com.kidslab.habithero.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kidslab.habithero.data.repository.HabitHeroRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Decide si hay que mostrar la bienvenida o ir directo al inicio. */
class RaizViewModel(repositorio: HabitHeroRepository) : ViewModel() {

    val estado: StateFlow<EstadoArranque> = repositorio.perfil
        .map { perfil ->
            when {
                perfil == null -> EstadoArranque.Cargando
                perfil.onboardingCompletado -> EstadoArranque.Listo
                else -> EstadoArranque.NecesitaBienvenida
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstadoArranque.Cargando)

    init {
        viewModelScope.launch { repositorio.asegurarPerfil() }
    }
}

enum class EstadoArranque { Cargando, NecesitaBienvenida, Listo }
