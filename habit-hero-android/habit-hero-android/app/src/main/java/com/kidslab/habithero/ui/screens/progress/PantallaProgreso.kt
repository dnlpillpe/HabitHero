package com.kidslab.habithero.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kidslab.habithero.ui.FabricaViewModels
import com.kidslab.habithero.ui.components.BarraNivel
import com.kidslab.habithero.ui.components.EstadoVacio
import com.kidslab.habithero.ui.components.Pastilla
import com.kidslab.habithero.ui.theme.MentaLogro
import com.kidslab.habithero.ui.theme.colorHabito
import com.kidslab.habithero.util.FechasEs

/** Pantalla 4 de 6: resumen visual de los últimos siete días. */
@Composable
fun PantallaProgreso(
    viewModel: ProgresoViewModel = viewModel(factory = FabricaViewModels.Factory)
) {
    val estado by viewModel.estado.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Tu semana",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Pastilla(
                    icono = "✅",
                    valor = "${estado.marcasSemana}",
                    etiqueta = "marcas en 7 días",
                    modifier = Modifier.weight(1f)
                )
                Pastilla(
                    icono = "🔥",
                    valor = "${estado.mejorRachaGlobal}",
                    etiqueta = "mejor racha",
                    modifier = Modifier.weight(1f)
                )
                Pastilla(
                    icono = "🎯",
                    valor = "${(estado.porcentajeSemana * 100).toInt()}%",
                    etiqueta = "de lo previsto",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Cada día cuenta, y los días que fallas no restan nada.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    BarraNivel(
                        progreso = estado.porcentajeSemana,
                        alto = 12,
                        colorRelleno = MentaLogro,
                        modifier = Modifier.background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                    )
                }
            }
        }

        if (estado.filas.isEmpty() && !estado.cargando) {
            item {
                EstadoVacio(
                    icono = "📊",
                    titulo = "Todavía no hay nada que mostrar",
                    detalle = "Cuando crees hábitos y los marques, tu semana aparecerá aquí."
                )
            }
        }

        if (estado.filas.isNotEmpty()) {
            item { EncabezadoDias(estado) }
        }

        items(items = estado.filas, key = { it.habito.id }) { fila ->
            FilaHabito(fila)
        }
    }
}

@Composable
private fun EncabezadoDias(estado: EstadoProgreso) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(132.dp))
        estado.fechas.forEach { fecha ->
            Text(
                text = FechasEs.diaCorto(fecha.dayOfWeek.value),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FilaHabito(fila: FilaProgreso) {
    val color = colorHabito(fila.habito.colorIndex)

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.width(120.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = fila.habito.icono, fontSize = 18.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = fila.habito.nombre,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "🔥 ${fila.rachaActual}  ·  mejor ${fila.mejorRacha}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            fila.dias.forEach { dia ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    when (dia) {
                        EstadoDia.MARCADO -> Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "✓", color = Color.White, fontSize = 14.sp)
                        }

                        EstadoDia.PENDIENTE -> Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )

                        EstadoDia.NO_TOCABA -> Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}
