package ca.gbc.comp3074.p12.restaurantguide.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import ca.gbc.comp3074.p12.restaurantguide.data.DemoRestaurants
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Covers the static preview and captures URL handoffs without launching external apps. */
class RestaurantMapFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun eachSelectedMapShowsItsSavedAddressAndBackReturnsToSameDetails() {
        compose.setContent { RestaurantGuideTheme { RestaurantListFlow() } }
        DemoRestaurants.restaurants.forEach { record ->
            openRestaurant(record.id)
            compose.onNodeWithTag("details-full-map").performScrollTo().performClick()
            compose.onNodeWithTag("full-map-screen").assertIsDisplayed()
            compose.onNodeWithText("Full-Screen Map").assertIsDisplayed()
            compose.onNodeWithTag("full-map-preview").assertIsDisplayed().assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.ContentDescription, listOf("Map preview for ${record.name}"),
                ),
            )
            compose.onNodeWithTag("map-name").assertTextEquals(record.name)
            compose.onNodeWithTag("map-address").assertTextEquals(record.address)
            compose.onNodeWithTag("map-static-label").assertIsDisplayed().assertTextEquals("Map preview")
            compose.onNodeWithTag("get-directions").assertIsDisplayed().assertIsEnabled()
            snapshot("full-map-restaurant-${record.id}")
            compose.onNodeWithTag("map-back").performClick()
            compose.onNodeWithTag("restaurant-details").assertIsDisplayed()
            compose.onNodeWithText(record.name).performScrollTo().assertIsDisplayed()
            compose.onNodeWithText("← Back").performClick()
        }
    }

    @Test fun missingAddressHasIllustrativePreviewAndDisablesDirections() {
        val record = DemoRestaurants.restaurants.first().copy(address = "", mapRes = null, fullMapRes = null)
        compose.setContent { RestaurantGuideTheme { RestaurantMapEntry(record, {}) } }
        compose.onNodeWithTag("map-address").assertTextEquals("Add an address to get directions.")
        compose.onNodeWithText("Illustrative preview · saved address below").assertIsDisplayed()
        compose.onNodeWithTag("get-directions").assertIsNotEnabled()
        compose.onNodeWithTag("full-map-preview").assertIsDisplayed().assert(
            SemanticsMatcher("illustrative preview states that the saved address is missing") {
                it.config.getOrNull(SemanticsProperties.ContentDescription)?.any { description ->
                    description.contains("Illustrative map preview for ${record.name}") &&
                        description.contains("Saved address: Add an address to get directions.")
                } == true
            },
        )
        snapshot("full-map-missing-address")
    }

    @Test fun editedAddressUsesCurrentSavedAddressAndClearlyLabelledIllustrativeMap() {
        val record = DemoRestaurants.restaurants.first()
        val updatedAddress = "100 Demo Street, Toronto, ON M5A 1A1"
        compose.setContent { RestaurantGuideTheme { RestaurantListFlow() } }
        openRestaurant(record.id)
        compose.onNodeWithText("Edit").performClick()
        compose.onNodeWithTag("edit-address").performScrollTo().performTextReplacement(updatedAddress)
        closeSoftKeyboard()
        compose.onNodeWithTag("edit-save").performClick()
        compose.onNodeWithTag("details-full-map").performScrollTo().performClick()
        compose.onNodeWithTag("map-address").assertTextEquals(updatedAddress)
        compose.onNodeWithText("Illustrative preview · saved address below").assertIsDisplayed()
        compose.onNodeWithTag("full-map-preview").assertIsDisplayed().assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ContentDescription,
                listOf("Illustrative map preview for ${record.name}. Saved address: $updatedAddress."),
            ),
        )
        compose.onNodeWithTag("get-directions").assertIsEnabled()
        compose.onNodeWithText(record.address).assertDoesNotExist()
        snapshot("full-map-edited-address")
        compose.onNodeWithTag("map-back").performClick()
        compose.onNodeWithText(updatedAddress).performScrollTo().assertIsDisplayed()
    }

    @Test fun longSavedAddressKeepsDirectionsAndBackReachableInSmallViewport() {
        val longAddress = "Unit 123, First floor, accessible entrance via courtyard, " +
            "100 Long Demonstration Street, Near the north side of the public square, " +
            "Toronto, Ontario M5A 1A1, Canada"
        val record = DemoRestaurants.restaurants.first().copy(
            address = longAddress, mapRes = null, fullMapRes = null,
        )
        compose.setContent {
            RestaurantGuideTheme {
                Box(Modifier.size(width = 320.dp, height = 420.dp)) { RestaurantMapEntry(record, {}) }
            }
        }
        compose.onNodeWithTag("map-address").assertTextEquals(longAddress)
        compose.onNodeWithTag("get-directions").assertIsDisplayed().assertIsEnabled()
        val infoScroll = compose.onNodeWithTag("map-info").fetchSemanticsNode()
            .config[SemanticsProperties.VerticalScrollAxisRange]
        assertTrue("The complete long address must remain scrollable", infoScroll.maxValue() > 0f)
        compose.onNodeWithTag("map-info").performTouchInput { swipeUp() }
        compose.waitUntil(5_000) { infoScroll.value() > 0f }
        compose.onNodeWithTag("get-directions").assertIsDisplayed().assertIsEnabled()
        compose.onNodeWithTag("map-back").assertIsDisplayed()
        snapshot("full-map-small-viewport-long-address")
    }

    @Test fun portraitOnlyPreviewRemainsRealAndDirectionsHandoffUsesSelectedRecord() {
        val record = DemoRestaurants.restaurants[1].copy(mapRes = null)
        val handler = RecordingDirectionsContext(compose.activity)
        compose.setContent {
            CompositionLocalProvider(LocalContext provides handler) {
                RestaurantGuideTheme { RestaurantMapEntry(record, {}) }
            }
        }
        compose.onNodeWithTag("full-map-preview").assertIsDisplayed().assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ContentDescription, listOf("Map preview for ${record.name}"),
            ),
        )
        compose.onNodeWithText("Illustrative preview · saved address below").assertDoesNotExist()
        compose.onNodeWithTag("get-directions").performClick()
        compose.runOnIdle {
            assertNotNull(handler.attempted)
            assertEquals(Intent.ACTION_VIEW, handler.attempted?.action)
            assertEquals("https", handler.attempted?.data?.scheme)
            assertEquals("www.google.com", handler.attempted?.data?.host)
            assertEquals("/maps/dir/", handler.attempted?.data?.path)
            assertEquals("1", handler.attempted?.data?.getQueryParameter("api"))
            assertEquals("${record.name}, ${record.address}",
                handler.attempted?.data?.getQueryParameter("destination"))
        }
        compose.onNodeWithTag("full-map-screen").assertIsDisplayed()
    }

    @Test fun unavailableDirectionsHandlerShowsDismissibleFeedbackWithoutLeavingMap() {
        val record = DemoRestaurants.restaurants[2]
        val handler = RecordingDirectionsContext(compose.activity, available = false)
        compose.setContent {
            CompositionLocalProvider(LocalContext provides handler) {
                RestaurantGuideTheme { RestaurantMapEntry(record, {}) }
            }
        }
        compose.onNodeWithTag("get-directions").performClick()
        compose.onNodeWithText("Directions unavailable").assertIsDisplayed()
        compose.onNodeWithText("No app is available to open directions. Try again after installing a map app or browser.")
            .assertIsDisplayed()
        snapshot("full-map-directions-unavailable")
        compose.onNodeWithText("Got it").performClick()
        compose.onNodeWithTag("full-map-screen").assertIsDisplayed()
        compose.onNodeWithTag("map-address").assertTextEquals(record.address)
        compose.onNodeWithTag("get-directions").assertIsDisplayed().assertIsEnabled()
    }

    private fun openRestaurant(id: Int) {
        compose.onNodeWithTag("restaurant-list").performScrollToNode(hasTestTag("restaurant-card-$id"))
        compose.onNodeWithTag("restaurant-card-$id").performClick()
    }

    private fun snapshot(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        android.os.SystemClock.sleep(200)
        val bitmap = instrumentation.uiAutomation.takeScreenshot() ?: return
        val folder = instrumentation.targetContext.filesDir.resolve("list-test-screens").apply { mkdirs() }
        folder.resolve("$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private class RecordingDirectionsContext(context: Context, private val available: Boolean = true) :
        ContextWrapper(context) {
        var attempted: Intent? = null
        override fun startActivity(intent: Intent) {
            attempted = Intent(intent)
            if (!available) throw ActivityNotFoundException("Deterministic missing directions-handler fixture")
        }
    }
}
