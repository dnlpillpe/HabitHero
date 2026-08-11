package com.kidslab.habithero.ui.screens.shop

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kidslab.habithero.ui.FabricaViewModels
import com.kidslab.habithero.ui.components.EstadoVacio
import com.kidslab.habithero.ui.components.Pastilla
import com.kidslab.habithero.ui.theme.MentaLogro
import kotlinx.coroutines.delay

/** Pantalla 5 de 7: gastar las monedas ganadas en avatares y marcos nuevos. */
@Composable
fun PantallaTienda(
    viewModel: TiendaViewModel = viewModel(factory = FabricaViewModels.Factory)
) {
    val estado by viewModel.estado.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Tienda", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                Pastilla(icono = "🪙", valor = "${estado.monedas}", etiqueta = "monedas")
            }
        }

        item {
            Text(
                text = "Usa tus monedas para desbloquear avatares y marcos nuevos para tu héroe.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (estado.avatares.isNotEmpty()) {
            item { Text(text = "Avatares", style = MaterialTheme.typography.titleLarge) }
            items(items = estado.avatares, key = { it.item.id }) { itemUi ->
                TarjetaItemTienda(itemUi = itemUi, monedas = estado.monedas, alComprar = { viewModel.comprar(itemUi.item.id) })
            }
        }

        if (estado.marcos.isNotEmpty()) {
            item { Text(text = "Marcos", style = MaterialTheme.typography.titleLarge) }
            items(items = estado.marcos, key = { it.item.id }) { itemUi ->
                TarjetaItemTienda(itemUi = itemUi, monedas = estado.monedas, alComprar = { viewModel.comprar(itemUi.item.id) })
            }
        }

        if (estado.avatares.isEmpty() && estado.marcos.isEmpty() && !estado.cargando) {
            item {
                EstadoVacio(
                    icono = "🛍️",
                    titulo = "La tienda está vacía por ahora",
                    detalle = "Vuelve pronto por más sorpresas."
                )
            }
        }

        mensaje?.let { texto ->
            item {
                LaunchedEffect(texto) {
                    delay(2500)
                    viewModel.cerrarMensaje()
                }
                Snackbar { Text(texto) }
            }
        }
    }
}

@Composable
private fun TarjetaItemTienda(
    itemUi: ItemTiendaUi,
    monedas: Int,
    alComprar: () -> Unit
) {
    val item = itemUi.item
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.emoji, fontSize = 24.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (itemUi.desbloqueado) "Ya lo tienes" else "🪙 ${item.precio}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (itemUi.desbloqueado) MentaLogro else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(10.dp))
            if (!itemUi.desbloqueado) {
                Button(
                    onClick = alComprar,
                    enabled = monedas >= item.precio,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text(text = "Comprar", textAlign = TextAlign.Center)
                }
            } else {
                Text(text = "✅", fontSize = 22.sp)
            }
        }
    }
}
