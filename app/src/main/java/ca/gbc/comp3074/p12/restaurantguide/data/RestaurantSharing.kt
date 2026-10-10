package ca.gbc.comp3074.p12.restaurantguide.data

/** The complete selected record, ready for an email draft; opening it never sends mail. */
fun restaurantShareBody(restaurant: Restaurant): String = buildString {
    appendLine(restaurant.name)
    appendLine()
    appendLine("Address: ${restaurant.address.ifBlank { "Not added" }}")
    appendLine("Phone: ${restaurant.phone.ifBlank { "Not added" }}")
    appendLine("Rating: ${if (restaurant.rating > 0) "${restaurant.rating}.0 / 5" else "Not rated"}")
    appendLine("Tags: ${restaurant.tags.joinToString(", ").ifBlank { "None" }}")
    appendLine()
    appendLine("Description:")
    append(restaurant.description.ifBlank { "Not added" })
}
