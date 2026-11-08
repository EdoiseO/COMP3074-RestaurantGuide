package ca.gbc.comp3074.p12.restaurantguide.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RestaurantEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: builder(context, "restaurant_database").build().also { instance = it }
        }

        internal fun builder(context: Context, name: String): Builder<AppDatabase> =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, name)
                // V1 was an unreleased prototype with unsafe numeric image IDs. Reset only
                // that version; future versions must supply an explicit migration.
                .fallbackToDestructiveMigrationFrom(true, 1)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Room's schema creation transaction includes this synchronous callback.
                        // No reader/writer can see half a seed or race its explicit IDs.
                        seed(db)
                    }
                })

        private fun seed(db: SupportSQLiteDatabase) {
            DemoRestaurants.restaurants.forEach { insertRow(db, it.toEntity()) }
        }

        private fun insertRow(db: SupportSQLiteDatabase, row: RestaurantEntity) {
            db.execSQL("""INSERT INTO restaurants (id,name,description,address,phone,tagsJson,rating,
                listSummary,notes,coverUri,photoKey,galleryKeysJson,mapKey,fullMapKey,photoUrisJson,photoCaptionsJson)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""", arrayOf<Any?>(
                row.id, row.name, row.description, row.address, row.phone, row.tagsJson, row.rating,
                row.listSummary, row.notes, row.coverUri, row.photoKey, row.galleryKeysJson,
                row.mapKey, row.fullMapKey, row.photoUrisJson, row.photoCaptionsJson,
            ))
        }
    }
}
