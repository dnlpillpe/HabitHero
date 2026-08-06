package com.kidslab.habithero.ui.screens.badges

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kidslab.habithero.ui.FabricaViewModels
import com.kidslab.habithero.ui.components.BarraNivel
import com.kidslab.habithero.ui.components.Pastilla
import com.kidslab.habithero.ui.theme.MentaLogro
import com.kidslab.habithero.ui.theme.OroMedalla
import com.kidslab.habithero.util.FechasEs

/** Pantalla 5 de 6: insignias y recompensas. */
@Composable
fun PantallaInsignias(
    viewModel: InsigniasViewModel = viewModel(factory = FabricaViewModels.Factory)
) {
    val estado by viewModel.estado.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "Tus premios", style = MaterialTheme.typography.headlineMedium)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Pastilla(
                    icono = "🪙",
                    valor = "${estado.monedas}",
                    etiqueta = "monedas",
                    modifier = Modifier.weight(1f)
                )
                Pastilla(
                    icono = "⭐",
                    valor = "${estado.nivel}",
                    etiqueta = "nivel",
                    modifier = Modifier.weight(1f)
                )
                Pastilla(
                    icono = "🏅",
                    valor = "${estado.conseguidas}/${estado.total}",
                    etiqueta = "insignias",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        items(items = estado.insignias, key = { it.insignia.id }) { item ->
            TarjetaInsignia(item)
        }

        item {
            Text(
                text = "Las insignias no se pierden nunca. Una vez conseguidas, son tuyas para siempre.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

@Composable
private fun TarjetaInsignia(item: InsigniaUi) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (item.conseguida) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (item.conseguida) OroMedalla
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (item.conseguida) item.insignia.icono else "🔒",
                    fontSize = 26.sp,
                    modifier = Modifier.alpha(if (item.conseguida) 1f else 0.7f)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.insignia.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = item.insignia.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                if (item.conseguida) {
                    Text(
                        text = item.fecha?.let { "Conseguida el ${FechasEs.fechaLarga(it)}" }
                            ?: "¡Conseguida!",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    BarraNivel(
                        progreso = item.progreso,
                        alto = 10,
                        colorRelleno = MentaLogro,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${(item.progreso * 100).toInt()}% del camino",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
