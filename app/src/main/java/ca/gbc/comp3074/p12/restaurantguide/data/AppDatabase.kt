package ca.gbc.comp3074.p12.restaurantguide.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room Database Singleton configured with a one-time seeding callback
 * so demo seeds populate on fresh install without re-adding deleted seeds on restart.
 */
@Database(entities = [RestaurantEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun restaurantDao(): RestaurantDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "restaurant_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    // Seed demo data precisely once upon initial table creation
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = database.restaurantDao()
                        val initialEntities = DemoRestaurants.restaurants.map { it.toEntity() }
                        for (entity in initialEntities) {
                            dao.insertRestaurant(entity)
                        }
                    }
                }
            }
        }
    }
}