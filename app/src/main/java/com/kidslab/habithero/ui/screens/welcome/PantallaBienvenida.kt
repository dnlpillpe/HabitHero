package com.kidslab.habithero.ui.screens.welcome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kidslab.habithero.ui.FabricaViewModels
import com.kidslab.habithero.ui.components.SelectorEmoji
import com.kidslab.habithero.util.Catalogos

/** Pantalla 1 de 7: bienvenida breve y elección de avatar. */
@Composable
fun PantallaBienvenida(
    alTerminar: () -> Unit,
    viewModel: BienvenidaViewModel = viewModel(factory = FabricaViewModels.Factory)
) {
    val estado by viewModel.estado.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = estado.avatar, fontSize = 76.sp)
        Spacer(Modifier.height(10.dp))

        Text(
            text = "¡Hola, héroe!",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Elige tu avatar y empieza a completar misiones cada día.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Tu avatar",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        SelectorEmoji(
            opciones = Catalogos.AVATARES,
            seleccionado = estado.avatar,
            tamano = 56,
            porFila = 4,
            alSeleccionar = viewModel::elegirAvatar
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = estado.nombre,
            onValueChange = viewModel::cambiarNombre,
            label = { Text("¿Cómo te llamas?") },
            placeholder = { Text("Tu nombre") },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Puedes dejarlo vacío y cambiarlo luego en Ajustes.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp)
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = { viewModel.empezar(alTerminar) },
            enabled = !estado.guardando,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text(text = "¡Empezar!", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = "HabitHero funciona sin internet y sin cuentas.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
