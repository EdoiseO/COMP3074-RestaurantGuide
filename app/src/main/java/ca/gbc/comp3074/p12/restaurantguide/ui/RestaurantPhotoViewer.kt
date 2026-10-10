package ca.gbc.comp3074.p12.restaurantguide.ui

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import androidx.core.net.toUri
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ca.gbc.comp3074.p12.restaurantguide.R
import java.io.IOException
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** A bundled demo image or a photo explicitly chosen through the system picker. */
data class RestaurantPhoto(
    val resourceId: Int? = null,
    val uri: String? = null,
    val caption: String = "",
    val title: String = "",
)

enum class PhotoLoadStatus { Loading, Ready, Unavailable }

/** Loads selected photos off the UI thread, bounded to the size of their display slot. */
@Composable
fun RestaurantPhotoImage(
    photo: RestaurantPhoto,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
    onStatusChanged: ((PhotoLoadStatus) -> Unit)? = null,
) {
    val resourceId = photo.resourceId
    if (resourceId != null && resourceId > 0) {
        LaunchedEffect(resourceId) { onStatusChanged?.invoke(PhotoLoadStatus.Ready) }
        Image(
            painter = painterResource(resourceId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            alignment = alignment,
        )
        return
    }

    val resolver = LocalContext.current.applicationContext.contentResolver
    val density = LocalDensity.current.density
    BoxWithConstraints(modifier = modifier) {
        val largestSlot = max(maxWidth.value, maxHeight.value) * density
        val decodeSize = if (largestSlot.isFinite()) {
            ceil(largestSlot).toInt().coerceIn(64, MaximumPhotoDimension)
        } else {
            MaximumPhotoDimension
        }
        // A new URI starts with a fresh Loading state; a previous photo's Ready state
        // must never enable Save while its replacement is still being decoded.
        val state by key(photo.uri, resolver, decodeSize) {
            produceState<SelectedPhotoState>(initialValue = SelectedPhotoState.Loading) {
                val bitmap = withContext(Dispatchers.IO) {
                    photo.uri?.takeIf(String::isNotBlank)?.let { uri ->
                        decodeSelectedPhoto(resolver, uri.toUri(), decodeSize)
                    }
                }
                value = if (bitmap == null) SelectedPhotoState.Unavailable else SelectedPhotoState.Ready(bitmap)
            }
        }
        val status = when (state) {
            SelectedPhotoState.Loading -> PhotoLoadStatus.Loading
            SelectedPhotoState.Unavailable -> PhotoLoadStatus.Unavailable
            is SelectedPhotoState.Ready -> PhotoLoadStatus.Ready
        }
        val statusCallback by rememberUpdatedState(onStatusChanged)
        LaunchedEffect(status) {
            statusCallback?.invoke(status)
        }
        when (val current = state) {
            is SelectedPhotoState.Ready -> Image(
                bitmap = current.bitmap,
                contentDescription = contentDescription,
                contentScale = contentScale,
                alignment = alignment,
                modifier = Modifier.fillMaxSize(),
            )
            else -> {
                val status = stringResource(
                    if (current == SelectedPhotoState.Loading) {
                        R.string.photo_viewer_loading
                    } else {
                        R.string.photo_viewer_unavailable
                    },
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .semantics {
                            this.contentDescription = listOfNotNull(contentDescription, status)
                                .joinToString(", ")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(painterResource(R.drawable.ic_photo), null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                        Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp, lineHeight = 16.sp, maxLines = 2, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

/** A compact full-screen gallery: swipe the image, use arrows, or select a thumbnail. */
@Composable
fun RestaurantPhotoViewer(
    restaurantName: String,
    photos: List<RestaurantPhoto>,
    initialIndex: Int,
    onClose: () -> Unit,
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .safeDrawingPadding()
                .testTag("restaurant_photo_viewer"),
        ) {
            if (photos.isEmpty()) {
                PhotoViewerHeader(restaurantName, null, onClose)
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.photo_viewer_empty), color = Color.White)
                }
            } else {
                val pager = rememberPagerState(
                    initialPage = initialIndex.coerceIn(0, photos.lastIndex),
                    pageCount = { photos.size },
                )
                val scope = rememberCoroutineScope()
                val thumbnails = rememberLazyListState(
                    initialFirstVisibleItemIndex = initialIndex.coerceIn(0, photos.lastIndex),
                )
                LaunchedEffect(pager.currentPage) {
                    thumbnails.animateScrollToItem(pager.currentPage)
                }
                PhotoViewerHeader(
                    photos[pager.currentPage].title.ifBlank { restaurantName },
                    stringResource(R.string.photo_viewer_count, pager.currentPage + 1, photos.size),
                    onClose,
                )
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    HorizontalPager(
                        state = pager,
                        modifier = Modifier.fillMaxSize().testTag("restaurant_photo_pager"),
                    ) { index ->
                        RestaurantPhotoImage(
                            photo = photos[index],
                            contentDescription = stringResource(
                                R.string.photo_viewer_photo_description,
                                restaurantName,
                                index + 1,
                                photos.size,
                            ),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(vertical = 12.dp),
                        )
                    }
                    Row(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        PhotoNavigationButton(
                            text = "‹",
                            description = stringResource(R.string.photo_viewer_previous),
                            enabled = photos.size > 1,
                        ) {
                            val previous = (pager.currentPage - 1 + photos.size) % photos.size
                            scope.launch { pager.animateScrollToPage(previous) }
                        }
                        PhotoNavigationButton(
                            text = "›",
                            description = stringResource(R.string.photo_viewer_next),
                            enabled = photos.size > 1,
                        ) {
                            val next = (pager.currentPage + 1) % photos.size
                            scope.launch { pager.animateScrollToPage(next) }
                        }
                    }
                }
                photos[pager.currentPage].caption.takeIf(String::isNotBlank)?.let { caption ->
                    key(pager.currentPage) { Text(
                        text = caption,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 120.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .testTag("photo-viewer-caption"),
                    ) }
                }
                Text(stringResource(R.string.photo_viewer_guidance), color = Color(0xFFB9B5BD),
                    fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    textAlign = TextAlign.Center)
                LazyRow(
                    state = thumbnails,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    itemsIndexed(photos) { index, photo ->
                        val isActive = index == pager.currentPage
                        val description = stringResource(
                            R.string.photo_viewer_thumbnail_description,
                            index + 1,
                            photos.size,
                        )
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    BorderStroke(
                                        if (isActive) 2.dp else 1.dp,
                                        if (isActive) Color.White else Color(0xFF49454F),
                                    ),
                                    RoundedCornerShape(14.dp),
                                )
                                .clickable(role = Role.Button) {
                                    scope.launch { pager.animateScrollToPage(index) }
                                }
                                .semantics {
                                    contentDescription = description
                                    selected = isActive
                                }
                                .padding(if (isActive) 3.dp else 2.dp),
                        ) {
                            RestaurantPhotoImage(
                                photo = photo,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(11.dp)),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoViewerHeader(name: String, count: String?, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f).padding(top = 4.dp)) {
            Text(
                text = name,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            count?.let {
                Text(it, color = Color(0xFFB9B5BD), style = MaterialTheme.typography.bodyMedium)
            }
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(48.dp)
                .border(1.dp, Color(0xFF49454F), CircleShape)
                .background(Color(0xFF151318), CircleShape),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.photo_viewer_close),
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun PhotoNavigationButton(
    text: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(48.dp)
            .border(1.dp, Color(0xFF49454F), CircleShape)
            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
            .semantics { contentDescription = description },
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color(0xFF555158),
            fontSize = 36.sp,
        )
    }
}

private sealed interface SelectedPhotoState {
    data object Loading : SelectedPhotoState
    data object Unavailable : SelectedPhotoState
    data class Ready(val bitmap: ImageBitmap) : SelectedPhotoState
}

private const val MaximumPhotoDimension = 1280

private fun decodeSelectedPhoto(resolver: ContentResolver, uri: Uri, size: Int): ImageBitmap? {
    return try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                val ratio = max(info.size.width, info.size.height).toFloat() / size
                if (ratio > 1f) {
                    decoder.setTargetSize(
                        (info.size.width / ratio).roundToInt().coerceAtLeast(1),
                        (info.size.height / ratio).roundToInt().coerceAtLeast(1),
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            decodeLegacyPhoto(resolver, uri, size)
        }
        bitmap?.asImageBitmap()
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: UnsupportedOperationException) {
        null
    }
}

private fun decodeLegacyPhoto(resolver: ContentResolver, uri: Uri, size: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sample = 1
    while (max(bounds.outWidth, bounds.outHeight) / sample > size) sample *= 2
    val options = BitmapFactory.Options().apply {
        inSampleSize = sample
        inScaled = false
    }
    val decoded = resolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, options)
    } ?: return null
    val orientation = try {
        resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        } ?: ExifInterface.ORIENTATION_NORMAL
    } catch (_: IOException) {
        ExifInterface.ORIENTATION_NORMAL
    }
    val transform = Matrix().apply {
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> postScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                postScale(-1f, 1f)
                postRotate(90f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                postScale(-1f, 1f)
                postRotate(270f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
        }
    }
    if (transform.isIdentity) return decoded
    val rotated = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, transform, true)
    if (rotated !== decoded) decoded.recycle()
    return rotated
}
