package com.kidslab.habithero.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kidslab.habithero.domain.Categoria
import com.kidslab.habithero.ui.theme.OroMedalla
import com.kidslab.habithero.ui.theme.Superficie
import com.kidslab.habithero.ui.theme.colorHabito
import com.kidslab.habithero.util.FechasEs
import com.kidslab.habithero.util.TiendaCatalogo

/**
 * Tarjeta grande de hábito: toda la superficie marca o desmarca.
 * Mantener pulsado abre el editor de ese hábito.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TarjetaHabito(
    nombre: String,
    icono: String,
    colorIndex: Int,
    marcado: Boolean,
    racha: Int,
    textoDias: String,
    alPulsar: () -> Unit,
    alEditar: () -> Unit,
    modifier: Modifier = Modifier,
    categoria: Categoria? = null,
    tieneRecordatorio: Boolean = false
) {
    val color = colorHabito(colorIndex)
    val fondo = if (marcado) color else Superficie

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                role = Role.Button,
                onClick = alPulsar,
                onLongClick = alEditar
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = fondo),
        elevation = CardDefaults.cardElevation(defaultElevation = if (marcado) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (marcado) Color.White.copy(alpha = 0.22f) else color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icono, fontSize = 28.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (marcado) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                val sufijoRecordatorio = if (tieneRecordatorio) " 🔔" else ""
                Text(
                    text = (if (racha > 0) "🔥 $racha  ·  $textoDias" else textoDias) + sufijoRecordatorio,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (marcado) Color.White.copy(alpha = 0.85f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (categoria != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${categoria.icono} ${categoria.etiqueta}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (marcado) Color.White.copy(alpha = 0.75f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (marcado) Color.White else color.copy(alpha = 0.12f))
                    .clickable(role = Role.Checkbox, onClick = alPulsar),
                contentAlignment = Alignment.Center
            ) {
                if (marcado) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Quitar la marca de hoy",
                        tint = color
                    )
                } else {
                    Text(text = " ", fontSize = 20.sp)
                }
            }
        }
    }
}

/** Selector de días de la semana con chips redondos (L M X J V S D). */
@Composable
fun SelectorDias(
    seleccionados: Set<Int>,
    alCambiar: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        (1..7).forEach { dia ->
            val activo = dia in seleccionados
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (activo) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable(role = Role.Checkbox) {
                        val nuevo = seleccionados.toMutableSet()
                        if (activo) nuevo.remove(dia) else nuevo.add(dia)
                        alCambiar(nuevo)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = FechasEs.diaCorto(dia),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (activo) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Rejilla de emojis para elegir icono o avatar. */
@Composable
fun SelectorEmoji(
    opciones: List<String>,
    seleccionado: String,
    tamano: Int = 52,
    porFila: Int = 6,
    alSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        opciones.chunked(porFila).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                fila.forEach { emoji ->
                    val activo = emoji == seleccionado
                    Box(
                        modifier = Modifier
                            .size(tamano.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (activo) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                width = if (activo) 3.dp else 1.dp,
                                color = if (activo) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable(role = Role.RadioButton) { alSeleccionar(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = (tamano / 2).sp)
                    }
                }
                repeat(porFila - fila.size) { Spacer(Modifier.size(tamano.dp)) }
            }
        }
    }
}

/** Barra de progreso hacia el siguiente nivel. */
@Composable
fun BarraNivel(
    progreso: Float,
    modifier: Modifier = Modifier,
    alto: Int = 14,
    colorRelleno: Color = OroMedalla
) {
    val animado by animateFloatAsState(
        targetValue = progreso.coerceIn(0f, 1f),
        label = "progresoNivel"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alto.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.30f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animado)
                .height(alto.dp)
                .clip(CircleShape)
                .background(colorRelleno)
        )
    }
}

/** Pastilla con icono y número: monedas, nivel, racha. */
@Composable
fun Pastilla(
    icono: String,
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier,
    fondo: Color = MaterialTheme.colorScheme.surface,
    tinta: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = fondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icono, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(text = valor, style = MaterialTheme.typography.titleLarge, color = tinta)
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = tinta.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Avatar del héroe con su marco de tienda (si tiene uno equipado) y pequeños
 * adornos según el nivel alcanzado: un avatar que "evoluciona" sin necesitar
 * ninguna imagen nueva, solo emoji y color.
 */
@Composable
fun AvatarConMarco(
    avatar: String,
    nivel: Int,
    marco: TiendaCatalogo.ItemTienda?,
    modifier: Modifier = Modifier,
    tamano: Int = 58
) {
    Box(modifier = modifier.size((tamano + 20).dp), contentAlignment = Alignment.Center) {
        val bordeMarco = Modifier
            .size(tamano.dp)
            .clip(CircleShape)
            .background(Superficie)
        Box(
            modifier = if (marco != null) {
                bordeMarco.border(3.dp, colorHabito(marco.colorIndex), CircleShape)
            } else {
                bordeMarco
            },
            contentAlignment = Alignment.Center
        ) {
            Text(text = avatar, fontSize = (tamano / 2).sp)
        }

        if (nivel >= 10) {
            Text(text = "👑", fontSize = (tamano / 3).sp, modifier = Modifier.align(Alignment.TopCenter))
        }
        if (nivel >= 5) {
            Text(text = "⭐", fontSize = (tamano / 4).sp, modifier = Modifier.align(Alignment.BottomEnd))
        }
    }
}

/** Estado vacío amable: nunca un reproche, siempre una invitación. */
@Composable
fun EstadoVacio(
    icono: String,
    titulo: String,
    detalle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icono, fontSize = 54.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = detalle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
