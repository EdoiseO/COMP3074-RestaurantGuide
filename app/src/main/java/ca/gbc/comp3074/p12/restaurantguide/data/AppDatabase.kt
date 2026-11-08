package ca.gbc.comp3074.p12.restaurantguide.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
                .addMigrations(MIGRATION_1_2)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        // Room's schema creation transaction includes this synchronous callback.
                        // No reader/writer can see half a seed or race its explicit IDs.
                        DemoRestaurants.restaurants.forEach { insertRow(db, it.toEntity()) }
                    }
                })

        internal val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE restaurants_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL,
                    description TEXT NOT NULL, address TEXT NOT NULL, phone TEXT NOT NULL,
                    tagsJson TEXT NOT NULL, rating REAL NOT NULL, listSummary TEXT NOT NULL,
                    notes TEXT NOT NULL, coverUri TEXT NOT NULL, photoKey TEXT,
                    galleryKeysJson TEXT NOT NULL, mapKey TEXT, fullMapKey TEXT,
                    photoUrisJson TEXT NOT NULL, photoCaptionsJson TEXT NOT NULL)""")
                db.query("SELECT * FROM restaurants").use { cursor ->
                    fun text(column: String) = cursor.getString(cursor.getColumnIndexOrThrow(column))
                    fun legacyList(column: String, separator: String): List<String> =
                        text(column).let { if (it.isEmpty()) emptyList() else it.split(separator) }
                    while (cursor.moveToNext()) {
                        val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                        // V1 stored resource IDs. Recover only recognised resources and known demos;
                        // never treat an unknown old integer as a drawable in the new build.
                        val demo = DemoRestaurants.restaurants.firstOrNull {
                            it.id == id && it.name == text("name") && it.address == text("address")
                        }
                        val photoIndex = cursor.getColumnIndexOrThrow("photoRes")
                        val photo = if (cursor.isNull(photoIndex)) null else
                            BundledRestaurantImages.key(cursor.getInt(photoIndex))
                        val gallery = legacyList("galleryResString", ",").mapNotNull {
                            it.toIntOrNull()?.let(BundledRestaurantImages::key)
                        }
                        val uris = legacyList("photoUrisString", "|")
                        val captions = legacyList("photoCaptionsString", "|")
                        val row = RestaurantEntity(
                            id, text("name"), text("description"), text("address"), text("phone"),
                            legacyList("tagsString", ",").toJson(), cursor.getFloat(cursor.getColumnIndexOrThrow("rating")),
                            text("listSummary"), text("notes"), text("coverUri"),
                            demo?.photoRes?.let(BundledRestaurantImages::key) ?: photo,
                            (demo?.galleryRes?.mapNotNull(BundledRestaurantImages::key) ?: gallery).toJson(),
                            demo?.mapRes?.let(BundledRestaurantImages::key),
                            demo?.fullMapRes?.let(BundledRestaurantImages::key), uris.toJson(),
                            uris.indices.map { captions.getOrElse(it) { "" } }.toJson(),
                        )
                        insertRow(db, row, "restaurants_new")
                    }
                }
                db.execSQL("DROP TABLE restaurants")
                db.execSQL("ALTER TABLE restaurants_new RENAME TO restaurants")
                // V1 delimiter ambiguity cannot be reversed; all future writes use lossless JSON.
                // Do not re-seed: deleted demo rows must stay deleted.
            }
        }

        private fun insertRow(db: SupportSQLiteDatabase, row: RestaurantEntity, table: String = "restaurants") {
            db.execSQL("""INSERT INTO $table (id,name,description,address,phone,tagsJson,rating,
                listSummary,notes,coverUri,photoKey,galleryKeysJson,mapKey,fullMapKey,photoUrisJson,photoCaptionsJson)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)""", arrayOf<Any?>(
                row.id, row.name, row.description, row.address, row.phone, row.tagsJson, row.rating,
                row.listSummary, row.notes, row.coverUri, row.photoKey, row.galleryKeysJson,
                row.mapKey, row.fullMapKey, row.photoUrisJson, row.photoCaptionsJson,
            ))
        }
    }
}
