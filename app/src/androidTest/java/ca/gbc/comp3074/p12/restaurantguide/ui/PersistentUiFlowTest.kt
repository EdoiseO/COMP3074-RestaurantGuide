package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.gbc.comp3074.p12.restaurantguide.data.DemoRestaurants
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PersistentUiFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private fun show(store: FakeRestaurantStore, restoration: StateRestorationTester? = null) {
        val content: @androidx.compose.runtime.Composable () -> Unit = {
            val owner: RestaurantViewModel = viewModel(factory = viewModelFactory {
                initializer { RestaurantViewModel(store) }
            })
            RestaurantGuideTheme { RestaurantListFlow(owner) }
        }
        if (restoration == null) compose.setContent(content) else restoration.setContent(content)
    }

    @Test fun failedAddKeepsDraftAcrossRecreationAndRetryCommitsOnce() {
        val store = FakeRestaurantStore().apply { failWrites = true }
        val restoration = StateRestorationTester(compose)
        show(store, restoration)
        compose.onNodeWithTag("add-restaurant").performClick()
        compose.onNodeWithTag("add-name").performTextInput("Keep my cafe")
        compose.onNodeWithTag("add-save").performClick()
        compose.onNodeWithTag("form-save-error").assertIsDisplayed()
        compose.onNodeWithTag("add-name").assertTextContains("Keep my cafe")
        compose.runOnIdle { assertTrue(store.allRestaurants.value.isEmpty()) }
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithTag("add-name").assertTextContains("Keep my cafe")
        compose.runOnIdle { store.failWrites = false }
        compose.onNodeWithTag("add-save").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("1 saved")
        compose.runOnIdle { assertEquals(1, store.insertCount) }
    }

    @Test fun notesFailureKeepsDialogDraftAndDeleteFailureKeepsDetails() {
        val store = FakeRestaurantStore(DemoRestaurants.restaurants).apply { failWrites = true }
        show(store)
        compose.onNodeWithTag("restaurant-card-1").performClick()
        compose.onNodeWithTag("details-edit-notes").performScrollTo().performClick()
        compose.onNodeWithTag("visit-notes-input").performTextReplacement("Keep my visit notes")
        compose.onNodeWithTag("notes-save").performClick()
        compose.onNodeWithTag("notes-save-error").assertIsDisplayed()
        compose.onNodeWithTag("visit-notes-input").assertTextContains("Keep my visit notes")
        compose.runOnIdle { store.failWrites = false }
        compose.onNodeWithTag("notes-save").performClick()
        compose.onNodeWithTag("visit-notes-input").assertDoesNotExist()
        compose.onNodeWithText("Keep my visit notes").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { store.failWrites = true }
        compose.onNodeWithTag("details-delete").performClick()
        compose.onNodeWithTag("confirm-delete").performClick()
        compose.onNodeWithText("Changes were not saved. Your draft is still here. Please try again.").assertIsDisplayed()
        compose.runOnIdle { assertEquals(3, store.allRestaurants.value.size); store.failWrites = false }
        compose.onNodeWithTag("confirm-delete").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("2 saved")
    }

    @Test fun missingSelectedRecordOffersBackAndKeepsSearch() {
        val store = FakeRestaurantStore(DemoRestaurants.restaurants)
        show(store)
        compose.onNodeWithTag("restaurant-search").performTextInput("Brothers")
        compose.onNodeWithTag("restaurant-search").performImeAction()
        compose.onNodeWithTag("restaurant-card-1").performClick()
        compose.runOnIdle { store.allRestaurants.value = store.allRestaurants.value.filterNot { it.id == 1 } }
        compose.onNodeWithTag("missing-record-back").assertIsDisplayed().performClick()
        compose.onNodeWithTag("restaurant-search").assertTextContains("Brothers")
        compose.onNodeWithText("No restaurants found").assertIsDisplayed()
    }
}
