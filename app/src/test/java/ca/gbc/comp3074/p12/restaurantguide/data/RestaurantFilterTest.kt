package ca.gbc.comp3074.p12.restaurantguide.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RestaurantFilterTest {
    private val restaurants = DemoRestaurants.restaurants

    @Test
    fun allResetWithBlankSearchReturnsEveryRestaurantInOriginalOrder() {
        val result = filterRestaurants(restaurants, "   ", emptySet())

        assertEquals(restaurants, result)
    }

    @Test
    fun nameSearchIgnoresCaseAndSurroundingWhitespace() {
        val result = filterRestaurants(restaurants, "  bRoThErS  ", emptySet())

        assertEquals(listOf(1), result.map { it.id })
    }

    @Test
    fun selectedTagsMatchAnyCategoryRatherThanRequiringEveryCategory() {
        val result = filterRestaurants(restaurants, "", setOf("Shawarma", "Mexican"))

        assertEquals(listOf(1, 3), result.map { it.id })
    }

    @Test
    fun selectingSeveralTagsOnOneRestaurantDoesNotDuplicateItsCard() {
        val result = filterRestaurants(restaurants, "", setOf("Shawarma", "Falafel"))

        assertEquals(listOf(1), result.map { it.id })
    }

    @Test
    fun nameSearchAndSelectedTagsBothRestrictResults() {
        val result = filterRestaurants(restaurants, "riza", setOf("Shawarma", "Turkish"))
        val mismatchedCategory = filterRestaurants(restaurants, "riza", setOf("Mexican"))

        assertEquals(listOf(2), result.map { it.id })
        assertTrue(mismatchedCategory.isEmpty())
    }

    @Test
    fun tagComparisonIgnoresCase() {
        val result = filterRestaurants(restaurants, "", setOf("mExIcAn"))

        assertEquals(listOf(3), result.map { it.id })
    }

    @Test
    fun unmatchedNameOrTagProducesNoResults() {
        assertTrue(filterRestaurants(restaurants, "does not exist", emptySet()).isEmpty())
        assertTrue(filterRestaurants(restaurants, "", setOf("Italian")).isEmpty())
    }

    @Test
    fun searchMatchesTheNameRatherThanDescription() {
        val result = filterRestaurants(restaurants, "Turkish", emptySet())

        assertTrue(result.isEmpty())
    }

    @Test
    fun anEmptySavedListStaysEmptyRegardlessOfFilters() {
        assertTrue(filterRestaurants(emptyList(), "", emptySet()).isEmpty())
        assertTrue(filterRestaurants(emptyList(), "Shawarma", setOf("Shawarma")).isEmpty())
    }
}
