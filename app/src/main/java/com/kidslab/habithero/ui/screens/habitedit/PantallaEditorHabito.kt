package com.kidslab.habithero.ui.screens.habitedit

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kidslab.habithero.domain.Categoria
import com.kidslab.habithero.ui.FabricaViewModels
import com.kidslab.habithero.ui.components.SelectorDias
import com.kidslab.habithero.ui.components.SelectorEmoji
import com.kidslab.habithero.ui.theme.ColoresHabito
import com.kidslab.habithero.ui.theme.colorHabito
import com.kidslab.habithero.util.Catalogos
import com.kidslab.habithero.util.FechasEs

/** Pantalla 3 de 7: crear o editar un hábito. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditorHabito(
    habitId: Long,
    alVolver: () -> Unit,
    viewModel: EditorHabitoViewModel = viewModel(factory = FabricaViewModels.Factory)
) {
    val estado by viewModel.estado.collectAsState()
    var pedirBorrado by remember { mutableStateOf(false) }

    val contexto = LocalContext.current
    var permisoNotificacionesConcedido by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido -> permisoNotificacionesConcedido = concedido }

    LaunchedEffect(habitId) { viewModel.cargar(habitId) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(if (estado.esNuevo) "Nuevo hábito" else "Editar hábito")
                },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (!estado.esNuevo) {
                        IconButton(onClick = { pedirBorrado = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Borrar hábito")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            VistaPrevia(icono = estado.icono, nombre = estado.nombre, colorIndex = estado.colorIndex)

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = estado.nombre,
                onValueChange = viewModel::cambiarNombre,
                label = { Text("Nombre del hábito") },
                placeholder = { Text("Ej.: Regar las plantas") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                supportingText = {
                    Text("Máximo 30 letras · te quedan ${estado.caracteresRestantes}")
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))
            Titulo("Elige un icono")
            SelectorEmoji(
                opciones = Catalogos.ICONOS_HABITO,
                seleccionado = estado.icono,
                tamano = 50,
                porFila = 6,
                alSeleccionar = viewModel::cambiarIcono
            )

            Spacer(Modifier.height(18.dp))
            Titulo("Elige un color")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ColoresHabito.forEachIndexed { indice, color ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (indice == estado.colorIndex) 4.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            )
                            .clickable { viewModel.cambiarColor(indice) }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Titulo("¿Qué días?")
            SelectorDias(
                seleccionados = estado.dias,
                alCambiar = viewModel::cambiarDias
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { viewModel.aplicarAtajo(setOf(1, 2, 3, 4, 5, 6, 7)) },
                    label = { Text("Todos") }
                )
                AssistChip(
                    onClick = { viewModel.aplicarAtajo(setOf(1, 2, 3, 4, 5)) },
                    label = { Text("L a V") }
                )
                AssistChip(
                    onClick = { viewModel.aplicarAtajo(setOf(6, 7)) },
                    label = { Text("Finde") }
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = FechasEs.textoDias(estado.dias.toList()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))
            Titulo("Categoría")
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Categoria.entries.forEach { categoria ->
                    FilterChip(
                        selected = estado.categoria == categoria,
                        onClick = { viewModel.cambiarCategoria(categoria) },
                        label = { Text("${categoria.icono} ${categoria.etiqueta}") }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Titulo("Recordatorio (opcional)")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = estado.recordatorioActivo,
                    onCheckedChange = { activo ->
                        viewModel.activarRecordatorio(activo)
                        if (activo && !permisoNotificacionesConcedido && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            lanzadorPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (estado.recordatorioActivo) "Te avisamos a esta hora" else "Sin recordatorio",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (estado.recordatorioActivo) {
                Spacer(Modifier.height(10.dp))
                SelectorHora(
                    minutos = estado.horaRecordatorioMinutos ?: EstadoEditor.MINUTOS_POR_DEFECTO,
                    alCambiar = viewModel::cambiarHoraRecordatorio
                )
                if (!permisoNotificacionesConcedido) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Activa las notificaciones en Ajustes del sistema para recibir el aviso.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            Button(
                onClick = { viewModel.guardar(alVolver) },
                enabled = estado.puedeGuardar,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            ) {
                Text(
                    text = if (estado.esNuevo) "Crear hábito" else "Guardar cambios",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (!estado.puedeGuardar) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (!estado.nombreValido) {
                        "Escribe un nombre para tu hábito."
                    } else {
                        "Elige al menos un día de la semana."
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (pedirBorrado) {
        AlertDialog(
            onDismissRequest = { pedirBorrado = false },
            title = { Text("¿Borrar este hábito?") },
            text = { Text("Se borrarán también sus marcas. Tus monedas y tus insignias no se tocan.") },
            confirmButton = {
                TextButton(onClick = {
                    pedirBorrado = false
                    viewModel.eliminar(alVolver)
                }) { Text("Sí, borrar") }
            },
            dismissButton = {
                TextButton(onClick = { pedirBorrado = false }) { Text("Cancelar") }
            }
        )
    }
}

/** Selector de hora hecho a mano: dos ruedas de +/- para horas y minutos (pasos de 5). */
@Composable
private fun SelectorHora(minutos: Int, alCambiar: (hora: Int, minuto: Int) -> Unit) {
    val hora = minutos / 60
    val minuto = minutos % 60
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        RuedaHora(etiqueta = hora.toString().padStart(2, '0')) { delta ->
            alCambiar(Math.floorMod(hora + delta, 24), minuto)
        }
        Text(":", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(horizontal = 6.dp))
        RuedaHora(etiqueta = minuto.toString().padStart(2, '0')) { delta ->
            alCambiar(hora, Math.floorMod(minuto + delta * 5, 60))
        }
    }
}

@Composable
private fun RuedaHora(etiqueta: String, alCambiar: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BotonPaso(simbolo = "+") { alCambiar(1) }
        Text(text = etiqueta, style = MaterialTheme.typography.headlineSmall)
        BotonPaso(simbolo = "−") { alCambiar(-1) }
    }
}

@Composable
private fun BotonPaso(simbolo: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = simbolo, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun VistaPrevia(icono: String, nombre: String, colorIndex: Int) {
    val color = colorHabito(colorIndex)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(color)
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.24f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icono, fontSize = 28.sp)
        }
        Spacer(Modifier.size(14.dp))
        Text(
            text = nombre.ifBlank { "Tu nuevo hábito" },
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
    }
}
