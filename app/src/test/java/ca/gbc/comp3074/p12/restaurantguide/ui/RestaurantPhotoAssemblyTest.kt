package ca.gbc.comp3074.p12.restaurantguide.ui

import ca.gbc.comp3074.p12.restaurantguide.R
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RestaurantPhotoAssemblyTest {
    private val restaurant = Restaurant(1, "Two Brothers Shawarma", "", "", "", emptyList(), 4f,
        photoRes = R.drawable.two_brothers_meal,
        galleryRes = listOf(R.drawable.two_brothers_meal, R.drawable.two_brothers_chicken))

    @Test fun removingCoverFromAlbumKeepsHeroWithoutReinsertingThumbnail() {
        val removed = restaurant.copy(galleryRes = listOf(R.drawable.two_brothers_chicken))
        assertEquals(listOf(R.drawable.two_brothers_chicken), restaurantPhotos(removed).map { it.resourceId })
        assertEquals(R.drawable.two_brothers_meal, restaurantCoverPhoto(removed)?.resourceId)
        assertEquals(listOf(R.drawable.two_brothers_meal, R.drawable.two_brothers_chicken),
            restaurantCoverGallery(removed).map { it.resourceId })
    }

    @Test fun chosenCoverOverridesBundledHeroAndDoesNotAutomaticallyJoinAlbum() {
        val changed = restaurant.copy(coverUri = "content://photos/new-cover", galleryRes = emptyList())
        assertEquals("content://photos/new-cover", restaurantCoverPhoto(changed)?.uri)
        assertTrue(restaurantPhotos(changed).isEmpty())
        assertEquals(1, restaurantCoverGallery(changed).size)
    }

    @Test fun coverGalleryKeepsExistingCaptionAndExactSeedPhotoTitles() {
        val changed = restaurant.copy(coverUri = "content://photos/visit", photoUris = listOf("content://photos/visit"),
            photoCaptions = listOf("Order this again."),
            galleryRes = listOf(R.drawable.two_brothers_chicken, R.drawable.two_brothers_falafel, R.drawable.two_brothers_baklava))
        assertEquals(listOf("Chicken shawarma plate", "Falafel plate", "Baklava", ""),
            restaurantPhotos(changed).map { it.title })
        val hero = restaurantCoverGallery(changed)
        assertEquals(4, hero.size)
        assertEquals("Order this again.", hero.first().caption)
    }

    @Test fun coverGalleryDoesNotDropSeparateCaptionsOnRepeatedSelections() {
        val changed = restaurant.copy(coverUri = "content://photos/visit", galleryRes = emptyList(),
            photoUris = listOf("content://photos/visit", "content://photos/visit"),
            photoCaptions = listOf("First visit.", "Second visit."))
        assertEquals(listOf("First visit.", "Second visit."), restaurantCoverGallery(changed).map { it.caption })
    }
}
