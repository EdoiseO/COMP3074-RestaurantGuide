package ca.gbc.comp3074.p12.restaurantguide.data

/** Saveable primitive fields only; this snapshot is UI state, not durable storage. */
fun saveRestaurantState(records: List<Restaurant>): List<Any> =
    listOf("restaurants-v3") + records.flatMap { restaurant ->
        listOf(
            restaurant.id, restaurant.name, restaurant.description, restaurant.address,
            restaurant.phone, ArrayList(restaurant.tags), restaurant.rating,
            restaurant.photoRes ?: 0, restaurant.listSummary, restaurant.notes,
            ArrayList(restaurant.galleryRes), restaurant.mapRes ?: 0,
            ArrayList(restaurant.photoUris), ArrayList(restaurant.photoCaptions),
            restaurant.fullMapRes ?: 0, restaurant.coverUri,
        )
    }

/** Reads current fractional records and both previously installed integer-rating snapshots. */
fun restoreRestaurantState(values: List<Any>): List<Restaurant> {
    val version = when (values.firstOrNull()) {
        "restaurants-v3" -> 3
        "restaurants-v2" -> 2
        else -> 1
    }
    val fields = if (version > 1) values.drop(1) else values
    val fieldCount = when (version) {
        3 -> 16
        2 -> 15
        else -> 9
    }
    require(fields.size % fieldCount == 0) { "Incomplete restaurant state" }
    return fields.chunked(fieldCount).map { record ->
        Restaurant(
            id = record[0] as Int,
            name = record[1] as String,
            description = record[2] as String,
            address = record[3] as String,
            phone = record[4] as String,
            tags = (record[5] as List<*>).map { it as String },
            rating = (record[6] as Number).toFloat(),
            photoRes = (record[7] as Int).takeIf { it != 0 },
            listSummary = record[8] as String,
            notes = if (version > 1) record[9] as String else "",
            galleryRes = if (version > 1) (record[10] as List<*>).map { it as Int } else emptyList(),
            mapRes = if (version > 1) (record[11] as Int).takeIf { it != 0 } else null,
            photoUris = if (version > 1) (record[12] as List<*>).map { it as String } else emptyList(),
            photoCaptions = if (version > 1) (record[13] as List<*>).map { it as String } else emptyList(),
            fullMapRes = if (version > 1) (record[14] as Int).takeIf { it != 0 } else null,
            coverUri = if (version == 3) record[15] as String else "",
        )
    }
}
