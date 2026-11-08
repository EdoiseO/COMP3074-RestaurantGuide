package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.p12.restaurantguide.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val owners = mutableListOf<RestaurantViewModel>()
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { owners.forEach { it.viewModelScope.cancel() }; Dispatchers.resetMain() }
    private fun owner(store: ControlledStore) = RestaurantViewModel(store).also { owners += it }
    private val record = Restaurant(1, "Cafe", "Lunch", "Toronto", "416-555-0100", listOf("Cafe"), 4.5f,
        notes = "Keep this", photoUris = listOf("content://photos/1"), photoCaptions = listOf("Meal | drink"))

    @Test fun loadingIsDistinctFromEmptyAndReadFailuresCanRetry() = runTest(dispatcher) {
        val store = ControlledStore().apply { failReads = true }
        val vm = owner(store)
        assertTrue(vm.state.value.loading)
        runCurrent()
        assertTrue(vm.state.value.loadFailed)
        store.failReads = false
        vm.reload(); runCurrent()
        assertFalse(vm.state.value.loading)
        assertFalse(vm.state.value.loadFailed)
        assertTrue(vm.state.value.restaurants.isEmpty())
    }

    @Test fun duplicateSaveIsIgnoredUntilCommitAndCompletionIsAcknowledged() = runTest(dispatcher) {
        val store = ControlledStore().apply { gate = CompletableDeferred() }
        val vm = owner(store); runCurrent()
        vm.write(RestaurantWrite.ADD, record)
        vm.write(RestaurantWrite.ADD, record)
        runCurrent()
        assertTrue(vm.state.value.saving)
        assertNull(vm.state.value.completed)
        assertEquals(1, store.calls)
        store.gate!!.complete(Unit); runCurrent()
        assertEquals(RestaurantWrite.ADD, vm.state.value.completed)
        assertEquals(record, vm.state.value.restaurants.single())
        vm.write(RestaurantWrite.ADD, record); runCurrent()
        assertEquals(1, store.calls)
        vm.acknowledgeWrite()
        assertNull(vm.state.value.completed)
    }

    @Test fun failedWriteRetainsOriginalAndRetriesExactDraft() = runTest(dispatcher) {
        val store = ControlledStore(listOf(record)).apply { failWrites = true }
        val vm = owner(store); runCurrent()
        val draft = record.copy(name = "Edited Cafe", rating = 2.5f, notes = "Changed")
        vm.write(RestaurantWrite.EDIT, draft); runCurrent()
        assertEquals(RestaurantWriteError.FAILED, vm.state.value.writeError)
        assertNull(vm.state.value.completed)
        assertEquals(record, store.rows.value.single())
        store.failWrites = false
        vm.write(RestaurantWrite.EDIT, draft); runCurrent()
        assertEquals(draft, store.rows.value.single())
        assertEquals(RestaurantWrite.EDIT, vm.state.value.completed)
    }

    @Test fun missingRecordIsReportedAndDeleteFailureDoesNotRemoveIt() = runTest(dispatcher) {
        val store = ControlledStore(listOf(record)).apply { failWrites = true }
        val vm = owner(store); runCurrent()
        vm.write(RestaurantWrite.DELETE, record); runCurrent()
        assertEquals(listOf(record), vm.state.value.restaurants)
        store.failWrites = false; store.rows.value = emptyList()
        vm.write(RestaurantWrite.DELETE, record); runCurrent()
        assertEquals(RestaurantWriteError.MISSING, vm.state.value.writeError)
        assertNull(vm.state.value.completed)
    }

    @Test fun successfulWriteIsNotRetriedWhenObservationLaterFails() = runTest(dispatcher) {
        val store = ControlledStore()
        val vm = owner(store); runCurrent()
        vm.write(RestaurantWrite.ADD, record); runCurrent()
        store.failReads = true; vm.reload(); runCurrent()
        assertTrue(vm.state.value.loadFailed)
        assertNull(vm.state.value.writeError)
        store.failReads = false; vm.reload(); runCurrent()
        assertEquals(1, store.calls)
    }

    @Test fun insertCompletionCarriesIdBeforeDelayedObservationAndReadRetryKeepsIt() = runTest(dispatcher) {
        val store = ControlledStore().apply { delayEmission = true }
        val vm = owner(store); runCurrent()
        vm.write(RestaurantWrite.ADD, record); runCurrent()
        assertEquals(1, vm.state.value.createdId)
        assertTrue(vm.state.value.restaurants.isEmpty())
        assertEquals(RestaurantWrite.ADD, vm.state.value.completed)
        store.failReads = true; vm.reload(); runCurrent()
        assertTrue(vm.state.value.loadFailed)
        assertEquals(1, vm.state.value.createdId)
        store.rows.value = listOf(record)
        store.failReads = false; vm.reload(); runCurrent()
        assertEquals(record, vm.state.value.restaurants.single())
        assertEquals(1, store.calls)
    }

    @Test fun notesEditUsesSavedPhotosWithoutOpeningThem() = runTest(dispatcher) {
        val store = ControlledStore(listOf(record))
        val permissions = UnavailablePhotoGrants()
        val vm = RestaurantViewModel(store, PhotoWriteCoordinator(permissions)).also { owners += it }
        runCurrent()
        vm.write(RestaurantWrite.DETAILS, record.copy(notes = "Edited notes")); runCurrent()
        assertEquals(RestaurantWrite.DETAILS, vm.state.value.completed)
        assertNull(vm.state.value.writeError)
        assertEquals("Edited notes", store.rows.value.single().notes)
        assertTrue(permissions.opened.isEmpty())
    }

    @Test fun addChecksPhotosEvenWhenDraftIdMatchesSavedRestaurant() = runTest(dispatcher) {
        val store = ControlledStore(listOf(record))
        val permissions = UnavailablePhotoGrants()
        val vm = RestaurantViewModel(store, PhotoWriteCoordinator(permissions)).also { owners += it }
        runCurrent()
        vm.write(RestaurantWrite.ADD, record); runCurrent()
        assertEquals(RestaurantWriteError.PHOTO_UNAVAILABLE, vm.state.value.writeError)
        assertNull(vm.state.value.completed)
        assertEquals(record.photoUris, permissions.opened)
        assertEquals(0, store.calls)
    }

    private class UnavailablePhotoGrants : PhotoGrants {
        val opened = mutableListOf<String>()
        override suspend fun readable(reference: String): Boolean { opened += reference; return false }
        override suspend fun persisted() = emptySet<String>()
        override suspend fun take(reference: String): Boolean = error("Must not request an unreadable photo")
        override suspend fun release(reference: String) = Unit
    }

    private class ControlledStore(initial: List<Restaurant> = emptyList()) : RestaurantStore {
        val rows = MutableStateFlow(initial)
        var delayEmission = false
        var failReads = false
        var failWrites = false
        var calls = 0
        var gate: CompletableDeferred<Unit>? = null
        override val allRestaurants: Flow<List<Restaurant>> = flow {
            check(!failReads); emitAll(rows)
        }
        private suspend fun beforeWrite() { calls++; gate?.await(); check(!failWrites) }
        override suspend fun insert(restaurant: Restaurant): Int {
            beforeWrite()
            val id = (rows.value.maxOfOrNull { it.id } ?: 0) + 1
            if (!delayEmission) rows.value = rows.value + restaurant.copy(id = id)
            return id
        }
        override suspend fun update(restaurant: Restaurant) {
            beforeWrite()
            if (rows.value.none { it.id == restaurant.id }) throw RestaurantMissingException()
            rows.value = rows.value.map { if (it.id == restaurant.id) restaurant else it }
        }
        override suspend fun delete(restaurant: Restaurant) {
            beforeWrite()
            if (rows.value.none { it.id == restaurant.id }) throw RestaurantMissingException()
            rows.value = rows.value.filterNot { it.id == restaurant.id }
        }
    }
}
