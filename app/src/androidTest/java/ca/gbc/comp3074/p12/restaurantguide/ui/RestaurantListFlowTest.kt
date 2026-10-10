package ca.gbc.comp3074.p12.restaurantguide.ui

import android.graphics.Bitmap
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme
import org.junit.Rule
import org.junit.Test

class RestaurantListFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun searchAndMultipleFiltersSurviveDetailsBackAndRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { RestaurantGuideTheme { RestaurantListFlow() } }
        compose.onNodeWithTag("result-count").assertTextContains("3 saved restaurants")
        compose.onNodeWithContentDescription("Clear search").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("3 saved restaurants")
        snapshot("default")
        compose.onNodeWithTag("filter-tag-Shawarma").performClick()
        compose.onNodeWithTag("filter-tag-Turkish").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("2 saved restaurants")
        snapshot("multiple-filters")
        compose.onNodeWithTag("restaurant-search").performTextInput("Brothers")
        // The count must update while typing, before the keyboard's Search action.
        compose.onNodeWithTag("result-count").assertTextContains("1 saved restaurant")
        compose.onNodeWithTag("restaurant-search").performImeAction()
        snapshot("live-search")
        compose.onNodeWithTag("result-count").assertTextContains("1 saved restaurant")
        compose.onNodeWithTag("restaurant-card-1").performClick()
        compose.onNodeWithTag("restaurant-details").assertIsDisplayed()
        compose.onNodeWithText("Two Brothers Shawarma").assertIsDisplayed()
        compose.onNodeWithText("← Back").performClick()
        assertSearchState()
        restoration.emulateSavedInstanceStateRestore()
        assertSearchState()
        compose.onNodeWithTag("filter-All").performClick()
        compose.onNodeWithTag("filter-All").assertIsSelected()
        compose.onNodeWithTag("result-count").assertTextContains("1 saved restaurant")
        compose.onNodeWithContentDescription("Clear search").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("3 saved restaurants")
    }

    @Test fun noResultsHasWorkingResetAndAboutNavigation() {
        compose.setContent { RestaurantGuideTheme { RestaurantListFlow() } }
        compose.onNodeWithTag("restaurant-search").performTextInput("does-not-exist")
        compose.onNodeWithTag("restaurant-search").performImeAction()
        compose.onNodeWithText("No restaurants found").assertIsDisplayed()
        compose.onNodeWithTag("add-restaurant").assertIsDisplayed()
        snapshot("no-results")
        compose.onNodeWithText("Clear search and filters").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("3 saved restaurants")
        compose.onNodeWithText("About").performClick()
        compose.onNodeWithText("Team members").assertIsDisplayed()
        compose.onNodeWithText("Kalid Ali\nPhillip Onofua\nMohamed Amine OUATAR\nJoshua Zaakir").assertIsDisplayed()
        compose.onNodeWithText("← Back").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("3 saved restaurants")
    }

    @Test fun emptyCollectionCanAddAndFilterACustomRestaurant() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { RestaurantGuideTheme { RestaurantListFlow(emptyList()) } }
        compose.onNodeWithText("No restaurants saved yet").assertIsDisplayed()
        snapshot("empty-list")
        compose.onNodeWithTag("add-restaurant").performClick()
        compose.onNodeWithText("Save").performClick()
        compose.onNodeWithText("Enter a restaurant name before saving. You can add photos and other details later.").assertIsDisplayed()
        compose.onNodeWithText("Got it").performClick()
        compose.onNodeWithTag("add-name").performTextInput("My Cafe")
        compose.onNodeWithTag("add-description").performScrollTo().performTextInput("Coffee and lunch")
        listOf("Cafe", "Lunch", "all").forEach { tag ->
            compose.onNodeWithTag("add-add-tag").performScrollTo().performClick()
            compose.onNodeWithTag("tag-input").performTextInput(tag)
            compose.onNodeWithTag("tag-save").performClick()
        }
        compose.onNodeWithText("Save").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("1 saved restaurant")
        compose.onNodeWithText("My Cafe").assertIsDisplayed()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("My Cafe").assertIsDisplayed()
        compose.onNodeWithText("all (tag)").assertIsDisplayed()
        compose.onNodeWithTag("filter-tag-Cafe").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("1 saved restaurant")
    }

    @Test fun scrollingOpensTheCorrectRestaurantAndBackKeepsPosition() {
        compose.setContent { RestaurantGuideTheme { RestaurantListFlow() } }
        compose.onNodeWithTag("restaurant-list").performScrollToNode(hasTestTag("restaurant-card-3"))
        compose.onNodeWithContentDescription("Photo of La Unica").assertIsDisplayed()
        snapshot("third-card")
        compose.onNodeWithTag("restaurant-card-3").performClick()
        compose.onNodeWithText("La Unica").assertIsDisplayed()
        compose.onNodeWithText("Mexican bakery and taqueria", substring = true).assertIsDisplayed()
        snapshot("la-unica-details-top")
        compose.onNodeWithText("850 Wilson Ave, North York, ON M3K 1E5").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("437-216-6130").performScrollTo().assertIsDisplayed()
        snapshot("la-unica-details")
        compose.onNodeWithTag("details-edit-notes").performScrollTo()
        snapshot("la-unica-details-bottom")
        compose.onNodeWithText("← Back").performClick()
        compose.onNodeWithTag("restaurant-card-3").assertIsDisplayed()
        compose.onNodeWithTag("filter-tag-Turkish").performClick()
        compose.onNodeWithTag("restaurant-card-2").performClick()
        snapshot("midyeci-details-top")
        compose.onNodeWithText("954 Wilson Ave, North York, ON M3K 1E7").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("416-398-3884").performScrollTo().assertIsDisplayed()
        snapshot("midyeci-details")
        compose.onNodeWithTag("details-edit-notes").performScrollTo()
        snapshot("midyeci-details-bottom")
    }

    private fun assertSearchState() {
        compose.onNodeWithTag("restaurant-search").assertTextContains("Brothers")
        compose.onNodeWithTag("filter-tag-Shawarma").assertIsSelected()
        compose.onNodeWithTag("filter-tag-Turkish").assertIsSelected()
        compose.onNodeWithTag("result-count").assertTextContains("1 saved restaurant")
    }

    private fun snapshot(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        android.os.SystemClock.sleep(200)
        val bitmap = instrumentation.uiAutomation.takeScreenshot() ?: return
        val context = instrumentation.targetContext
        val folder = context.filesDir.resolve("list-test-screens").apply { mkdirs() }
        folder.resolve(name + ".png").outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        bitmap.recycle()
    }
}
