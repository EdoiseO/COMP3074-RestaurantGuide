package ca.gbc.comp3074.p12.restaurantguide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ca.gbc.comp3074.p12.restaurantguide.ui.RestaurantGuideApp
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RestaurantGuideTheme {
                RestaurantGuideApp()
            }
        }
    }
}
