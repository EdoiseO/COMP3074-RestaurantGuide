package ca.gbc.comp3074.p12.restaurantguide.data

import ca.gbc.comp3074.p12.restaurantguide.R

/** Persist names, never build-specific Android resource integers. Unknown keys stay unavailable. */
internal object BundledRestaurantImages {
    private val resources = mapOf(
        "la_unica" to R.drawable.la_unica,
        "la_unica_full_map" to R.drawable.la_unica_full_map,
        "la_unica_map" to R.drawable.la_unica_map,
        "midyeci_riza" to R.drawable.midyeci_riza,
        "midyeci_riza_full_map" to R.drawable.midyeci_riza_full_map,
        "midyeci_riza_map" to R.drawable.midyeci_riza_map,
        "the_fry_storefront" to R.drawable.the_fry_storefront,
        "the_fry_chicken_spread" to R.drawable.the_fry_chicken_spread,
        "the_fry_chicken_plates" to R.drawable.the_fry_chicken_plates,
        "two_brothers_baklava" to R.drawable.two_brothers_baklava,
        "two_brothers_chicken" to R.drawable.two_brothers_chicken,
        "two_brothers_falafel" to R.drawable.two_brothers_falafel,
        "two_brothers_full_map" to R.drawable.two_brothers_full_map,
        "two_brothers_map" to R.drawable.two_brothers_map,
        "two_brothers_meal" to R.drawable.two_brothers_meal,
    )
    fun key(id: Int?): String? = resources.entries.firstOrNull { it.value == id }?.key
    fun resource(key: String?): Int? = resources[key]
}
