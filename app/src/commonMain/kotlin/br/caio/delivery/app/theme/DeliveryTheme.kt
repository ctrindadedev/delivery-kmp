package br.caio.delivery.app.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme =
    lightColorScheme(
        primary = Color(0xFFA62B34),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDADB),
        onPrimaryContainer = Color(0xFF40000A),
        secondary = Color(0xFF775656),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDADA),
        onSecondaryContainer = Color(0xFF2C1516),
        tertiary = Color(0xFF755A2C),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDEA6),
        onTertiaryContainer = Color(0xFF271900),
        background = Color(0xFFFFF8F7),
        onBackground = Color(0xFF241919),
        surface = Color(0xFFFFF8F7),
        onSurface = Color(0xFF241919),
        surfaceVariant = Color(0xFFF4DDDC),
        onSurfaceVariant = Color(0xFF524344),
        outline = Color(0xFF857374),
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = Color(0xFFFFB2B7),
        onPrimary = Color(0xFF680019),
        primaryContainer = Color(0xFF861D2A),
        onPrimaryContainer = Color(0xFFFFDADB),
        secondary = Color(0xFFE6BDBD),
        onSecondary = Color(0xFF44292A),
        secondaryContainer = Color(0xFF5D3F40),
        onSecondaryContainer = Color(0xFFFFDADA),
        tertiary = Color(0xFFE5C18D),
        onTertiary = Color(0xFF422C02),
        tertiaryContainer = Color(0xFF5B4216),
        onTertiaryContainer = Color(0xFFFFDEA6),
        background = Color(0xFF1C1112),
        onBackground = Color(0xFFF3DEDE),
        surface = Color(0xFF1C1112),
        onSurface = Color(0xFFF3DEDE),
        surfaceVariant = Color(0xFF524344),
        onSurfaceVariant = Color(0xFFD7C1C1),
        outline = Color(0xFFA98C8D),
    )

private val DeliveryShapes =
    Shapes(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(20.dp),
    )

@Composable
fun DeliveryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        shapes = DeliveryShapes,
        content = content,
    )
}
