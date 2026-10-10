package ca.gbc.comp3074.p12.restaurantguide.data

/** Aligns older optional captions before adding a new visit photo and its own caption. */
fun Restaurant.withVisitPhoto(uri: String, caption: String): Restaurant = copy(
    photoUris = photoUris + uri,
    photoCaptions = photoUris.indices.map { photoCaptions.getOrElse(it) { "" } } + caption,
)
