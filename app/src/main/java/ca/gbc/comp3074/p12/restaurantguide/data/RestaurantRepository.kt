package ca.gbc.comp3074.p12.restaurantguide.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RestaurantRepository(private val restaurantDao: RestaurantDao) : RestaurantStore {
    override val allRestaurants: Flow<List<Restaurant>> = restaurantDao.getAllRestaurants()
        .map { entities -> entities.map { it.toDomain() } }

    override suspend fun insert(restaurant: Restaurant): Int =
        restaurantDao.insertRestaurant(restaurant.copy(id = 0).toEntity()).toInt()

    override suspend fun update(restaurant: Restaurant) {
        if (restaurantDao.updateRestaurant(restaurant.toEntity()) == 0) throw RestaurantMissingException()
    }

    override suspend fun delete(restaurant: Restaurant) {
        if (restaurantDao.deleteRestaurant(restaurant.toEntity()) == 0) throw RestaurantMissingException()
    }
}
