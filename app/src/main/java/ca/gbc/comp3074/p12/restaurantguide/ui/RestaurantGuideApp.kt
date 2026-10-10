package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun RestaurantGuideApp(
    nativeSplashRemoved: Boolean = true,
    onSplashVisibilityChanged: (Boolean) -> Unit = {},
) {
    var splashVisible by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(splashVisible) {
        onSplashVisibilityChanged(splashVisible)
    }
    LaunchedEffect(splashVisible, nativeSplashRemoved) {
        if (splashVisible && nativeSplashRemoved) {
            delay(1_500)
            splashVisible = false
        }
    }
    if (splashVisible) SplashScreen() else RestaurantListFlow()
}
