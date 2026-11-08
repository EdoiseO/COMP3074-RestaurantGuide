package ca.gbc.comp3074.p12.restaurantguide.data

import android.content.Context
import android.graphics.BitmapFactory
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class StorageRoundTripTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun freshDatabaseIncludesTheFryWithOfflinePhotosAndSurvivesReopen() = runBlocking {
        val name = "four-seeds-${UUID.randomUUID()}"
        var db = AppDatabase.builder(context, name).build()
        try {
            val rows = db.restaurantDao().getAllRestaurants().first()
            assertEquals(listOf(4, 3, 2, 1), rows.map { it.id })
            val fry = rows.first().toDomain()
            assertEquals("The Fry", fry.name)
            assertEquals(listOf("Korean", "Fried chicken"), fry.tags)
            assertEquals(3.5f, fry.rating)
            assertEquals("the_fry_storefront", rows.first().photoKey)
            assertEquals(3, fry.galleryRes.size)
            (fry.galleryRes + listOfNotNull(fry.photoRes)).distinct().forEach { resource ->
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeResource(context.resources, resource, bounds)
                assertTrue("Bundled photo must decode", bounds.outWidth > 0 && bounds.outHeight > 0)
            }
            assertNull(fry.mapRes)
            assertNull(fry.fullMapRes)
            db.close()
            db = AppDatabase.builder(context, name).build()
            assertEquals(rows, db.restaurantDao().getAllRestaurants().first())
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun losslessFieldsAndStableResourceKeysSurviveReopen() = runBlocking {
        val name = "round-trip-${UUID.randomUUID()}"
        var db = AppDatabase.builder(context, name).build()
        try {
            val source = DemoRestaurants.restaurants.first().copy(id = 0,
                tags = listOf("Rice, beans", "a|b", "", "é🍜"),
                photoUris = listOf("content://photos/a|b", "content://photos/c"),
                photoCaptions = listOf("", "Meal | sauce, \"yes\"\nAgain"), coverUri = "content://photos/cover")
            val id = db.restaurantDao().insertRestaurant(source.toEntity()).toInt()
            val entity = db.restaurantDao().getAllRestaurants().first().single { it.id == id }
            assertEquals("two_brothers_meal", entity.photoKey)
            assertEquals("two_brothers_map", entity.mapKey)
            assertEquals("two_brothers_full_map", entity.fullMapKey)
            db.close()
            db = AppDatabase.builder(context, name).build()
            assertEquals(source.copy(id = id), db.restaurantDao().getAllRestaurants().first().single { it.id == id }.toDomain())
            assertNull(BundledRestaurantImages.resource("removed_resource"))
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun firstOpenConcurrentInsertCannotRaceSeedsAndDeletedSeedsStayDeleted() = runBlocking {
        val name = "seed-${UUID.randomUUID()}"
        var db = AppDatabase.builder(context, name).build()
        try {
            val inserted = coroutineScope {
                val read = async { db.restaurantDao().getAllRestaurants().first() }
                val write = async { db.restaurantDao().insertRestaurant(DemoRestaurants.restaurants.first().copy(id = 0, name = "User restaurant").toEntity()) }
                assertTrue(read.await().size >= 4)
                write.await().toInt()
            }
            assertEquals(5, inserted)
            val all = db.restaurantDao().getAllRestaurants().first()
            assertEquals(5, all.size)
            assertEquals("User restaurant", all.single { it.id == inserted }.name)
            db.restaurantDao().deleteRestaurant(all.single { it.id == 4 })
            db.close()
            db = AppDatabase.builder(context, name).build()
            val reopened = db.restaurantDao().getAllRestaurants().first()
            assertEquals(4, reopened.size)
            assertFalse(reopened.any { it.id == 4 })
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun v1ResetSeedsInsideUpgradeTransactionAndDoesNotReseedOnOpen() = runBlocking {
        val name = "migration-${UUID.randomUUID()}"
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { old ->
            old.execSQL("""CREATE TABLE restaurants (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL, description TEXT NOT NULL, address TEXT NOT NULL, phone TEXT NOT NULL,
                tagsString TEXT NOT NULL, rating REAL NOT NULL, listSummary TEXT NOT NULL, notes TEXT NOT NULL,
                coverUri TEXT NOT NULL, photoRes INTEGER, galleryResString TEXT NOT NULL,
                photoUrisString TEXT NOT NULL, photoCaptionsString TEXT NOT NULL)""")
            old.execSQL("INSERT INTO restaurants VALUES (9,'User','Description','Address','111','Tag',4.5,'Summary','Notes','content://cover',NULL,'','content://a|content://b','|caption')")
            old.version = 1
        }
        var resetWasTransactional = false
        var db = AppDatabase.builder(context, name)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                    resetWasTransactional = db.inTransaction()
                    assertTrue("Reset must be inside the upgrade transaction", resetWasTransactional)
                    db.query("SELECT count(*) FROM restaurants").use {
                        assertTrue(it.moveToFirst())
                        assertEquals(4, it.getInt(0))
                    }
                }
            }).build()
        try {
            val rows = db.restaurantDao().getAllRestaurants().first()
            assertTrue(resetWasTransactional)
            assertEquals(listOf(4, 3, 2, 1), rows.map { it.id })
            assertEquals(DemoRestaurants.restaurants.sortedByDescending { it.id }.map { it.name }, rows.map { it.name })
            assertTrue(rows.filter { it.id != 4 }.all { it.mapKey != null && it.fullMapKey != null })
            assertNull(rows.first().mapKey)
            assertNull(rows.first().fullMapKey)
            assertFalse(rows.any { it.id == 9 || it.name == "User" })
            db.openHelper.readableDatabase.query("SELECT seq FROM sqlite_sequence WHERE name = 'restaurants'").use {
                assertTrue(it.moveToFirst())
                assertEquals(4, it.getInt(0))
            }
            val repository = RestaurantRepository(db.restaurantDao())
            repository.insert(DemoRestaurants.restaurants.first().copy(id = 0, name = "Added after reset"))
            assertEquals(5, repository.allRestaurants.first().single { it.name == "Added after reset" }.id)
            db.close()
            db = AppDatabase.builder(context, name).build()
            assertEquals(listOf(5, 4, 3, 2, 1), db.restaurantDao().getAllRestaurants().first().map { it.id })
            db.restaurantDao().deleteRestaurant(db.restaurantDao().getAllRestaurants().first().single { it.id == 1 })
            db.close()
            db = AppDatabase.builder(context, name).build()
            assertEquals(listOf(5, 4, 3, 2), db.restaurantDao().getAllRestaurants().first().map { it.id })
        } finally { db.close(); context.deleteDatabase(name) }
    }
}
