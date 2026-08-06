package com.kidslab.habithero.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.kidslab.habithero.HabitHeroApp
import com.kidslab.habithero.data.repository.HabitHeroRepository
import com.kidslab.habithero.ui.screens.badges.InsigniasViewModel
import com.kidslab.habithero.ui.screens.habitedit.EditorHabitoViewModel
import com.kidslab.habithero.ui.screens.home.InicioViewModel
import com.kidslab.habithero.ui.screens.progress.ProgresoViewModel
import com.kidslab.habithero.ui.screens.settings.ConfiguracionViewModel
import com.kidslab.habithero.ui.screens.welcome.BienvenidaViewModel

/** Fábrica única para todos los ViewModel de la app. */
object FabricaViewModels {

    val Factory = viewModelFactory {
        initializer { RaizViewModel(repositorio()) }
        initializer { BienvenidaViewModel(repositorio()) }
        initializer { InicioViewModel(repositorio()) }
        initializer { EditorHabitoViewModel(repositorio()) }
        initializer { ProgresoViewModel(repositorio()) }
        initializer { InsigniasViewModel(repositorio()) }
        initializer { ConfiguracionViewModel(repositorio()) }
    }
}

private fun CreationExtras.repositorio(): HabitHeroRepository =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HabitHeroApp).repositorio
