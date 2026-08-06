package com.kidslab.habithero.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val EsquemaClaro = lightColorScheme(
    primary = AzulHeroe,
    onPrimary = Superficie,
    primaryContainer = AzulSuave,
    onPrimaryContainer = AzulProfundo,
    secondary = OroMedalla,
    onSecondary = Tinta,
    secondaryContainer = OroSuave,
    onSecondaryContainer = Tinta,
    tertiary = RosaEnergia,
    onTertiary = Superficie,
    tertiaryContainer = RosaSuave,
    onTertiaryContainer = Tinta,
    background = Fondo,
    onBackground = Tinta,
    surface = Superficie,
    onSurface = Tinta,
    surfaceVariant = AzulSuave,
    onSurfaceVariant = TintaSuave,
    outline = Borde,
    outlineVariant = Borde,
    error = Color_Error,
    onError = Superficie
)

private val FormasHabitHero = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun HabitHeroTheme(content: @Composable () -> Unit) {
    // Un único esquema claro: los colores son parte de la identidad de la app
    // y no deben cambiar con el tema del sistema.
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = TipografiaHabitHero,
        shapes = FormasHabitHero,
        content = content
    )
}
