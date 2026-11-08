package ca.gbc.comp3074.p12.restaurantguide.data

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
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

object RestaurantPhotoAccess {
    @Volatile private var coordinator: PhotoWriteCoordinator? = null
    fun get(context: Context): PhotoWriteCoordinator = coordinator ?: synchronized(this) {
        coordinator ?: PhotoWriteCoordinator(AndroidPhotoGrants(context.applicationContext.contentResolver))
            .also { coordinator = it }
    }
}

internal class AndroidPhotoGrants(private val resolver: ContentResolver) : PhotoGrants {
    override suspend fun readable(reference: String): Boolean = withContext(Dispatchers.IO) {
        val uri = reference.toUri()
        if (uri.scheme != ContentResolver.SCHEME_CONTENT) return@withContext false
        try { resolver.openInputStream(uri)?.use { it.read() != -1 } == true }
        catch (_: IOException) { false }
        catch (_: SecurityException) { false }
        catch (_: IllegalArgumentException) { false }
    }
    override suspend fun persisted(): Set<String> = withContext(Dispatchers.IO) {
        resolver.persistedUriPermissions.filter { it.isReadPermission }.map { it.uri.toString() }.toSet()
    }
    override suspend fun take(reference: String): Boolean = withContext(Dispatchers.IO) {
        try {
            resolver.takePersistableUriPermission(reference.toUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION)
            true
        } catch (_: SecurityException) { false }
        catch (_: IllegalArgumentException) { false }
    }
    override suspend fun release(reference: String) = withContext(Dispatchers.IO) {
        try { resolver.releasePersistableUriPermission(reference.toUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        catch (_: SecurityException) { /* Already revoked. */ }
    }
}
