package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.gbc.comp3074.p12.restaurantguide.data.DemoRestaurants
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import ca.gbc.comp3074.p12.restaurantguide.data.RestaurantMissingException
import ca.gbc.comp3074.p12.restaurantguide.data.RestaurantStore
import kotlinx.coroutines.flow.MutableStateFlow

/** Activity-owned fixture survives UI recreation without touching the production database. */
@Composable
fun TestRestaurantListFlow(initial: List<Restaurant> = DemoRestaurants.restaurants) {
    val owner: RestaurantViewModel = viewModel(factory = viewModelFactory {
        initializer { RestaurantViewModel(FakeRestaurantStore(initial)) }
    })
    RestaurantListFlow(owner)
}

class FakeRestaurantStore(initial: List<Restaurant> = emptyList()) : RestaurantStore {
    override val allRestaurants = MutableStateFlow(initial)
    var failWrites = false
    var insertCount = 0
    override suspend fun insert(restaurant: Restaurant): Int {
        check(!failWrites)
        val id = (allRestaurants.value.maxOfOrNull { it.id } ?: 0) + 1
        insertCount++
        allRestaurants.value = listOf(restaurant.copy(id = id)) + allRestaurants.value
        return id
    }
    override suspend fun update(restaurant: Restaurant) {
        check(!failWrites)
        if (allRestaurants.value.none { it.id == restaurant.id }) throw RestaurantMissingException()
        allRestaurants.value = allRestaurants.value.map { if (it.id == restaurant.id) restaurant else it }
    }
    override suspend fun delete(restaurant: Restaurant) {
        check(!failWrites)
        if (allRestaurants.value.none { it.id == restaurant.id }) throw RestaurantMissingException()
        allRestaurants.value = allRestaurants.value.filterNot { it.id == restaurant.id }
    }
}
