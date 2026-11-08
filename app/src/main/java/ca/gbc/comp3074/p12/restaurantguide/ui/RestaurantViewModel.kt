package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import ca.gbc.comp3074.p12.restaurantguide.data.PhotoWriteCoordinator
import ca.gbc.comp3074.p12.restaurantguide.data.PhotoUnavailableException
import ca.gbc.comp3074.p12.restaurantguide.data.PhotoWriteResult
import ca.gbc.comp3074.p12.restaurantguide.data.photoReferences
import ca.gbc.comp3074.p12.restaurantguide.data.RestaurantMissingException
import ca.gbc.comp3074.p12.restaurantguide.data.RestaurantStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RestaurantWrite { ADD, EDIT, DETAILS, DELETE }
enum class RestaurantWriteError { FAILED, MISSING, PHOTO_UNAVAILABLE }

data class RestaurantUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val loading: Boolean = true,
    val hasLoaded: Boolean = false,
    val createdId: Int? = null,
    val loadFailed: Boolean = false,
    val saving: Boolean = false,
    val sessionPhotoWarning: Boolean = false,
    val writeError: RestaurantWriteError? = null,
    val completed: RestaurantWrite? = null,
    val revision: Int = 0,
)

/** Navigation remains in Compose; this owner keeps an in-flight write alive across rotation. */
class RestaurantViewModel(
    private val repository: RestaurantStore,
    private val photoAccess: PhotoWriteCoordinator? = null,
) : ViewModel() {
    private val mutableState = MutableStateFlow(RestaurantUiState())
    val state = mutableState.asStateFlow()
    private var observation: Job? = null

    init {
        reload()
        viewModelScope.launch {
            try { photoAccess?.reconcile(repository) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { /* Retry cleanup on the next successful write/open. */ }
        }
    }

    fun reload() {
        observation?.cancel()
        mutableState.update { it.copy(loading = true, loadFailed = false) }
        observation = viewModelScope.launch {
            try {
                repository.allRestaurants.collect { rows ->
                    mutableState.update { it.copy(restaurants = rows, loading = false, loadFailed = false, hasLoaded = true) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableState.update { it.copy(loading = false, loadFailed = true) }
            }
        }
    }

    fun write(kind: RestaurantWrite, restaurant: Restaurant) {
        if (state.value.saving || state.value.completed != null) return
        mutableState.update { it.copy(saving = true, writeError = null) }
        viewModelScope.launch {
            try {
                val commit: suspend () -> Int? = {
                    when (kind) {
                        RestaurantWrite.ADD -> repository.insert(restaurant)
                        RestaurantWrite.EDIT, RestaurantWrite.DETAILS -> { repository.update(restaurant); null }
                        RestaurantWrite.DELETE -> { repository.delete(restaurant); null }
                    }
                }
                val result = photoAccess?.commit(
                    store = repository,
                    references = if (kind == RestaurantWrite.DELETE) emptySet() else restaurant.photoReferences(),
                    savedRestaurantId = if (kind == RestaurantWrite.ADD) null else restaurant.id,
                    write = commit,
                )
                    ?: PhotoWriteResult(commit(), false)
                val createdId = result.value
                // Navigation is allowed only after the durable write. Observation supplies records;
                // don't turn a later refresh error into a retryable write (which could duplicate Add).
                mutableState.update { it.copy(saving = false, completed = kind, createdId = createdId, sessionPhotoWarning = result.sessionOnly, revision = it.revision + 1) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.update { it.copy(saving = false, writeError =
                    when (error) {
                        is RestaurantMissingException -> RestaurantWriteError.MISSING
                        is PhotoUnavailableException -> RestaurantWriteError.PHOTO_UNAVAILABLE
                        else -> RestaurantWriteError.FAILED
                    }) }
            }
        }
    }

    fun dismissWriteError() {
        mutableState.update { it.copy(writeError = null) }
    }
    fun dismissPhotoWarning() { mutableState.update { it.copy(sessionPhotoWarning = false) } }
    fun acknowledgeWrite() { mutableState.update { it.copy(completed = null, createdId = null) } }
}
