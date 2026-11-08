package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.gbc.comp3074.p12.restaurantguide.data.RestaurantStore
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import kotlinx.coroutines.flow.*
import ca.gbc.comp3074.p12.restaurantguide.data.DemoRestaurants
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class PersistentUiFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private lateinit var owner: RestaurantViewModel
    private fun show(store: RestaurantStore, restoration: StateRestorationTester? = null) {
        val content: @androidx.compose.runtime.Composable () -> Unit = {
            val owner: RestaurantViewModel = viewModel(factory = viewModelFactory {
                initializer { RestaurantViewModel(store) }
            })
            this.owner = owner
            RestaurantGuideTheme { RestaurantListFlow(owner) }
        }
        if (restoration == null) compose.setContent(content) else restoration.setContent(content)
    }

    @Test fun delayedInsertEmissionScrollsToNewRecordAfterListWasScrolled() {
        val initial = (1..20).map { DemoRestaurants.restaurants.first().copy(id = it, name = "Cafe $it") }
        val delegate = FakeRestaurantStore(initial)
        var pending: Restaurant? = null
        val store = object : RestaurantStore by delegate {
            override suspend fun insert(restaurant: Restaurant): Int {
                pending = restaurant.copy(id = 21)
                return 21
            }
        }
        show(store)
        compose.onNodeWithTag("restaurant-list").performScrollToNode(hasTestTag("restaurant-card-20"))
        compose.onNodeWithTag("add-restaurant").performClick()
        compose.onNodeWithTag("add-name").performTextInput("Newest cafe")
        compose.onNodeWithTag("add-save").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("20 saved")
        compose.runOnIdle { delegate.allRestaurants.value = listOf(pending!!) + initial }
        compose.onNodeWithTag("restaurant-card-21").assertIsDisplayed()
    }

    @Test fun readFailureOnDetailsOffersRetryWithoutLosingSelectedRecord() {
        val delegate = FakeRestaurantStore(DemoRestaurants.restaurants)
        var failReads = false
        val store = object : RestaurantStore by delegate {
            override val allRestaurants: Flow<List<Restaurant>> = flow {
                check(!failReads); emitAll(delegate.allRestaurants)
            }
        }
        show(store)
        compose.onNodeWithTag("restaurant-card-1").performClick()
        compose.runOnIdle { failReads = true; owner.reload() }
        compose.onNodeWithTag("storage-retry").assertIsDisplayed()
        compose.runOnIdle { failReads = false }
        compose.onNodeWithTag("storage-retry").performClick()
        compose.onNodeWithTag("details-edit").assertIsDisplayed()
    }

    @Test fun readRetryKeepsUnsavedFormDraft() {
        val delegate = FakeRestaurantStore(DemoRestaurants.restaurants)
        var failReads = false
        val store = object : RestaurantStore by delegate {
            override val allRestaurants: Flow<List<Restaurant>> = flow {
                check(!failReads); emitAll(delegate.allRestaurants)
            }
        }
        show(store)
        compose.onNodeWithTag("add-restaurant").performClick()
        compose.onNodeWithTag("add-name").performTextInput("Keep across read retry")
        compose.runOnIdle { failReads = true; owner.reload() }
        compose.onNodeWithTag("storage-retry").assertIsDisplayed()
        compose.runOnIdle { failReads = false }
        compose.onNodeWithTag("storage-retry").performClick()
        compose.onNodeWithTag("add-name").assertTextContains("Keep across read retry")
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
        compose.runOnIdle { assertEquals(4, store.allRestaurants.value.size); store.failWrites = false }
        compose.onNodeWithTag("confirm-delete").performClick()
        compose.onNodeWithTag("result-count").assertTextContains("3 saved")
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
