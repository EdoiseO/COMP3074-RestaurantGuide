package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.unit.IntSize
import kotlin.math.min

internal const val MaximumPhotoZoom = 3f
internal const val PhotoZoomStep = .5f

/** Diagnostic semantics let device checks observe the actual gesture offset and its bounds. */
internal val PhotoPanOffsetKey = SemanticsPropertyKey<Offset>("Photo pan offset")
internal val PhotoPanLimitKey = SemanticsPropertyKey<Offset>("Photo pan limit")

@Stable
internal class PhotoZoomState {
    var scale by mutableFloatStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set
    var ready by mutableStateOf(false)
        private set
    private var viewport by mutableStateOf(IntSize.Zero)
    private var image by mutableStateOf(IntSize.Zero)

    val enlarged: Boolean get() = scale > 1f
    val panLimit: Offset
        get() {
            if (viewport.width == 0 || viewport.height == 0 || image.width == 0 || image.height == 0) {
                return Offset.Zero
            }
            val fit = min(viewport.width.toFloat() / image.width, viewport.height.toFloat() / image.height)
            return Offset(
                ((image.width * fit * scale - viewport.width) / 2f).coerceAtLeast(0f),
                ((image.height * fit * scale - viewport.height) / 2f).coerceAtLeast(0f),
            )
        }

    fun updateReady(value: Boolean) {
        ready = value
        if (!value) reset()
    }

    fun updateViewport(value: IntSize) {
        viewport = value
        offset = bounded(offset)
    }

    fun updateImage(value: IntSize) {
        image = value
        offset = bounded(offset)
    }

    fun updateScale(value: Float) {
        if (!ready || !value.isFinite()) return
        scale = normalizedScale(value)
        offset = if (scale == 1f) Offset.Zero else bounded(offset)
    }

    fun reset() {
        scale = 1f
        offset = Offset.Zero
    }

    fun panBy(value: Offset) {
        if (ready && enlarged) offset = bounded(offset + value)
    }

    fun transform(centroid: Offset, pan: Offset, zoom: Float) {
        if (!ready || !zoom.isFinite()) return
        val oldScale = scale
        val nextScale = normalizedScale(oldScale * zoom)
        val relativeFocus = centroid - Offset(viewport.width / 2f, viewport.height / 2f)
        val nextOffset = relativeFocus - (relativeFocus - offset) * (nextScale / oldScale) + pan
        scale = nextScale
        offset = if (nextScale == 1f) Offset.Zero else bounded(nextOffset)
    }

    private fun normalizedScale(value: Float): Float = value.coerceIn(1f, MaximumPhotoZoom)
        .let { if (it < 1.01f) 1f else it }

    private fun bounded(value: Offset): Offset {
        val limit = panLimit
        return Offset(value.x.coerceIn(-limit.x, limit.x), value.y.coerceIn(-limit.y, limit.y))
    }
}

/** At 100% one finger belongs to the pager; a pinch or enlarged photo owns its image gesture. */
internal fun Modifier.photoZoomGestures(state: PhotoZoomState): Modifier = pointerInput(state) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        var ownsGesture = state.enlarged
        var pagerOwnsGesture = false
        do {
            val event = awaitPointerEvent()
            val multiTouch = event.changes.count { it.pressed } > 1
            if (!ownsGesture && event.changes.any { it.isConsumed }) pagerOwnsGesture = true
            if (state.ready && !pagerOwnsGesture && (ownsGesture || multiTouch)) {
                ownsGesture = true
                val centroid = event.calculateCentroid(useCurrent = false)
                if (centroid != Offset.Unspecified) {
                    state.transform(centroid, event.calculatePan(), event.calculateZoom())
                }
                event.changes.forEach { if (it.positionChanged()) it.consume() }
            }
        } while (event.changes.any { it.pressed })
    }
}
