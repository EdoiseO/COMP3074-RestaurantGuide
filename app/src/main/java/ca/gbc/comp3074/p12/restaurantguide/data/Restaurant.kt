package ca.gbc.comp3074.p12.restaurantguide.data

data class Restaurant(
    val id: Int,
    val name: String,
    val description: String,
    val address: String,
    val phone: String,
    val tags: List<String>,
    val rating: Int,
    val photoRes: Int? = null,
    val listSummary: String = description,
)

fun filterRestaurants(
    restaurants: List<Restaurant>,
    query: String,
    selectedTags: Set<String>,
): List<Restaurant> {
    val nameQuery = query.trim()
    return restaurants.filter { restaurant ->
        val matchesName = restaurant.name.contains(nameQuery, ignoreCase = true)
        val matchesTags = selectedTags.isEmpty() || restaurant.tags.any { restaurantTag ->
            selectedTags.any { selectedTag ->
                restaurantTag.equals(selectedTag, ignoreCase = true)
            }
        }
        matchesName && matchesTags
    }
}
