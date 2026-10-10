package ca.gbc.comp3074.p12.restaurantguide.data

import java.util.Locale
import kotlin.math.roundToInt

/** The same fractional label is used in cards, Details, forms and email drafts. */
fun formatRating(rating: Float): String = String.format(Locale.ROOT, "%.1f", rating)

/** Maps a finger's horizontal position across the stars to a half-step rating. */
fun snapRatingFromPosition(position: Float, width: Float): Float {
    if (!position.isFinite() || !width.isFinite() || width <= 0f) return 0.5f
    val value = position.coerceIn(0f, width) / width * 5f
    return ((value * 2f).roundToInt() / 2f).coerceIn(0.5f, 5f)
}
