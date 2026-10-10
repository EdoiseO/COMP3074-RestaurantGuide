package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ca.gbc.comp3074.p12.restaurantguide.R
import ca.gbc.comp3074.p12.restaurantguide.data.formatRating
import ca.gbc.comp3074.p12.restaurantguide.data.snapRatingFromPosition
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** Drag directly across the stars; every half star has a stable numeric value. */
@Composable
fun StarRating(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    interactive: Boolean = true,
    starSize: Dp = 44.dp,
) {
    val displayedRating = if (rating.isFinite()) rating.coerceIn(0f, 5f) else 0f
    val latestOnRatingChange by rememberUpdatedState(onRatingChange)
    val label = stringResource(if (interactive) R.string.rating_control_label else R.string.rating_display_label)
    val description = if (displayedRating == 0f) {
        stringResource(R.string.rating_unrated)
    } else {
        stringResource(R.string.rating_accessible_value, formatRating(displayedRating))
    }
    var ratingModifier = modifier
        .width(starSize * 5)
        .height(if (interactive) maxOf(48.dp, starSize) else starSize)
        .semantics {
            contentDescription = label
            stateDescription = description
            if (interactive) {
                progressBarRangeInfo = ProgressBarRangeInfo(displayedRating, 0f..5f, steps = 9)
                setProgress { requested ->
                    if (!requested.isFinite() || (displayedRating == 0f && requested <= 0f)) {
                        false
                    } else {
                        val snapped = ((requested.coerceIn(0.5f, 5f) * 2f).roundToInt() / 2f)
                            .coerceIn(0.5f, 5f)
                        if (snapped == displayedRating) {
                            false
                        } else {
                            latestOnRatingChange(snapped)
                            true
                        }
                    }
                }
            }
        }
    if (interactive) {
        ratingModifier = ratingModifier
            .pointerInput(Unit) {
                detectTapGestures { position ->
                    latestOnRatingChange(snapRatingFromPosition(position.x, size.width.toFloat()))
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { position ->
                        latestOnRatingChange(snapRatingFromPosition(position.x, size.width.toFloat()))
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        latestOnRatingChange(snapRatingFromPosition(change.position.x, size.width.toFloat()))
                    },
                )
            }
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) {
                    false
                } else {
                    val adjusted = when (event.key) {
                        Key.DirectionRight, Key.DirectionUp -> (displayedRating + 0.5f).coerceIn(0.5f, 5f)
                        Key.DirectionLeft, Key.DirectionDown -> if (displayedRating == 0f) {
                            0f
                        } else {
                            (displayedRating - 0.5f).coerceIn(0.5f, 5f)
                        }
                        Key.MoveHome -> 0.5f
                        Key.MoveEnd -> 5f
                        else -> null
                    }
                    adjusted?.let { latestOnRatingChange(it); true } ?: false
                }
            }
            .focusable()
    }
    Canvas(ratingModifier) {
        val cellWidth = size.width / 5f
        val starDiameter = min(cellWidth, starSize.toPx())
        val outerRadius = (starDiameter / 2f - 2.dp.toPx()).coerceAtLeast(1f)
        val innerRadius = outerRadius * 0.45f
        val starColor = Color(0xFFF4B400)
        repeat(5) { index ->
            val center = Offset(cellWidth * (index + 0.5f), size.height / 2f)
            val path = Path().apply {
                repeat(10) { point ->
                    val angle = -Math.PI / 2.0 + point * Math.PI / 5.0
                    val radius = if (point % 2 == 0) outerRadius else innerRadius
                    val x = center.x + cos(angle).toFloat() * radius
                    val y = center.y + sin(angle).toFloat() * radius
                    if (point == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            val fraction = (displayedRating - index).coerceIn(0f, 1f)
            if (fraction > 0f) {
                clipRect(
                    left = center.x - outerRadius,
                    right = center.x - outerRadius + outerRadius * 2f * fraction,
                ) {
                    drawPath(path, starColor)
                }
            }
            drawPath(path, starColor, style = Stroke(width = 1.2.dp.toPx(), join = StrokeJoin.Round))
        }
    }
}
