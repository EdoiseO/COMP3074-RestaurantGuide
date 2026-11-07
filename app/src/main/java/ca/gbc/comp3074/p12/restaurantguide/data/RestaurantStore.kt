package ca.gbc.comp3074.p12.restaurantguide.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Shared UI/storage contract (#15). IDs are stable positive Ints; insert ignores a draft's ID
 * and returns the generated ID. A successful suspend write means storage has committed it.
 * Updates replace a complete saved record, including tags, phones, notes, half-star rating,
 * independent cover/album references and captions. Drafts never enter this stream.
 * Reads/writes may throw; callers must keep drafts on failure and propagate cancellation.
 * Updating/deleting a missing ID throws [RestaurantMissingException], never silently succeeds.
 */
interface RestaurantStore {
    val allRestaurants: Flow<List<Restaurant>>
    fun observeRestaurant(id: Int): Flow<Restaurant?> = allRestaurants.map { rows -> rows.find { it.id == id } }
    suspend fun insert(restaurant: Restaurant): Int
    suspend fun update(restaurant: Restaurant)
    suspend fun delete(restaurant: Restaurant)
}

class RestaurantMissingException : IllegalStateException("Restaurant no longer exists")
