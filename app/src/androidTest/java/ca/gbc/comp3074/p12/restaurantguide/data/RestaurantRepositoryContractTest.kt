package ca.gbc.comp3074.p12.restaurantguide.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class RestaurantRepositoryContractTest {
    @Test fun basicCrudCommitsAcrossDatabaseReopenAndRejectsMissingIds() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "repository-contract-${UUID.randomUUID()}.db"
        fun open() = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        var database = open()
        try {
            var store: RestaurantStore = RestaurantRepository(database.restaurantDao())
            val draft = Restaurant(999, "Cafe", "Lunch", "Toronto", "416-555-0100\n437-555-0100",
                listOf("Cafe", "Lunch"), 4.5f, notes = "First visit\nTry again",
                photoUris = listOf("content://photos/1"), photoCaptions = listOf("Lunch"))
            val id = store.insert(draft)
            assertTrue(id > 0)
            assertNotEquals(999, id)
            val saved = draft.copy(id = id)
            assertEquals(saved, store.observeRestaurant(id).first())
            database.close(); database = open()
            store = RestaurantRepository(database.restaurantDao())
            assertEquals(saved, store.observeRestaurant(id).first())
            val changed = saved.copy(notes = "Second visit", rating = 2.5f)
            store.update(changed)
            assertEquals(changed, store.observeRestaurant(id).first())
            store.delete(changed)
            database.close(); database = open()
            store = RestaurantRepository(database.restaurantDao())
            assertTrue(store.allRestaurants.first().isEmpty())
            try { store.update(changed); fail("Missing update must fail") }
            catch (_: RestaurantMissingException) { }
            try { store.delete(changed); fail("Missing delete must fail") }
            catch (_: RestaurantMissingException) { }
        } finally {
            database.close()
            context.deleteDatabase(name)
        }
    }
}
