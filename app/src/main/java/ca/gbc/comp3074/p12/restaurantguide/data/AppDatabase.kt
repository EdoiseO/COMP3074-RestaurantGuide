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
                // Only unreleased V1 may reset and re-seed. Later schema versions need real
                // migrations without seeding, so deleted demo rows stay deleted.
                .fallbackToDestructiveMigrationFrom(true, 1)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        seed(db)
                    }

                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        seed(db)
                    }
                })

        private fun seed(db: SupportSQLiteDatabase) {
            // Fresh creation and the V1 reset both run inside the framework's open/upgrade
            // transaction. Keep schema creation and seeding atomic if called without one.
            val ownsTransaction = !db.inTransaction()
            if (ownsTransaction) db.beginTransaction()
            try {
                // Room 2.8.5 calls onDestructiveMigration AFTER dropping tables but BEFORE
                // createAllTables. Create the V2 table here; Room's later IF NOT EXISTS is safe.
                // Keep this definition aligned with the exported V2 Room schema.
                db.execSQL("""CREATE TABLE IF NOT EXISTS restaurants (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL,
                    description TEXT NOT NULL, address TEXT NOT NULL, phone TEXT NOT NULL,
                    tagsJson TEXT NOT NULL, rating REAL NOT NULL, listSummary TEXT NOT NULL,
                    notes TEXT NOT NULL, coverUri TEXT NOT NULL, photoKey TEXT,
                    galleryKeysJson TEXT NOT NULL, mapKey TEXT, fullMapKey TEXT,
                    photoUrisJson TEXT NOT NULL, photoCaptionsJson TEXT NOT NULL)""")
                DemoRestaurants.restaurants.forEach { insertRow(db, it.toEntity()) }
                if (ownsTransaction) db.setTransactionSuccessful()
            } finally {
                if (ownsTransaction) db.endTransaction()
            }
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
