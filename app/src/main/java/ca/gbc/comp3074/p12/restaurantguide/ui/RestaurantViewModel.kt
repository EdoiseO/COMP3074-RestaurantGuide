package ca.gbc.comp3074.p12.restaurantguide.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.p12.restaurantguide.data.AppDatabase
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import ca.gbc.comp3074.p12.restaurantguide.data.RestaurantRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RestaurantViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RestaurantRepository
    val restaurants: StateFlow<List<Restaurant>>

    init {
        val dao = AppDatabase.getDatabase(application).restaurantDao()
        repository = RestaurantRepository(dao)
        restaurants = repository.allRestaurants.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun addRestaurant(restaurant: Restaurant) {
        viewModelScope.launch { repository.insert(restaurant) }
    }

    fun updateRestaurant(restaurant: Restaurant) {
        viewModelScope.launch { repository.update(restaurant) }
    }

    fun deleteRestaurant(restaurant: Restaurant) {
        viewModelScope.launch { repository.delete(restaurant) }
    }
}