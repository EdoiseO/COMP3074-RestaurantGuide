package ca.gbc.comp3074.p12.restaurantguide.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RestaurantStateTest {
    @Test
    fun currentSnapshotRestoresFractionalRatingsAndEveryPhotoField() {
        val selected = Restaurant(
            id = 7, name = "Café & Tacos", description = "Description",
            address = "42 Main St", phone = "416-555-0100 / 416-555-0101",
            tags = listOf("Cafe", "Tacos"), rating = 4.5f,
            photoRes = 101, listSummary = "Cafe • Tacos", notes = "Order again",
            galleryRes = listOf(102, 103), mapRes = 104, fullMapRes = 105,
            photoUris = listOf("content://photos/visit-1", "content://photos/visit-2"),
            photoCaptions = listOf("Meal", "Dessert"),
            coverUri = "content://photos/cover",
        )
        val unrated = Restaurant(
            id = 8, name = "New Restaurant", description = "", address = "", phone = "",
            tags = emptyList(), rating = 0f,
        )

        val snapshot = saveRestaurantState(listOf(selected, unrated))

        assertEquals("restaurants-v3", snapshot.first())
        assertEquals(listOf(selected, unrated), restoreRestaurantState(snapshot))
    }

    @Test
    fun versionTwoIntegerRatingMigratesWithoutLosingNotesPhotosOrMaps() {
        val snapshot: List<Any> = listOf(
            "restaurants-v2", 2, "Previous Restaurant", "Description", "954 Wilson Ave",
            "416-555-0100", arrayListOf("Turkish"), 4, 101, "Turkish",
            "Old note", arrayListOf(102), 103,
            arrayListOf("content://photos/old"), arrayListOf("Old caption"), 104,
        )

        val migrated = restoreRestaurantState(snapshot).single()

        assertEquals(4f, migrated.rating, 0f)
        assertEquals("", migrated.coverUri)
        assertEquals("Old note", migrated.notes)
        assertEquals(listOf("content://photos/old"), migrated.photoUris)
        assertEquals(listOf("Old caption"), migrated.photoCaptions)
        assertEquals(listOf(102), migrated.galleryRes)
        assertEquals(103, migrated.mapRes)
        assertEquals(104, migrated.fullMapRes)
    }

    @Test
    fun legacyNineFieldIntegerRatingMigratesWithOptionalDefaults() {
        val snapshot: List<Any> = listOf(
            1, "Legacy Restaurant", "Description", "123 Main St", "416-555-0100",
            arrayListOf("Cafe"), 3, 0, "Cafe",
        )

        val migrated = restoreRestaurantState(snapshot).single()

        assertEquals(
            Restaurant(
                id = 1, name = "Legacy Restaurant", description = "Description",
                address = "123 Main St", phone = "416-555-0100", tags = listOf("Cafe"),
                rating = 3f, listSummary = "Cafe",
            ),
            migrated,
        )
    }

    @Test
    fun emptyListsRemainEmptyInEverySupportedSnapshotVersion() {
        assertEquals(emptyList<Restaurant>(), restoreRestaurantState(saveRestaurantState(emptyList())))
        assertEquals(emptyList<Restaurant>(), restoreRestaurantState(listOf("restaurants-v2")))
        assertEquals(emptyList<Restaurant>(), restoreRestaurantState(emptyList()))
    }
}
