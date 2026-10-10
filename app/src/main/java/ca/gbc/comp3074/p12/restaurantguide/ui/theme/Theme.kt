package ca.gbc.comp3074.p12.restaurantguide.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RestaurantGuideColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    background = Color.White,
    surface = Color.White,
    surfaceVariant = Color(0xFFF1ECF7),
    onSurfaceVariant = Color(0xFF49454F),
)

@Composable
fun RestaurantGuideTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RestaurantGuideColors,
        content = content,
    )
}
