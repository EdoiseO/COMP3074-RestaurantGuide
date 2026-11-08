package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import ca.gbc.comp3074.p12.restaurantguide.data.RestaurantMissingException
import ca.gbc.comp3074.p12.restaurantguide.data.RestaurantStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class RestaurantWrite { ADD, EDIT, DETAILS, DELETE }
enum class RestaurantWriteError { FAILED, MISSING }

data class RestaurantUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val loading: Boolean = true,
    val hasLoaded: Boolean = false,
    val createdId: Int? = null,
    val loadFailed: Boolean = false,
    val saving: Boolean = false,
    val writeError: RestaurantWriteError? = null,
    val completed: RestaurantWrite? = null,
    val revision: Int = 0,
)

/** Navigation remains in Compose; this owner keeps an in-flight write alive across rotation. */
class RestaurantViewModel(private val repository: RestaurantStore) : ViewModel() {
    private val mutableState = MutableStateFlow(RestaurantUiState())
    val state = mutableState.asStateFlow()
    private var observation: Job? = null

    init { reload() }

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
                val createdId = if (kind == RestaurantWrite.ADD) repository.insert(restaurant) else null
                when (kind) {
                    RestaurantWrite.ADD -> Unit
                    RestaurantWrite.EDIT, RestaurantWrite.DETAILS -> repository.update(restaurant)
                    RestaurantWrite.DELETE -> repository.delete(restaurant)
                }
                // Navigation is allowed only after the durable write. Observation supplies records;
                // don't turn a later refresh error into a retryable write (which could duplicate Add).
                mutableState.update { it.copy(saving = false, completed = kind, createdId = createdId, revision = it.revision + 1) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.update { it.copy(saving = false, writeError =
                    if (error is RestaurantMissingException) RestaurantWriteError.MISSING else RestaurantWriteError.FAILED) }
            }
        }
    }

    fun dismissWriteError() {
        mutableState.update { it.copy(writeError = null) }
    }
    fun acknowledgeWrite() { mutableState.update { it.copy(completed = null, createdId = null) } }
}
