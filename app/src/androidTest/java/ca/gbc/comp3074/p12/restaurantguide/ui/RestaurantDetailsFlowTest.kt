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
import androidx.compose.ui.test.swipeUp
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
        restoration.setContent { RestaurantGuideTheme { TestRestaurantListFlow() } }
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
        compose.setContent { RestaurantGuideTheme { TestRestaurantListFlow() } }
        openRestaurant(3)
        compose.onNodeWithTag("details-delete").performClick()
        snapshot("delete-confirmation")
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithTag("restaurant-details").assertIsDisplayed()
        compose.onNodeWithTag("details-delete").performClick()
        compose.onNodeWithTag("confirm-delete").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("3 saved")
        compose.onNodeWithTag("restaurant-card-1").assertIsDisplayed()
        compose.onNodeWithTag("restaurant-card-3").assertDoesNotExist()
        compose.onNodeWithTag("restaurant-list").performScrollToNode(hasTestTag("restaurant-card-2"))
        compose.onNodeWithTag("restaurant-card-2").assertIsDisplayed()
    }

    @Test fun editCancelAndSaveUpdateTheSameRestaurant() {
        compose.setContent { RestaurantGuideTheme { TestRestaurantListFlow() } }
        openRestaurant(2)
        compose.onNodeWithTag("details-edit").performClick()
        compose.onNodeWithTag("edit-name").assertTextContains("Midyeci Riza")
        compose.onNodeWithTag("edit-description").performScrollTo().performTextReplacement("Unsaved description")
        compose.onNodeWithTag("edit-back").performClick()
        compose.onNodeWithText("Turkish street food on Wilson Avenue", substring = true).assertIsDisplayed()
        compose.onNodeWithTag("details-edit").performClick()
        compose.onNodeWithTag("edit-description").performScrollTo().performTextReplacement("Try the stuffed mussels and ayran.")
        compose.onNodeWithTag("edit-save").performClick()
        compose.onNodeWithText("Try the stuffed mussels and ayran.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("954 Wilson Ave, North York, ON M3K 1E7").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("← Back").performClick()
        openRestaurant(1)
        compose.onNodeWithText("Chicken and beef shawarma served as plates", substring = true).assertIsDisplayed()
    }

    @Test fun photoGallerySwipesAndClosesBackToDetails() {
        compose.setContent { RestaurantGuideTheme { TestRestaurantListFlow() } }
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

    @Test fun mapAndShareUseSelectedRecordAndBackKeepsListSearch() {
        val noShare = MissingShareContext(compose.activity)
        compose.setContent {
            CompositionLocalProvider(LocalContext provides noShare) {
                RestaurantGuideTheme { TestRestaurantListFlow() }
            }
        }
        compose.onNodeWithTag("restaurant-search").performTextInput("Unica")
        compose.onNodeWithTag("restaurant-card-3").performClick()
        compose.onNodeWithTag("details-full-map").performClick()
        compose.onNodeWithTag("full-map-preview").assertIsDisplayed()
        compose.onNodeWithText("850 Wilson Ave, North York, ON M3K 1E5").assertIsDisplayed()
        snapshot("la-unica-full-map")
        val record = DemoRestaurants.restaurants[2]
        assertTrue(getDirectionsUri(record).getQueryParameter("destination")!!.contains(record.address))
        compose.onNodeWithTag("map-back").performClick()
        compose.onNodeWithTag("details-share").performClick()
        compose.onNodeWithTag("share-preview").assertIsDisplayed()
        snapshot("share-preview")
        compose.onNodeWithText("Close").performClick()
        compose.onNodeWithText("← Back").performClick()
        compose.onNodeWithTag("restaurant-search").assertTextContains("Unica")
        compose.onNodeWithTag("result-count").assertTextContains("1 saved")
    }

    @Test fun unavailableSelectedPhotoKeepsCaptionAcrossRestoration() {
        val restoration = StateRestorationTester(compose)
        val records = DemoRestaurants.restaurants.map {
            if (it.id == 2) it.copy(
                photoUris = listOf("content://unavailable-provider/visit-photo"),
                photoCaptions = listOf("Stuffed mussels — get these again."),
            ) else it
        }
        restoration.setContent { RestaurantGuideTheme { TestRestaurantListFlow(records) } }
        openRestaurant(2)
        compose.onNodeWithTag("details-photo-1").performScrollTo().performClick()
        compose.onNodeWithText("2 / 2 · My photos").assertIsDisplayed()
        compose.onNodeWithText("Stuffed mussels — get these again.").assertIsDisplayed()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Stuffed mussels — get these again.").assertIsDisplayed()
        compose.onNodeWithContentDescription("Close photos").performClick()
        compose.onNodeWithTag("restaurant-details").assertIsDisplayed()
    }

    @Test fun fixedActionsStayReachableAndNotesClearTheDock() {
        compose.setContent { RestaurantGuideTheme { TestRestaurantListFlow() } }
        openRestaurant(1)
        val editBounds = compose.onNodeWithTag("details-edit").fetchSemanticsNode().boundsInRoot
        val dockBounds = compose.onNodeWithTag("details-action-dock").fetchSemanticsNode().boundsInRoot
        compose.onNodeWithTag("details-scroll").performTouchInput { swipeUp() }
        compose.onNodeWithTag("details-edit-notes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("details-back").assertIsDisplayed()
        compose.onNodeWithTag("details-edit").assertIsDisplayed()
        compose.onNodeWithTag("details-share").assertIsDisplayed()
        compose.onNodeWithTag("details-delete").assertIsDisplayed()
        assertEquals(editBounds, compose.onNodeWithTag("details-edit").fetchSemanticsNode().boundsInRoot)
        assertEquals(dockBounds, compose.onNodeWithTag("details-action-dock").fetchSemanticsNode().boundsInRoot)
        val notesBounds = compose.onNodeWithTag("details-edit-notes").fetchSemanticsNode().boundsInRoot
        assertTrue("Notes edit must scroll fully above the dock", notesBounds.bottom <= dockBounds.top)
        compose.onNodeWithText("Share by email").assertDoesNotExist()
        compose.onNodeWithTag("details-edit").performClick()
        compose.onNodeWithTag("edit-name").assertTextContains("Two Brothers Shawarma")
        compose.onNodeWithTag("edit-cancel").performClick()
        compose.onNodeWithTag("details-back").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("4 saved")
    }

    @Test fun shareChooserIncludesCompletePayloadAndNoHandlerDoesNotLaunch() {
        val record = DemoRestaurants.restaurants[2]
        var chooser: Intent? = null
        launchRestaurantShare(record, "Share restaurant", canHandle = { true }, launch = { chooser = it })
        assertEquals(Intent.ACTION_CHOOSER, chooser?.action)
        @Suppress("DEPRECATION")
        val payload = chooser?.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertEquals(Intent.ACTION_SEND, payload?.action)
        assertEquals("text/plain", payload?.type)
        assertEquals(record.name, payload?.getStringExtra(Intent.EXTRA_SUBJECT))
        assertEquals(restaurantShareBody(record), payload?.getStringExtra(Intent.EXTRA_TEXT))
        var unavailable = false
        try {
            launchRestaurantShare(record, "Share restaurant", canHandle = { false },
                launch = { throw AssertionError("Must not open an empty chooser") })
        } catch (_: ActivityNotFoundException) { unavailable = true }
        assertTrue(unavailable)
    }

    @Test fun phoneTapOpensDialerForSelectedRestaurantAndKeepsSearch() {
        val dialer = DialerContext(compose.activity)
        compose.setContent {
            CompositionLocalProvider(LocalContext provides dialer) {
                RestaurantGuideTheme { TestRestaurantListFlow() }
            }
        }
        compose.onNodeWithTag("restaurant-search").performTextInput("Unica")
        compose.onNodeWithTag("restaurant-card-3").performClick()
        compose.onNodeWithTag("details-phone-0").performScrollTo().performClick()
        assertEquals(Intent.ACTION_DIAL, dialer.attempted?.action)
        assertEquals("tel", dialer.attempted?.data?.scheme)
        assertEquals("437-216-6130", dialer.attempted?.data?.schemeSpecificPart)
        compose.onNodeWithTag("restaurant-details").assertIsDisplayed()
        compose.onNodeWithTag("details-back").performClick()
        compose.onNodeWithTag("restaurant-search").assertTextContains("Unica")
    }

    @Test fun eachSavedPhoneNumberHasItsOwnDialAction() {
        val dialer = DialerContext(compose.activity)
        val record = DemoRestaurants.restaurants.first().copy(phone = "+1 (416) 201-8280;416-555-0100\n416-555-0101")
        compose.setContent {
            CompositionLocalProvider(LocalContext provides dialer) {
                RestaurantGuideTheme { TestRestaurantListFlow(listOf(record)) }
            }
        }
        openRestaurant(1)
        listOf("+1 (416) 201-8280", "416-555-0100", "416-555-0101").forEachIndexed { index, number ->
            compose.onNodeWithTag("details-phone-$index").performScrollTo().performClick()
            assertEquals(Intent.ACTION_DIAL, dialer.attempted?.action)
            assertEquals(number, dialer.attempted?.data?.schemeSpecificPart)
        }
    }

    @Test fun missingPhoneDoesNotOfferADialAction() {
        compose.setContent {
            RestaurantGuideTheme {
                TestRestaurantListFlow(listOf(DemoRestaurants.restaurants.first().copy(phone = "")))
            }
        }
        openRestaurant(1)
        compose.onNodeWithText("Not added").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("details-phone-0").assertDoesNotExist()
    }

    @Test fun missingDialerShowsCopyableNumberAndDismissesBackToDetails() {
        val dialer = DialerContext(compose.activity, unavailable = true)
        compose.setContent {
            CompositionLocalProvider(LocalContext provides dialer) {
                RestaurantGuideTheme { TestRestaurantListFlow() }
            }
        }
        openRestaurant(1)
        compose.onNodeWithTag("details-phone-0").performScrollTo().performClick()
        compose.onNodeWithTag("phone-unavailable").assertIsDisplayed()
        compose.onNodeWithText("No phone app is available. You can copy this number.").assertIsDisplayed()
        assertEquals("416-201-8280", dialer.attempted?.data?.schemeSpecificPart)
        compose.onNodeWithText("Got it").performClick()
        compose.onNodeWithTag("phone-unavailable").assertDoesNotExist()
        compose.onNodeWithTag("restaurant-details").assertIsDisplayed()
    }

    private class DialerContext(context: Context, val unavailable: Boolean = false) : ContextWrapper(context) {
        var attempted: Intent? = null
        override fun startActivity(intent: Intent) {
            attempted = intent
            if (unavailable) throw ActivityNotFoundException("No dialer test fixture")
        }
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

    private class MissingShareContext(context: Context) : ContextWrapper(context) {
        var attempted: Intent? = null
        override fun startActivity(intent: Intent) {
            attempted = intent
            throw ActivityNotFoundException("Deterministic no-share-handler test fixture")
        }
    }
}
