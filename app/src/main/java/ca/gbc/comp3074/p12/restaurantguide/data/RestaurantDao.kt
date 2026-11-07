package ca.gbc.comp3074.p12.restaurantguide.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for handling queries using Coroutines and Flows.
 */
@Dao
interface RestaurantDao {

    // Reactive stream of all saved restaurants ordered by ID descending
    @Query("SELECT * FROM restaurants ORDER BY id DESC")
    fun getAllRestaurants(): Flow<List<RestaurantEntity>>

    // Inserts a new record or replaces on conflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: RestaurantEntity): Long

    // Updates an existing record
    @Update
    suspend fun updateRestaurant(restaurant: RestaurantEntity)

    // Deletes a selected record
    @Delete
    suspend fun deleteRestaurant(restaurant: RestaurantEntity)
}