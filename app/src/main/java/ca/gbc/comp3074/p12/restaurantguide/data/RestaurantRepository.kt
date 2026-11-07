package ca.gbc.comp3074.p12.restaurantguide.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RestaurantRepository(private val restaurantDao: RestaurantDao) {
    val allRestaurants: Flow<List<Restaurant>> = restaurantDao.getAllRestaurants()
        .map { entities -> entities.map { it.toDomain() } }

    suspend fun insert(restaurant: Restaurant) {
        restaurantDao.insertRestaurant(restaurant.toEntity())
    }

    suspend fun update(restaurant: Restaurant) {
        restaurantDao.updateRestaurant(restaurant.toEntity())
    }

    suspend fun delete(restaurant: Restaurant) {
        restaurantDao.deleteRestaurant(restaurant.toEntity())
    }
}