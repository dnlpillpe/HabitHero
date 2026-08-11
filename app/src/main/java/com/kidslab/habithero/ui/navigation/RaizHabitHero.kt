package com.kidslab.habithero.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kidslab.habithero.ui.EstadoArranque
import com.kidslab.habithero.ui.FabricaViewModels
import com.kidslab.habithero.ui.RaizViewModel
import com.kidslab.habithero.ui.screens.badges.PantallaInsignias
import com.kidslab.habithero.ui.screens.habitedit.PantallaEditorHabito
import com.kidslab.habithero.ui.screens.home.PantallaInicio
import com.kidslab.habithero.ui.screens.progress.PantallaProgreso
import com.kidslab.habithero.ui.screens.settings.PantallaConfiguracion
import com.kidslab.habithero.ui.screens.shop.PantallaTienda
import com.kidslab.habithero.ui.screens.welcome.PantallaBienvenida

private data class Destino(val ruta: String, val etiqueta: String, val icono: ImageVector)

private val DESTINOS = listOf(
    Destino(Rutas.INICIO, "Hoy", Icons.Filled.Home),
    Destino(Rutas.PROGRESO, "Semana", Icons.Filled.DateRange),
    Destino(Rutas.INSIGNIAS, "Premios", Icons.Filled.Star),
    Destino(Rutas.TIENDA, "Tienda", Icons.Filled.ShoppingCart),
    Destino(Rutas.CONFIGURACION, "Ajustes", Icons.Filled.Settings)
)

@Composable
fun RaizHabitHero() {
    val raizViewModel: RaizViewModel = viewModel(factory = FabricaViewModels.Factory)
    val arranque by raizViewModel.estado.collectAsState()

    var destinoInicial by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(arranque) {
        if (destinoInicial == null && arranque != EstadoArranque.Cargando) {
            destinoInicial = if (arranque == EstadoArranque.NecesitaBienvenida) {
                Rutas.BIENVENIDA
            } else {
                Rutas.INICIO
            }
        }
    }

    val inicio = destinoInicial
    if (inicio == null) {
        PantallaCargando()
    } else {
        val navController = rememberNavController()
        GrafoHabitHero(navController = navController, destinoInicial = inicio)
    }
}

@Composable
private fun PantallaCargando() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "🦸", style = MaterialTheme.typography.displaySmall)
    }
}

@Composable
private fun GrafoHabitHero(navController: NavHostController, destinoInicial: String) {
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val mostrarBarra = rutaActual in Rutas.CON_BARRA

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    DESTINOS.forEach { destino ->
                        NavigationBarItem(
                            selected = rutaActual == destino.ruta,
                            onClick = {
                                if (rutaActual != destino.ruta) {
                                    navController.navigate(destino.ruta) {
                                        popUpTo(Rutas.INICIO) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(destino.icono, contentDescription = null) },
                            label = { Text(destino.etiqueta) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { relleno ->
        NavHost(
            navController = navController,
            startDestination = destinoInicial,
            modifier = Modifier.padding(relleno)
        ) {
            composable(Rutas.BIENVENIDA) {
                PantallaBienvenida(
                    alTerminar = {
                        navController.navigate(Rutas.INICIO) {
                            popUpTo(Rutas.BIENVENIDA) { inclusive = true }
                        }
                    }
                )
            }

            composable(Rutas.INICIO) {
                PantallaInicio(
                    alCrearHabito = { navController.navigate(Rutas.editor(null)) },
                    alEditarHabito = { id -> navController.navigate(Rutas.editor(id)) },
                    alVerProgreso = { navController.navigate(Rutas.PROGRESO) },
                    alVerInsignias = { navController.navigate(Rutas.INSIGNIAS) }
                )
            }

            composable(
                route = Rutas.EDITOR_PATRON,
                arguments = listOf(
                    navArgument(Rutas.ARG_HABITO) {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { entrada ->
                val habitId = entrada.arguments?.getLong(Rutas.ARG_HABITO) ?: 0L
                PantallaEditorHabito(
                    habitId = habitId,
                    alVolver = { navController.popBackStack() }
                )
            }

            composable(Rutas.PROGRESO) {
                PantallaProgreso()
            }

            composable(Rutas.INSIGNIAS) {
                PantallaInsignias()
            }

            composable(Rutas.TIENDA) {
                PantallaTienda()
            }

            composable(Rutas.CONFIGURACION) {
                PantallaConfiguracion(
                    alReiniciar = {
                        navController.navigate(Rutas.INICIO) {
                            popUpTo(Rutas.INICIO) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
