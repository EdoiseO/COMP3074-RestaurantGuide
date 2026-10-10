package ca.gbc.comp3074.p12.restaurantguide.ui

import android.graphics.Bitmap
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import ca.gbc.comp3074.p12.restaurantguide.data.DemoRestaurants
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class RestaurantFormFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun completeAddAndEditRetainHalfStarsAndAllFields() {
        val restoration = StateRestorationTester(compose)
        restoration.setContent { RestaurantGuideTheme { RestaurantListFlow(emptyList()) } }
        compose.onNodeWithTag("add-restaurant").performClick()
        snapshot("add-form-top")
        compose.onNodeWithTag("add-save").performClick()
        compose.onNodeWithText("Restaurant name required").assertIsDisplayed()
        snapshot("add-name-required")
        compose.onNodeWithText("Got it").performClick()
        field("add-name", "Demo Cafe")
        field("add-description", "Coffee, sandwiches and a quiet lunch spot.")
        closeSoftKeyboard()
        compose.onNodeWithTag("add-rating").performScrollTo().performClick()
        dragRating(.9f)
        compose.onNodeWithTag("rating-draft-value").assertTextContains("4.5 / 5")
        snapshot("rating-half-stars")
        compose.onNodeWithTag("rating-save").performClick()
        compose.onNodeWithTag("add-rating").performClick()
        dragRating(.5f)
        compose.onNodeWithTag("rating-draft-value").assertTextContains("2.5 / 5")
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithTag("add-rating").assertTextContains("4.5 / 5")
        addTag("Cafe")
        addTag("Lunch")
        addTag("cafe")
        compose.onNodeWithContentDescription("Remove tag Cafe").assertExists()
        compose.onNodeWithContentDescription("Remove tag cafe").assertDoesNotExist()
        compose.onNodeWithContentDescription("Remove tag Lunch").performClick()
        compose.onNodeWithTag("add-address").performScrollTo()
        compose.onNodeWithContentDescription("Enter address details").performClick()
        field("address-street", "2324 Lake Shore Blvd W")
        field("address-city", "Etobicoke")
        field("address-province", "ON")
        field("address-postal", "m8v1b6")
        compose.onNodeWithTag("address-save").performClick()
        field("add-phone", "416-201-8280\n437-216-6130")
        field("add-notes", "Beef plate — order it again.")
        closeSoftKeyboard()
        snapshot("add-form-bottom")
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("add-notes").assertTextContains("Beef plate — order it again.")
        compose.onNodeWithTag("add-save").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("1 saved restaurant")
        compose.onNodeWithText("4.5").assertIsDisplayed()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("restaurant-card-1").performClick()
        compose.onNodeWithContentDescription("Your rating: 4.5/5 stars").assertExists()
        compose.onNodeWithText("2324 Lake Shore Blvd W, Etobicoke, ON M8V 1B6").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("416-201-8280\n437-216-6130").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Beef plate — order it again.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Edit").performClick()
        compose.onNodeWithTag("edit-name").assertTextContains("Demo Cafe")
        field("edit-name", "Unsaved name")
        compose.onNodeWithTag("edit-back").performClick()
        compose.onNodeWithText("Demo Cafe").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Edit").performClick()
        compose.onNodeWithTag("edit-rating").performScrollTo().performClick()
        dragRating(.5f)
        compose.onNodeWithTag("rating-save").performClick()
        field("edit-notes", "Updated visit notes.")
        closeSoftKeyboard()
        compose.onNodeWithTag("edit-save").performClick()
        compose.onNodeWithContentDescription("Your rating: 2.5/5 stars").assertExists()
        compose.onNodeWithText("Updated visit notes.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("← Back").performClick()
        compose.onNodeWithText("2.5").assertIsDisplayed()
    }

    @Test fun editRejectsInvalidPhoneAndRemovesOnlyDraftPhotoUntilSave() {
        val original = DemoRestaurants.restaurants.first()
        var saved: Restaurant? = null
        compose.setContent { RestaurantGuideTheme { RestaurantEditEntry(original, {}, { saved = it }) } }
        snapshot("edit-form-top")
        compose.onNode(hasContentDescription("Remove photo") and hasAnyAncestor(hasTestTag("edit-resource-photo-0"))).performClick()
        compose.onNodeWithTag("edit-resource-photo-3").assertDoesNotExist()
        assertEquals(4, original.galleryRes.size)
        field("edit-phone", "bad number")
        compose.onNodeWithTag("edit-save").performClick()
        compose.onNodeWithText("Check phone number").assertIsDisplayed()
        compose.onNodeWithText("Got it").performClick()
        assertNull(saved)
        field("edit-phone", "416-201-8280")
        field("edit-address", "9 New St, Toronto, ON m5v2t6")
        closeSoftKeyboard()
        compose.onNodeWithTag("edit-save").performClick()
        compose.runOnIdle {
            assertNotNull(saved)
            assertEquals(original.id, saved!!.id)
            assertEquals(3, saved!!.galleryRes.size)
            assertEquals("9 New St, Toronto, ON M5V 2T6", saved!!.address)
            assertNull(saved!!.mapRes)
            assertNull(saved!!.fullMapRes)
        }
    }

    private fun field(tag: String, value: String) {
        compose.onNodeWithTag(tag).performScrollTo().performTextReplacement(value)
    }

    private fun addTag(value: String) {
        compose.onNodeWithTag("add-add-tag").performScrollTo().performClick()
        compose.onNodeWithTag("tag-input").performTextInput(value)
        compose.onNodeWithTag("tag-save").performClick()
    }

    private fun dragRating(fraction: Float) {
        compose.onNodeWithTag("rating-swipe-stars").performTouchInput {
            swipe(Offset(width * .15f, centerY), Offset(width * fraction, centerY), 600)
        }
    }

    private fun snapshot(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        android.os.SystemClock.sleep(200)
        val bitmap = instrumentation.uiAutomation.takeScreenshot()
        bitmap ?: return
        val folder = instrumentation.targetContext.filesDir.resolve("list-test-screens").apply { mkdirs() }
        folder.resolve("$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
