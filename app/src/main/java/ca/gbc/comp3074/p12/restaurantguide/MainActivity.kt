package ca.gbc.comp3074.p12.restaurantguide

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.mutableStateOf
import ca.gbc.comp3074.p12.restaurantguide.ui.RestaurantGuideApp
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val nativeSplash = installSplashScreen()
        super.onCreate(savedInstanceState)
        val nativeSplashRemoved = mutableStateOf(savedInstanceState != null)
        nativeSplash.setOnExitAnimationListener { provider ->
            provider.remove()
            nativeSplashRemoved.value = true
        }
        updateSystemBars(splashVisible = true)
        setContent {
            RestaurantGuideTheme {
                RestaurantGuideApp(
                    nativeSplashRemoved = nativeSplashRemoved.value,
                    onSplashVisibilityChanged = ::updateSystemBars,
                )
            }
        }
    }

    private fun updateSystemBars(splashVisible: Boolean) {
        val style = if (splashVisible) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.argb(128, 0, 0, 0))
        }
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}
