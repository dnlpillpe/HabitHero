package com.kidslab.habithero.ui.screens.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kidslab.habithero.ui.FabricaViewModels
import com.kidslab.habithero.ui.components.AvatarConMarco
import com.kidslab.habithero.ui.components.Pastilla
import com.kidslab.habithero.ui.components.SelectorEmoji
import com.kidslab.habithero.ui.theme.Color_Error
import com.kidslab.habithero.util.TiendaCatalogo

/** Pantalla 7 de 7: ajustes del héroe y reinicio de datos. */
@Composable
fun PantallaConfiguracion(
    alReiniciar: () -> Unit,
    viewModel: ConfiguracionViewModel = viewModel(factory = FabricaViewModels.Factory)
) {
    val estado by viewModel.estado.collectAsState()
    val reiniciando by viewModel.reiniciando.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf("🦸") }
    var iniciado by remember { mutableStateOf(false) }
    var pedirReinicio by remember { mutableStateOf(false) }
    var confirmarReinicio by remember { mutableStateOf(false) }

    LaunchedEffect(estado.nombre, estado.avatar) {
        if (!iniciado) {
            nombre = estado.nombre
            avatar = estado.avatar
            iniciado = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Text(text = "Ajustes", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Pastilla(
                icono = "📋",
                valor = "${estado.totalHabitos}",
                etiqueta = "hábitos",
                modifier = Modifier.weight(1f)
            )
            Pastilla(
                icono = "✅",
                valor = "${estado.totalMarcas}",
                etiqueta = "marcas",
                modifier = Modifier.weight(1f)
            )
            Pastilla(
                icono = "🪙",
                valor = "${estado.monedas}",
                etiqueta = "monedas",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(22.dp))
        Text(text = "Tu héroe", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it.take(20) },
            label = { Text("Nombre") },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))
        SelectorEmoji(
            opciones = estado.avataresDisponibles,
            seleccionado = avatar,
            tamano = 50,
            porFila = 6,
            alSeleccionar = { avatar = it }
        )

        if (estado.marcosDesbloqueados.isNotEmpty()) {
            Spacer(Modifier.height(22.dp))
            Text(text = "Marco del avatar", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OpcionMarco(
                    avatar = avatar,
                    marco = null,
                    seleccionado = estado.marcoSeleccionado == null,
                    alSeleccionar = { viewModel.seleccionarMarco(null) }
                )
                estado.marcosDesbloqueados.forEach { marco ->
                    OpcionMarco(
                        avatar = avatar,
                        marco = marco,
                        seleccionado = estado.marcoSeleccionado == marco.id,
                        alSeleccionar = { viewModel.seleccionarMarco(marco.id) }
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { viewModel.guardarPerfil(nombre, avatar) },
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text("Guardar cambios", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(28.dp))
        Text(text = "Empezar de cero", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Borra hábitos, marcas, monedas e insignias, y deja la app como recién instalada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { pedirReinicio = true },
                    enabled = !reiniciando,
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color_Error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (reiniciando) "Reiniciando…" else "Reiniciar todos los datos",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Text(text = "Sobre HabitHero", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Versión 1.0.0\n" +
                "Funciona sin internet: no pide permisos, no tiene cuentas, " +
                "no muestra publicidad y no envía datos a ningún sitio. " +
                "Todo se guarda solo en este dispositivo.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
    }

    if (pedirReinicio) {
        AlertDialog(
            onDismissRequest = { pedirReinicio = false },
            title = { Text("¿Reiniciar todo?") },
            text = { Text("Se borrarán tus hábitos, tus marcas, tus monedas y tus insignias. Esto no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    pedirReinicio = false
                    confirmarReinicio = true
                }) { Text("Continuar") }
            },
            dismissButton = {
                TextButton(onClick = { pedirReinicio = false }) { Text("Cancelar") }
            }
        )
    }

    if (confirmarReinicio) {
        AlertDialog(
            onDismissRequest = { confirmarReinicio = false },
            title = { Text("Última confirmación") },
            text = { Text("Toca “Sí, borrar todo” para empezar de cero.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarReinicio = false
                    iniciado = false
                    viewModel.reiniciarTodo(alReiniciar)
                }) { Text("Sí, borrar todo") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarReinicio = false }) { Text("Mejor no") }
            }
        )
    }
}

@Composable
private fun OpcionMarco(
    avatar: String,
    marco: TiendaCatalogo.ItemTienda?,
    seleccionado: Boolean,
    alSeleccionar: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .border(
                width = if (seleccionado) 3.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clickable(onClick = alSeleccionar)
            .padding(4.dp)
    ) {
        AvatarConMarco(avatar = avatar, nivel = 0, marco = marco, tamano = 48)
    }
}
