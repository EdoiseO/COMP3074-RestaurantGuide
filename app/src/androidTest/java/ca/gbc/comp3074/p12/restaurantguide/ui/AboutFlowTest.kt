package ca.gbc.comp3074.p12.restaurantguide.ui

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AboutFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun aboutInformationAndBothBackActionsPreserveSearchAndSelectedFilters() {
        compose.setContent { RestaurantGuideTheme { RestaurantListFlow() } }
        compose.onNodeWithTag("filter-tag-Shawarma").performClick()
        compose.onNodeWithTag("filter-tag-Turkish").performClick()
        compose.onNodeWithTag("restaurant-search").performTextInput("Brothers")
        compose.onNodeWithTag("restaurant-search").performImeAction()
        compose.onNodeWithText("About").performClick()
        assertApprovedInformation()
        snapshot("about-default")
        compose.onNodeWithTag("about-back").performClick()
        assertListState()
        compose.onNodeWithText("About").performClick()
        compose.onNodeWithTag("about-screen").assertIsDisplayed()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        assertListState()
    }

    @Test fun largeTextInSmallWindowKeepsAllInformationScrollableAndBackReachable() {
        var returned = false
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 2f)) {
                RestaurantGuideTheme {
                    Box(Modifier.size(width = 320.dp, height = 420.dp)) {
                        AboutEntry(onBack = { returned = true })
                    }
                }
            }
        }
        val scrollRange = compose.onNodeWithTag("about-body").fetchSemanticsNode()
            .config[SemanticsProperties.VerticalScrollAxisRange]
        assertTrue("About information must scroll rather than truncate at large text", scrollRange.maxValue() > 0f)
        compose.onNodeWithTag("about-purpose").performScrollTo().assertIsDisplayed()
            .assertTextEquals("Display and manage all saved restaurants")
        compose.onNodeWithTag("about-team-names").performScrollTo().assertIsDisplayed()
            .assertTextEquals(approvedNames)
        assertTrue("Reaching the names should scroll the information", scrollRange.value() > 0f)
        compose.onNodeWithTag("about-back").assertIsDisplayed()
        snapshot("about-small-window-large-text")
        compose.onNodeWithTag("about-back").performClick()
        compose.runOnIdle { assertTrue("Back remains usable after scrolling", returned) }
    }

    private fun assertApprovedInformation() {
        compose.onNodeWithTag("about-screen").assertIsDisplayed()
        compose.onNodeWithText("Restaurant Guide").assertIsDisplayed()
        compose.onNodeWithText("COMP3074 · Team P12").assertIsDisplayed()
        compose.onNodeWithTag("about-purpose").assertIsDisplayed()
            .assertTextEquals("Display and manage all saved restaurants")
        compose.onNodeWithText("Team members").assertIsDisplayed()
        compose.onNodeWithTag("about-team-names").assertIsDisplayed().assertTextEquals(approvedNames)
    }

    private fun assertListState() {
        compose.onNodeWithTag("restaurant-search").assertTextContains("Brothers")
        compose.onNodeWithTag("filter-tag-Shawarma").assertIsSelected()
        compose.onNodeWithTag("filter-tag-Turkish").assertIsSelected()
        compose.onNodeWithTag("result-count").assertTextContains("1 saved restaurant")
        compose.onNodeWithTag("restaurant-card-1").assertIsDisplayed()
    }

    private fun snapshot(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val bitmap = instrumentation.uiAutomation.takeScreenshot() ?: return
        val folder = instrumentation.targetContext.filesDir.resolve("about-test-screens").apply { mkdirs() }
        folder.resolve("$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private val approvedNames = "Kalid Ali\nPhillip Onofua\nMohamed Amine OUATAR\nJoshua Zaakir"
}
