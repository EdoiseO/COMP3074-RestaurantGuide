package ca.gbc.comp3074.p12.restaurantguide.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.SemanticsMatcher
import androidx.test.platform.app.InstrumentationRegistry
import ca.gbc.comp3074.p12.restaurantguide.data.DemoRestaurants
import ca.gbc.comp3074.p12.restaurantguide.data.restaurantShareBody
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RestaurantDetailsFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun notesSaveCancelAndSelectedRecordSurviveRestoration() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { RestaurantGuideTheme { RestaurantListFlow() } }
        openRestaurant(1)
        snapshot("two-brothers-details-top")
        compose.onNodeWithTag("details-tag-Shawarma").assert(
            SemanticsMatcher("read-only tag") {
                !it.config.contains(SemanticsActions.OnClick) && !it.config.contains(SemanticsProperties.Selected)
            },
        )
        compose.onNodeWithTag("details-edit-notes").performScrollTo().performClick()
        compose.onNodeWithTag("visit-notes-input").performTextInput("Beef plate — order it again.")
        compose.onNodeWithText("Save").performClick()
        compose.onNodeWithText("Beef plate — order it again.").assertIsDisplayed()
        snapshot("two-brothers-details-bottom")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Beef plate — order it again.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("details-edit-notes").performScrollTo().performClick()
        compose.onNodeWithTag("visit-notes-input").performTextReplacement("Discard this")
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Beef plate — order it again.").assertIsDisplayed()
        compose.onNodeWithText("← Back").performClick()
        openRestaurant(2)
        compose.onNodeWithText("Add your visit notes here.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Beef plate — order it again.").assertDoesNotExist()
    }

    @Test fun deleteCancelThenConfirmRemovesOnlySelectedRecord() {
        compose.setContent { RestaurantGuideTheme { RestaurantListFlow() } }
        openRestaurant(3)
        compose.onNodeWithText("Delete").performClick()
        snapshot("delete-confirmation")
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithTag("restaurant-details").assertIsDisplayed()
        compose.onNodeWithText("Delete").performClick()
        compose.onNodeWithTag("confirm-delete").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("2 saved restaurants")
        compose.onNodeWithTag("restaurant-card-1").assertIsDisplayed()
        compose.onNodeWithTag("restaurant-card-3").assertDoesNotExist()
        compose.onNodeWithTag("restaurant-list").performScrollToNode(hasTestTag("restaurant-card-2"))
        compose.onNodeWithTag("restaurant-card-2").assertIsDisplayed()
    }

    @Test fun editCancelAndSaveUpdateTheSameRestaurant() {
        compose.setContent { RestaurantGuideTheme { RestaurantListFlow() } }
        openRestaurant(2)
        compose.onNodeWithText("Edit").performClick()
        compose.onNodeWithTag("edit-name").assertTextContains("Midyeci Riza")
        compose.onNodeWithTag("edit-description").performScrollTo().performTextReplacement("Unsaved description")
        compose.onNodeWithTag("edit-back").performClick()
        compose.onNodeWithText("Turkish street food on Wilson Avenue", substring = true).assertIsDisplayed()
        compose.onNodeWithText("Edit").performClick()
        compose.onNodeWithTag("edit-description").performScrollTo().performTextReplacement("Try the stuffed mussels and ayran.")
        compose.onNodeWithTag("edit-save").performClick()
        compose.onNodeWithText("Try the stuffed mussels and ayran.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("954 Wilson Ave, North York, ON M3K 1E7").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("← Back").performClick()
        openRestaurant(1)
        compose.onNodeWithText("Chicken and beef shawarma served as plates", substring = true).assertIsDisplayed()
    }

    @Test fun photoGallerySwipesAndClosesBackToDetails() {
        compose.setContent { RestaurantGuideTheme { RestaurantListFlow() } }
        openRestaurant(1)
        compose.onNodeWithTag("details-photo-0").performScrollTo().performClick()
        compose.onNodeWithTag("restaurant_photo_viewer").assertIsDisplayed()
        compose.onNodeWithText("1 / 4 · My photos").assertIsDisplayed()
        compose.onNodeWithContentDescription("Previous photo").performClick()
        compose.onNodeWithText("4 / 4 · My photos").assertIsDisplayed()
        compose.onNodeWithContentDescription("Next photo").performClick()
        compose.onNodeWithText("1 / 4 · My photos").assertIsDisplayed()
        compose.onNodeWithTag("restaurant_photo_pager").performTouchInput { swipeLeft() }
        compose.onNodeWithText("2 / 4 · My photos").assertIsDisplayed()
        compose.onNodeWithContentDescription("Next photo").performClick()
        compose.onNodeWithText("3 / 4 · My photos").assertIsDisplayed()
        snapshot("photo-gallery")
        compose.onNodeWithContentDescription("Close photos").performClick()
        compose.onNodeWithTag("restaurant-details").assertIsDisplayed()
        compose.onNodeWithTag("details-add-photo").performScrollTo().performClick()
        compose.onNodeWithText("Add a visit photo").assertIsDisplayed()
        compose.onNodeWithText("Caption (optional)").assertIsDisplayed()
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithTag("details-photo-0").assertIsDisplayed()
    }

    @Test fun mapAndEmailUseSelectedRecordAndBackKeepsListSearch() {
        val noEmail = MissingEmailContext(compose.activity)
        compose.setContent {
            CompositionLocalProvider(LocalContext provides noEmail) {
                RestaurantGuideTheme { RestaurantListFlow() }
            }
        }
        compose.onNodeWithTag("restaurant-search").performTextInput("Unica")
        compose.onNodeWithTag("restaurant-card-3").performClick()
        compose.onNodeWithTag("details-full-map").performScrollTo().performClick()
        compose.onNodeWithTag("full-map-preview").assertIsDisplayed()
        compose.onNodeWithText("850 Wilson Ave, North York, ON M3K 1E5").assertIsDisplayed()
        snapshot("la-unica-full-map")
        val record = DemoRestaurants.restaurants[2]
        assertTrue(getDirectionsUri(record).getQueryParameter("destination")!!.contains(record.address))
        compose.onNodeWithTag("map-back").performClick()
        compose.onNodeWithTag("details-email").performScrollTo().performClick()
        compose.onNodeWithTag("email-preview").assertIsDisplayed()
        assertEquals(Intent.ACTION_SENDTO, noEmail.attempted?.action)
        assertEquals("mailto", noEmail.attempted?.data?.scheme)
        assertEquals(record.name, noEmail.attempted?.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals(restaurantShareBody(record), noEmail.attempted?.getStringExtra(Intent.EXTRA_TEXT))
        snapshot("email-preview")
        compose.onNodeWithText("Close").performClick()
        compose.onNodeWithText("← Back").performClick()
        compose.onNodeWithTag("restaurant-search").assertTextContains("Unica")
        compose.onNodeWithTag("result-count").assertTextContains("1 saved restaurant")
    }

    @Test fun unavailableSelectedPhotoKeepsCaptionAcrossRestoration() {
        val restoration = StateRestorationTester(compose)
        val records = DemoRestaurants.restaurants.map {
            if (it.id == 2) it.copy(
                photoUris = listOf("content://unavailable-provider/visit-photo"),
                photoCaptions = listOf("Stuffed mussels — get these again."),
            ) else it
        }
        restoration.setContent { RestaurantGuideTheme { RestaurantListFlow(records) } }
        openRestaurant(2)
        compose.onNodeWithTag("details-photo-1").performScrollTo().performClick()
        compose.onNodeWithText("2 / 2 · My photos").assertIsDisplayed()
        compose.onNodeWithText("Stuffed mussels — get these again.").assertIsDisplayed()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Stuffed mussels — get these again.").assertIsDisplayed()
        compose.onNodeWithContentDescription("Close photos").performClick()
        compose.onNodeWithTag("restaurant-details").assertIsDisplayed()
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
        folder.resolve(name + ".png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private class MissingEmailContext(context: Context) : ContextWrapper(context) {
        var attempted: Intent? = null
        override fun startActivity(intent: Intent) {
            attempted = intent
            throw ActivityNotFoundException("Deterministic no-email-handler test fixture")
        }
    }
}
