package ca.gbc.comp3074.p12.restaurantguide.data

import org.junit.Assert.assertEquals
import org.junit.Test

class RestaurantPhotosTest {
    private val record = Restaurant(
        id = 7, name = "Cafe", description = "Description", address = "123 Main St",
        phone = "416-555-0100", tags = listOf("Cafe"), rating = 4.5f,
        notes = "Order again", coverUri = "content://photos/cover",
    )

    @Test
    fun missingLegacyCaptionDoesNotStealTheNewPhotosCaption() {
        val previous = record.copy(photoUris = listOf("content://photos/old"))

        val changed = previous.withVisitPhoto("content://photos/new", "New meal")

        assertEquals(listOf("content://photos/old", "content://photos/new"), changed.photoUris)
        assertEquals(listOf("", "New meal"), changed.photoCaptions)
        assertEquals(record.id, changed.id)
        assertEquals(record.coverUri, changed.coverUri)
        assertEquals(record.notes, changed.notes)
    }

    @Test
    fun partiallyAlignedLegacyCaptionsPreserveTheirOriginalPhotoPositions() {
        val previous = record.copy(
            photoUris = listOf("content://photos/first", "content://photos/second"),
            photoCaptions = listOf("First meal"),
        )

        val changed = previous.withVisitPhoto("content://photos/new", "New meal")

        assertEquals(listOf("First meal", "", "New meal"), changed.photoCaptions)
    }

    @Test
    fun surplusLegacyCaptionsCannotBecomeTheNewPhotosCaption() {
        val previous = record.copy(
            photoUris = listOf("content://photos/old"),
            photoCaptions = listOf("Old meal", "Orphaned caption"),
        )

        val changed = previous.withVisitPhoto("content://photos/new", "New meal")

        assertEquals(listOf("Old meal", "New meal"), changed.photoCaptions)
    }

    @Test
    fun alignedCaptionsAndAllOtherRecordFieldsRemainUnchanged() {
        val previous = record.copy(
            photoUris = listOf("content://photos/first", "content://photos/second"),
            photoCaptions = listOf("First meal", "Second meal"),
        )

        val changed = previous.withVisitPhoto("content://photos/new", "New meal")

        assertEquals(
            previous.copy(
                photoUris = previous.photoUris + "content://photos/new",
                photoCaptions = previous.photoCaptions + "New meal",
            ),
            changed,
        )
    }
}
