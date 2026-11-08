package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.p12.restaurantguide.data.*
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PhotoSaveFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun formLocksAcrossRecreationUntilPhotoCheckAndDatabaseWriteFinish() {
        val photo = "content://test/photo"
        val seed = Restaurant(1, "Photo cafe", "", "", "", emptyList(), 4.5f, coverUri = photo)
        val store = FakeRestaurantStore(listOf(seed))
        val gate = CompletableDeferred<Unit>()
        val grants = object : PhotoGrants {
            val held = mutableSetOf<String>()
            override suspend fun readable(reference: String): Boolean { gate.await(); return true }
            override suspend fun persisted() = held.toSet()
            override suspend fun take(reference: String): Boolean { held += reference; return true }
            override suspend fun release(reference: String) { held -= reference }
        }
        val owner = RestaurantViewModel(store, PhotoWriteCoordinator(grants))
        val restoration = StateRestorationTester(compose)
        restoration.setContent { RestaurantGuideTheme { RestaurantListFlow(owner) } }
        compose.onNodeWithTag("restaurant-card-1").performClick()
        compose.onNodeWithTag("details-edit").performClick()
        compose.onNodeWithTag("edit-name").performTextReplacement("Saved after check")
        compose.onNodeWithTag("edit-save").performClick()
        compose.onNodeWithTag("storage-saving").assertIsDisplayed()
        compose.onNodeWithTag("edit-save").assertIsNotEnabled()
        compose.onNodeWithTag("edit-cancel").assertIsNotEnabled()
        compose.runOnIdle { assertEquals("Photo cafe", store.allRestaurants.value.single().name) }
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("storage-saving").assertIsDisplayed()
        compose.runOnIdle { gate.complete(Unit) }
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("storage-saving").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("details-edit").assertIsDisplayed()
        compose.runOnIdle {
            assertEquals("Saved after check", store.allRestaurants.value.single().name)
            assertEquals(setOf(photo), grants.held)
            owner.viewModelScope.cancel()
        }
    }
}
