package ca.gbc.comp3074.p12.restaurantguide.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Simple Room Entity representing a restaurant row.
 */
@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val address: String,
    val phone: String,
    val tagsString: String,      // Simple comma-separated string for tags
    val rating: Float,           // 0.5 step rating
    val listSummary: String,
    val notes: String = "",
    val coverUri: String = "",
    val photoRes: Int? = null,   // Bundled demo drawable cover
    val galleryResString: String = "", // Bundled demo gallery resources
    val photoUrisString: String = "",
    val photoCaptionsString: String = ""
)

/**
 * Simple mapper functions to convert between Room database Entity and UI Model (Restaurant)
 */
fun RestaurantEntity.toDomain(): Restaurant = Restaurant(
    id = id,
    name = name,
    description = description,
    address = address,
    phone = phone,
    tags = if (tagsString.isBlank()) emptyList() else tagsString.split(",").map { it.trim() },
    rating = rating,
    listSummary = listSummary,
    notes = notes,
    coverUri = coverUri,
    photoRes = photoRes, // Properly mapped back to Domain model
    galleryRes = if (galleryResString.isBlank()) emptyList() else galleryResString.split(",").mapNotNull { it.toIntOrNull() }, // Properly mapped back to Domain model
    photoUris = if (photoUrisString.isBlank()) emptyList() else photoUrisString.split("|"),
    photoCaptions = if (photoCaptionsString.isBlank()) emptyList() else photoCaptionsString.split("|")
)

fun Restaurant.toEntity(): RestaurantEntity = RestaurantEntity(
    id = id,
    name = name,
    description = description,
    address = address,
    phone = phone,
    tagsString = tags.joinToString(","),
    rating = rating,
    listSummary = listSummary,
    notes = notes,
    coverUri = coverUri,
    photoRes = photoRes, // saved into Entity
    galleryResString = galleryRes.joinToString(","), // saved into Entity
    photoUrisString = photoUris.joinToString("|"),
    photoCaptionsString = photoCaptions.joinToString("|")
)