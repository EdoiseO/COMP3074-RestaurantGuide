package ca.gbc.comp3074.p12.restaurantguide.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RestaurantRatingTest {
    @Test
    fun draggingAcrossTheRowSelectsEveryHalfStar() {
        (1..10).forEach { halfStar ->
            assertEquals(halfStar / 2f, snapRatingFromPosition(halfStar * 20f, 200f), 0f)
        }
    }

    @Test
    fun draggingSnapsToTheNearestHalfStar() {
        assertEquals(1f, snapRatingFromPosition(49f, 200f), 0f)
        assertEquals(1.5f, snapRatingFromPosition(50f, 200f), 0f)
        assertEquals(1.5f, snapRatingFromPosition(51f, 200f), 0f)
    }

    @Test
    fun draggingOutsideTheStarsClampsToTheRatingLimits() {
        assertEquals(0.5f, snapRatingFromPosition(-100f, 200f), 0f)
        assertEquals(0.5f, snapRatingFromPosition(0f, 200f), 0f)
        assertEquals(5f, snapRatingFromPosition(300f, 200f), 0f)
    }

    @Test
    fun unmeasuredOrInvalidCoordinatesDoNotProduceAnInvalidRating() {
        assertEquals(0.5f, snapRatingFromPosition(10f, 0f), 0f)
        assertEquals(0.5f, snapRatingFromPosition(10f, -20f), 0f)
        assertEquals(0.5f, snapRatingFromPosition(Float.NaN, 200f), 0f)
        assertEquals(0.5f, snapRatingFromPosition(10f, Float.POSITIVE_INFINITY), 0f)
    }

    @Test
    fun wholeAndFractionalRatingsHaveConsistentOneDecimalLabels() {
        assertEquals("4.0", formatRating(4f))
        assertEquals("4.5", formatRating(4.5f))
        assertEquals("0.5", formatRating(0.5f))
        assertEquals("0.0", formatRating(0f))
    }
}
