package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RedesignScreenLayoutTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun narrowHomeWithDoubleSizeTextKeepsCountSearchAndAboutUsable() {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 2f)) {
                RestaurantGuideTheme {
                    Box(Modifier.size(320.dp, 640.dp)) { TestRestaurantListFlow() }
                }
            }
        }
        compose.onNodeWithTag("result-count").assertTextEquals("3 saved").assertIsDisplayed()
        compose.onNodeWithTag("list-about").assertIsDisplayed().performClick()
        compose.onNodeWithText("← Back").performClick()
        compose.onNodeWithTag("restaurant-search").performTextInput("zzzz")
        compose.onNodeWithTag("restaurant-search").performImeAction()
        compose.onNodeWithTag("result-count").assertTextEquals("0 saved")
        compose.onNodeWithTag("clear-search").assertIsDisplayed().performClick()
        compose.onNodeWithTag("result-count").assertTextEquals("3 saved")
        compose.onNodeWithTag("add-restaurant").assertIsDisplayed()
    }

    @Test fun narrowDetailsWithDoubleSizeTextKeepsNotesAndDockReachable() {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 2f)) {
                RestaurantGuideTheme {
                    Box(Modifier.size(320.dp, 640.dp)) { TestRestaurantListFlow() }
                }
            }
        }
        compose.onNodeWithTag("restaurant-card-1").performClick()
        compose.onNodeWithTag("details-edit-notes").performScrollTo().assertIsDisplayed()
        val notes = compose.onNodeWithTag("details-edit-notes").fetchSemanticsNode().boundsInRoot
        val dock = compose.onNodeWithTag("details-action-dock").fetchSemanticsNode().boundsInRoot
        assertTrue(notes.bottom <= dock.top)
        compose.onNodeWithTag("details-back").assertIsDisplayed()
        compose.onNodeWithTag("details-edit").assertIsDisplayed()
        compose.onNodeWithTag("details-share").assertIsDisplayed()
        compose.onNodeWithTag("details-full-map").assertIsDisplayed().performClick()
        compose.onNodeWithTag("map-back").performClick()
        compose.onNodeWithTag("details-delete").assertIsDisplayed().performClick()
        compose.onNodeWithTag("delete-cancel").performClick()
        compose.onNodeWithTag("details-back").performClick()
        compose.onNodeWithTag("result-count").assertTextEquals("3 saved")
    }
}
