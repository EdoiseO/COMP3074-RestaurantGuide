package ca.gbc.comp3074.p12.restaurantguide.data

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal interface PhotoGrants {
    suspend fun readable(reference: String): Boolean
    suspend fun persisted(): Set<String>
    suspend fun take(reference: String): Boolean
    suspend fun release(reference: String)
}

class PhotoUnavailableException : IllegalStateException("Photo is no longer readable")
data class PhotoWriteResult<T>(val value: T, val sessionOnly: Boolean)

/** One application-scoped coordinator serializes grant acquisition, durable writes and cleanup.
 * Draft selection takes no grants. All saved covers/albums protect their shared grants.
 */
class PhotoWriteCoordinator internal constructor(private val grants: PhotoGrants) {
    private val mutex = Mutex()

    suspend fun <T> commit(store: RestaurantStore, references: Set<String>, write: suspend () -> T): PhotoWriteResult<T> =
        mutex.withLock {
            val before = store.allRestaurants.first()
            val existing = grants.persisted()
            var sessionOnly = false
            try {
                for (reference in references) {
                    if (!grants.readable(reference)) throw PhotoUnavailableException()
                    if (reference !in existing && !grants.take(reference)) sessionOnly = true
                }
                val result = write()
                // A cleanup/read error after commit must never turn a successful Add into a
                // retryable failure. Leave grants in place and retry cleanup on next open/write.
                try { cleanup(store.allRestaurants.first(), references) } catch (_: Exception) { }
                PhotoWriteResult(result, sessionOnly)
            } catch (error: Exception) {
                withContext(NonCancellable) {
                    // Preserve old grants even if this attempt failed. Only roll back new grants
                    // not referenced by the saved database. Never remove provider-owned images.
                    val saved = try { store.allRestaurants.first() } catch (_: Exception) { before }
                    try {
                        val protected = saved.flatMap { it.photoReferences() }.toSet()
                        (grants.persisted() - existing - protected).forEach { grants.release(it) }
                    } catch (_: Exception) { }
                }
                throw error
            }
        }

    suspend fun reconcile(store: RestaurantStore) = mutex.withLock { cleanup(store.allRestaurants.first()) }

    private suspend fun cleanup(saved: List<Restaurant>, pending: Set<String> = emptySet()) {
        val references = saved.flatMap { it.photoReferences() }.toSet() + pending
        (grants.persisted() - references).forEach { grants.release(it) }
    }
}
