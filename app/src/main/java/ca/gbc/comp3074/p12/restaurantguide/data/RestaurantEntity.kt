package ca.gbc.comp3074.p12.restaurantguide.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val address: String,
    val phone: String,
    val tagsJson: String,
    val rating: Float,
    val listSummary: String,
    val notes: String = "",
    val coverUri: String = "",
    val photoKey: String? = null,
    val galleryKeysJson: String = "[]",
    val mapKey: String? = null,
    val fullMapKey: String? = null,
    val photoUrisJson: String = "[]",
    val photoCaptionsJson: String = "[]",
)

internal fun List<String>.toJson(): String = JSONArray(this).toString()
internal fun String.fromJson(): List<String> = JSONArray(this).let { array ->
    List(array.length()) { array.getString(it) }
}

fun RestaurantEntity.toDomain(): Restaurant = Restaurant(
    id = id, name = name, description = description, address = address, phone = phone,
    tags = tagsJson.fromJson(), rating = rating, listSummary = listSummary, notes = notes,
    coverUri = coverUri, photoRes = BundledRestaurantImages.resource(photoKey),
    galleryRes = galleryKeysJson.fromJson().mapNotNull(BundledRestaurantImages::resource),
    mapRes = BundledRestaurantImages.resource(mapKey),
    fullMapRes = BundledRestaurantImages.resource(fullMapKey),
    photoUris = photoUrisJson.fromJson(), photoCaptions = photoCaptionsJson.fromJson(),
)

fun Restaurant.toEntity(): RestaurantEntity = RestaurantEntity(
    id = id, name = name, description = description, address = address, phone = phone,
    tagsJson = tags.toJson(), rating = rating, listSummary = listSummary, notes = notes,
    coverUri = coverUri, photoKey = BundledRestaurantImages.key(photoRes),
    galleryKeysJson = galleryRes.mapNotNull(BundledRestaurantImages::key).toJson(),
    mapKey = BundledRestaurantImages.key(mapRes), fullMapKey = BundledRestaurantImages.key(fullMapRes),
    photoUrisJson = photoUris.toJson(), photoCaptionsJson = photoCaptions.toJson(),
)
