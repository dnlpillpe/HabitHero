package com.kidslab.habithero.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kidslab.habithero.data.local.entity.DesafioDiario
import com.kidslab.habithero.domain.Categoria
import com.kidslab.habithero.domain.GeneradorDesafios
import com.kidslab.habithero.ui.FabricaViewModels
import com.kidslab.habithero.ui.components.AvatarConMarco
import com.kidslab.habithero.ui.components.BarraNivel
import com.kidslab.habithero.ui.components.EstadoVacio
import com.kidslab.habithero.ui.components.TarjetaHabito
import com.kidslab.habithero.ui.theme.AzulHeroe
import com.kidslab.habithero.ui.theme.AzulProfundo
import com.kidslab.habithero.ui.theme.MentaLogro
import com.kidslab.habithero.util.FechasEs
import com.kidslab.habithero.util.TiendaCatalogo

/** Pantalla 2 de 7: los hábitos de hoy. */
@Composable
fun PantallaInicio(
    alCrearHabito: () -> Unit,
    alEditarHabito: (Long) -> Unit,
    alVerProgreso: () -> Unit,
    alVerInsignias: () -> Unit,
    viewModel: InicioViewModel = viewModel(factory = FabricaViewModels.Factory)
) {
    val estado by viewModel.estado.collectAsState()
    val celebracion by viewModel.celebracion.collectAsState()
    val aDesmarcar by viewModel.confirmarDesmarcar.collectAsState()

    LaunchedEffect(Unit) { viewModel.refrescarDia() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = alCrearHabito,
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Nuevo hábito", style = MaterialTheme.typography.labelLarge)
            }
        }
    ) { relleno ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                CabeceraHeroe(
                    avatar = estado.avatar,
                    marco = estado.marco,
                    nombre = estado.nombre,
                    nivel = estado.nivel,
                    monedas = estado.monedas,
                    progreso = estado.progresoNivel,
                    fecha = FechasEs.fechaLarga(estado.fecha),
                    completados = estado.completadosHoy,
                    total = estado.totalHoy,
                    alVerProgreso = alVerProgreso,
                    alVerInsignias = alVerInsignias
                )
            }

            estado.desafioHoy?.let { desafio ->
                item {
                    TarjetaDesafio(desafio = desafio, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            item {
                FiltroCategorias(
                    seleccionada = estado.categoriaFiltro,
                    alSeleccionar = viewModel::filtrarCategoria
                )
            }

            if (estado.habitosHoy.isEmpty() && !estado.cargando) {
                item {
                    EstadoVacio(
                        icono = "🗓️",
                        titulo = "Hoy no hay misiones",
                        detalle = if (estado.habitosOtrosDias > 0) {
                            "Tus hábitos están programados para otros días. Puedes crear uno nuevo para hoy."
                        } else {
                            "Crea tu primer hábito con el botón de abajo."
                        }
                    )
                }
            }

            items(items = estado.habitosHoy, key = { it.habito.id }) { item ->
                TarjetaHabito(
                    nombre = item.habito.nombre,
                    icono = item.habito.icono,
                    colorIndex = item.habito.colorIndex,
                    marcado = item.marcado,
                    racha = item.racha,
                    textoDias = FechasEs.textoDias(item.habito.diasSemana),
                    alPulsar = { viewModel.pulsarHabito(item) },
                    alEditar = { alEditarHabito(item.habito.id) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    categoria = item.habito.categoriaEnum(),
                    tieneRecordatorio = item.habito.horaRecordatorioMinutos != null
                )
            }

            if (estado.habitosHoy.isNotEmpty()) {
                item {
                    Text(
                        text = "Toca para marcar. Mantén pulsado un hábito para editarlo.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    aDesmarcar?.let { habito ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarDesmarcado,
            title = { Text("¿Quitar la marca?") },
            text = {
                Text("“${habito.nombre}” volverá a estar pendiente hoy. Los puntos que dio se devolverán.")
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmarDesmarcado) { Text("Sí, quitar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarDesmarcado) { Text("Dejarlo marcado") }
            }
        )
    }

    celebracion?.let { fiesta ->
        DialogoCelebracion(celebracion = fiesta, alCerrar = viewModel::cerrarCelebracion)
    }
}

@Composable
private fun CabeceraHeroe(
    avatar: String,
    marco: TiendaCatalogo.ItemTienda?,
    nombre: String,
    nivel: Int,
    monedas: Int,
    progreso: Float,
    fecha: String,
    completados: Int,
    total: Int,
    alVerProgreso: () -> Unit,
    alVerInsignias: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(AzulHeroe)
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarConMarco(avatar = avatar, nivel = nivel, marco = marco)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (nombre.isBlank()) "¡Hola!" else "¡Hola, $nombre!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = fecha,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Nivel $nivel",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
                Spacer(Modifier.width(10.dp))
                BarraNivel(progreso = progreso, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "🪙 $monedas",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AccesoRapido(
                    texto = "$completados de $total hoy",
                    icono = "✅",
                    alPulsar = alVerProgreso,
                    modifier = Modifier.weight(1f)
                )
                AccesoRapido(
                    texto = "Mis premios",
                    icono = "🏅",
                    alPulsar = alVerInsignias,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FiltroCategorias(
    seleccionada: Categoria?,
    alSeleccionar: (Categoria?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = seleccionada == null,
                onClick = { alSeleccionar(null) },
                label = { Text("Todas") }
            )
        }
        items(items = Categoria.entries.toList(), key = { it.name }) { categoria ->
            FilterChip(
                selected = seleccionada == categoria,
                onClick = { alSeleccionar(if (seleccionada == categoria) null else categoria) },
                label = { Text("${categoria.icono} ${categoria.etiqueta}") }
            )
        }
    }
}

@Composable
private fun TarjetaDesafio(desafio: DesafioDiario, modifier: Modifier = Modifier) {
    val (icono, descripcion) = when (desafio.tipo) {
        GeneradorDesafios.TIPO_TRES_HABITOS -> "🎯" to "Desafío de hoy: marca ${desafio.meta} hábitos"
        GeneradorDesafios.TIPO_TODOS_HOY -> "🏁" to "Desafío de hoy: completa todos tus hábitos"
        GeneradorDesafios.TIPO_ANTES_DE_HORA -> "⏰" to "Desafío de hoy: marca alguno antes de las 20:00"
        else -> "✨" to "Desafío sorpresa de hoy"
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (desafio.completado) MentaLogro.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = if (desafio.completado) "✅" else icono, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = descripcion, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = if (desafio.completado) {
                        "¡Completado! +${desafio.recompensaMonedas} 🪙 +${desafio.recompensaExperiencia} ⭐"
                    } else {
                        "Recompensa: +${desafio.recompensaMonedas} 🪙 +${desafio.recompensaExperiencia} ⭐"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AccesoRapido(
    texto: String,
    icono: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f)),
        onClick = alPulsar
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icono, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun DialogoCelebracion(celebracion: Celebracion, alCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = alCerrar,
        icon = { Text(text = if (celebracion.subioDeNivel) "🚀" else "🎉", fontSize = 40.sp) },
        title = {
            Text(
                text = if (celebracion.subioDeNivel) {
                    "¡Nivel ${celebracion.nivelNuevo}!"
                } else {
                    celebracion.mensaje
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🪙 +${celebracion.monedas}    ⭐ +${celebracion.experiencia}",
                    style = MaterialTheme.typography.titleMedium
                )
                if (celebracion.racha > 1) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "🔥 ${celebracion.racha} días seguidos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (celebracion.insignias.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "¡Nueva insignia!",
                        style = MaterialTheme.typography.titleMedium,
                        color = AzulProfundo
                    )
                    Spacer(Modifier.height(6.dp))
                    celebracion.insignias.forEach { insignia ->
                        Text(
                            text = "${insignia.icono}  ${insignia.nombre}",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = alCerrar) { Text("¡Genial!") }
        }
    )
}
