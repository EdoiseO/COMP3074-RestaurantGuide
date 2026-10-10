package ca.gbc.comp3074.p12.restaurantguide.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestaurantSharingTest {
    @Test
    fun draftIncludesAllSixRequestedFieldsAndExcludesVisitNotes() {
        val selected = Restaurant(
            id = 7,
            name = "Selected Restaurant",
            address = "123 Sample St, Toronto, ON A1A 1A1",
            phone = "416-555-0100 / 416-555-0101",
            rating = 4,
            tags = listOf("Bakery", "Tacos"),
            description = "Fresh bread and tacos.",
            notes = "Private visit note",
            photoCaptions = listOf("Private photo caption"),
        )

        val draft = restaurantShareBody(selected)

        assertEquals(
            "Selected Restaurant\n\n" +
                "Address: 123 Sample St, Toronto, ON A1A 1A1\n" +
                "Phone: 416-555-0100 / 416-555-0101\n" +
                "Rating: 4.0 / 5\n" +
                "Tags: Bakery, Tacos\n\n" +
                "Description:\nFresh bread and tacos.",
            draft,
        )
        assertFalse(draft.contains(selected.notes))
        assertFalse(draft.contains(selected.photoCaptions.single()))
    }

    @Test
    fun draftUsesTheSelectedRecordInsteadOfAnotherRestaurant() {
        val first = Restaurant(
            id = 1, name = "First Restaurant", description = "First description",
            address = "111 First Avenue", phone = "416-555-0100",
            tags = listOf("First tag"), rating = 1,
        )
        val second = Restaurant(
            id = 2, name = "Second Restaurant", description = "Second description",
            address = "222 Second Avenue", phone = "437-555-0200",
            tags = listOf("Second tag"), rating = 5,
        )

        val draft = restaurantShareBody(second)

        listOf(second.name, second.description, second.address, second.phone, second.tags.single())
            .forEach { assertTrue("Missing selected value: $it", draft.contains(it)) }
        listOf(first.name, first.description, first.address, first.phone, first.tags.single())
            .forEach { assertFalse("Another record leaked into the draft: $it", draft.contains(it)) }
        assertTrue(draft.contains("Rating: 5.0 / 5"))
    }

    @Test
    fun draftPreservesUnicodeAmpersandsAndMultilineDescription() {
        val selected = Restaurant(
            id = 3,
            name = "Café & Taquería",
            description = "Crème brûlée & tacos.\nWould order again: árbol salsa.",
            address = "42 Rue de l’Église & Main St",
            phone = "+1 437 555 0123",
            tags = listOf("Pâtisserie", "Bread & pastries"),
            rating = 3,
        )

        val draft = restaurantShareBody(selected)

        assertTrue(draft.startsWith(selected.name + "\n"))
        assertTrue(draft.contains("Address: ${selected.address}\n"))
        assertTrue(draft.contains("Tags: Pâtisserie, Bread & pastries\n"))
        assertTrue(draft.endsWith("Description:\n${selected.description}"))
        assertFalse(draft.contains("&amp;"))
        assertFalse(draft.contains("%26"))
    }

    @Test
    fun missingOptionalValuesHaveReadableDraftFallbacks() {
        val selected = Restaurant(
            id = 4, name = "New Restaurant", description = "", address = "",
            phone = "", tags = emptyList(), rating = 0,
        )

        val draft = restaurantShareBody(selected)

        assertTrue(draft.contains("Address: Not added\n"))
        assertTrue(draft.contains("Phone: Not added\n"))
        assertTrue(draft.contains("Rating: Not rated\n"))
        assertTrue(draft.contains("Tags: None\n"))
        assertTrue(draft.endsWith("Description:\nNot added"))
    }
}
