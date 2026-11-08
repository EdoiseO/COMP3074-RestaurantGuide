package ca.gbc.comp3074.p12.restaurantguide.data

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoWriteCoordinatorTest {
    private val photo = "content://photos/new"
    private val old = "content://photos/shared"
    private fun record(id: Int, uri: String) = Restaurant(id, "Cafe", "", "", "", emptyList(), 4.5f,
        coverUri = uri, photoUris = listOf(uri), photoCaptions = listOf("Caption | comma,"))

    @Test fun acquiresBeforeCommitAndReleasesOnlyUnreferencedGrants() = runTest {
        val grants = Grants().apply { held += old }
        val store = Store(listOf(record(1, old), record(2, old)))
        val coordinator = PhotoWriteCoordinator(grants)
        coordinator.commit(store, setOf(photo)) {
            assertTrue(photo in grants.held)
            store.update(record(1, photo))
        }
        assertEquals(setOf(old, photo), grants.held)
        coordinator.commit(store, emptySet()) { store.delete(record(2, old)) }
        assertEquals(setOf(photo), grants.held)
        coordinator.commit(store, emptySet()) { store.delete(record(1, photo)) }
        assertTrue(grants.held.isEmpty())
    }

    @Test fun readableTemporaryPhotoCommitsWithWarning() = runTest {
        val grants = Grants().apply { durable = false }
        val store = Store()
        val result = PhotoWriteCoordinator(grants).commit(store, setOf(photo)) { store.insert(record(1, photo)) }
        assertTrue(result.sessionOnly)
        assertEquals(photo, store.allRestaurants.value.single().coverUri)
    }

    @Test fun unreadablePhotoDoesNotWriteAndRollsBackNewGrant() = runTest {
        val grants = Grants().apply { unreadable = old }
        val store = Store()
        var wrote = false
        try {
            PhotoWriteCoordinator(grants).commit(store, linkedSetOf(photo, old)) { wrote = true }
            fail("Expected unavailable photo")
        } catch (_: PhotoUnavailableException) { }
        assertFalse(wrote)
        assertTrue(grants.held.isEmpty())
    }

    @Test fun writeFailureRollsBackOnlyNewUnusedGrantsAndRetryCanAcquireAgain() = runTest {
        val grants = Grants().apply { held += old }
        val store = Store(listOf(record(1, old)))
        val coordinator = PhotoWriteCoordinator(grants)
        try { coordinator.commit(store, setOf(photo)) { error("Disk full") }; fail("Expected write error") }
        catch (_: IllegalStateException) { }
        assertEquals(setOf(old), grants.held)
        coordinator.commit(store, setOf(photo)) { store.update(record(1, photo)) }
        assertEquals(setOf(photo), grants.held)
    }

    @Test fun cleanupWaitsForPendingWriteAndCancellationRollsBackItsGrant() = runTest {
        val grants = Grants()
        val store = Store()
        val coordinator = PhotoWriteCoordinator(grants)
        val gate = CompletableDeferred<Unit>()
        val job = launch { coordinator.commit(store, setOf(photo)) { gate.await(); store.insert(record(1, photo)) } }
        runCurrent()
        assertEquals(setOf(photo), grants.held)
        val cleanup = launch { coordinator.reconcile(store) }
        runCurrent()
        assertFalse(cleanup.isCompleted)
        job.cancelAndJoin()
        cleanup.join()
        assertTrue(grants.held.isEmpty())
    }

    @Test fun cleanupFailureAfterCommitDoesNotReportSaveFailure() = runTest {
        val grants = Grants().apply { held += old; failRelease = true }
        val store = Store()
        val result = PhotoWriteCoordinator(grants).commit(store, setOf(photo)) { store.insert(record(1, photo)) }
        assertEquals(1, result.value)
        assertEquals(1, store.allRestaurants.value.size)
    }

    private class Grants : PhotoGrants {
        val held = mutableSetOf<String>()
        var durable = true
        var unreadable: String? = null
        var failRelease = false
        override suspend fun readable(reference: String) = reference != unreadable
        override suspend fun persisted() = held.toSet()
        override suspend fun take(reference: String): Boolean { if (durable) held += reference; return durable }
        override suspend fun release(reference: String) { check(!failRelease); held -= reference }
    }
    private class Store(initial: List<Restaurant> = emptyList()) : RestaurantStore {
        override val allRestaurants = MutableStateFlow(initial)
        override suspend fun insert(restaurant: Restaurant): Int { allRestaurants.value += restaurant; return restaurant.id }
        override suspend fun update(restaurant: Restaurant) { allRestaurants.value = allRestaurants.value.map { if (it.id == restaurant.id) restaurant else it } }
        override suspend fun delete(restaurant: Restaurant) { allRestaurants.value = allRestaurants.value.filterNot { it.id == restaurant.id } }
    }
}
