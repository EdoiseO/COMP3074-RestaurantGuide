package ca.gbc.comp3074.p12.restaurantguide.data

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.annotation.MainThread
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/** Aligns older optional captions before adding a new visit photo and its own caption. */
fun Restaurant.withVisitPhoto(uri: String, caption: String): Restaurant = copy(
    photoUris = photoUris + uri,
    photoCaptions = photoUris.indices.map { photoCaptions.getOrElse(it) { "" } } + caption,
)

internal fun Restaurant.photoReferences(): Set<String> =
    (photoUris + coverUri).filter(String::isNotBlank).toSet()

/**
 * Checks sources off the UI thread, then acquires grants on the main thread so commits cannot
 * interleave with a rollback. The caller should publish the new references before suspending.
 */
@MainThread
internal suspend fun persistPhotoReadAccess(resolver: ContentResolver, references: Collection<String>): Boolean {
    if (references.isEmpty()) return true
    val uris = references.map(String::toUri).distinct()
    if (!withContext(Dispatchers.IO) { uris.all { resolver.canReadPhoto(it) } }) return false

    val existing = resolver.persistedUriPermissions.filter { it.isReadPermission }.map { it.uri }.toSet()
    val acquired = mutableListOf<Uri>()
    val read = Intent.FLAG_GRANT_READ_URI_PERMISSION
    try {
        for (uri in uris) {
            if (uri !in existing) {
                resolver.takePersistableUriPermission(uri, read)
                acquired.add(uri)
            }
        }
    } catch (_: SecurityException) {
        // A pre-existing grant may belong to a saved cover or another album entry.
        for (uri in acquired) {
            try {
                resolver.releasePersistableUriPermission(uri, read)
            } catch (_: SecurityException) {
                // The grant is already gone.
            }
        }
        return false
    }
    return true
}

private fun ContentResolver.canReadPhoto(uri: Uri): Boolean {
    if (uri.scheme != ContentResolver.SCHEME_CONTENT) return false
    return try {
        openInputStream(uri)?.use { it.read() != -1 } == true
    } catch (_: IOException) {
        false
    } catch (_: SecurityException) {
        false
    } catch (_: IllegalArgumentException) {
        false
    }
}
