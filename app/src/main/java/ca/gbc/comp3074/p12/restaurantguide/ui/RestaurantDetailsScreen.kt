package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ca.gbc.comp3074.p12.restaurantguide.R
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import ca.gbc.comp3074.p12.restaurantguide.data.withVisitPhoto

private val DetailsHeading = Color(0xFF4F378A)
private val DetailsText = Color(0xFF49454F)
private val DetailsBorder = Color(0xFFCAC4D0)

fun restaurantPhotos(restaurant: Restaurant): List<RestaurantPhoto> {
    // The album and cover are independent: removing an album photo must not
    // reinsert it merely because that image is still the restaurant's cover.
    return restaurant.galleryRes.distinct().map { restaurantResourcePhoto(it, restaurant.name) } +
        restaurant.photoUris.mapIndexed { index, uri ->
            RestaurantPhoto(uri = uri, caption = restaurant.photoCaptions.getOrElse(index) { "" })
        }
}

fun restaurantCoverPhoto(restaurant: Restaurant): RestaurantPhoto? =
    restaurant.coverUri.takeIf(String::isNotBlank)?.let { RestaurantPhoto(uri = it, title = restaurant.name) }
        ?: restaurant.photoRes?.let { restaurantResourcePhoto(it, restaurant.name) }

fun restaurantCoverGallery(restaurant: Restaurant): List<RestaurantPhoto> {
    val album = restaurantPhotos(restaurant)
    val cover = restaurantCoverPhoto(restaurant) ?: return album
    val coverIndex = album.indexOfFirst { it.resourceId == cover.resourceId && it.uri == cover.uri }
    return if (coverIndex < 0) listOf(cover) + album
        else listOf(album[coverIndex]) + album.filterIndexed { index, _ -> index != coverIndex }
}

private fun restaurantResourcePhoto(resourceId: Int, restaurantName: String): RestaurantPhoto =
    RestaurantPhoto(resourceId = resourceId, title = when (resourceId) {
        R.drawable.two_brothers_chicken -> "Chicken shawarma plate"
        R.drawable.two_brothers_falafel -> "Falafel plate"
        R.drawable.two_brothers_baklava -> "Baklava"
        else -> restaurantName
    })

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestaurantDetailsScreen(
    restaurant: Restaurant,
    scrollState: ScrollState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (Restaurant) -> Unit,
    onMap: () -> Unit,
    onEmail: () -> Unit,
) {
    var deleteOpen by rememberSaveable(restaurant.id) { mutableStateOf(false) }
    var notesOpen by rememberSaveable(restaurant.id) { mutableStateOf(false) }
    var photoOpen by rememberSaveable(restaurant.id) { mutableStateOf(false) }
    var photoIndex by rememberSaveable(restaurant.id) { mutableStateOf(0) }
    var viewingCover by rememberSaveable(restaurant.id) { mutableStateOf(false) }
    var addPhotoOpen by rememberSaveable(restaurant.id) { mutableStateOf(false) }
    val photos = restaurantPhotos(restaurant)
    val cover = restaurantCoverPhoto(restaurant)
    val openPhoto: (Int) -> Unit = { index -> viewingCover = false; photoIndex = index; photoOpen = true }

    Scaffold { safePadding ->
        Column(Modifier.fillMaxSize().padding(safePadding).testTag("restaurant-details")) {
            Row(
                Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text(stringResource(R.string.destination_back), fontSize = 16.sp) }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { deleteOpen = true }) { Text(stringResource(R.string.details_delete), fontSize = 16.sp) }
                TextButton(onClick = onEdit) { Text(stringResource(R.string.details_edit), fontSize = 16.sp) }
                Spacer(Modifier.width(8.dp))
            }
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState)
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier.fillMaxWidth().height(180.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    if (cover != null) {
                        RestaurantPhotoImage(
                            cover,
                            stringResource(R.string.destination_restaurant_photo, restaurant.name),
                            Modifier.fillMaxSize().clickable(onClickLabel = stringResource(R.string.details_view_photo)) {
                                viewingCover = true; photoIndex = 0; photoOpen = true
                            },
                            ContentScale.Crop,
                            alignment = when (cover.resourceId) {
                                R.drawable.midyeci_riza -> BiasAlignment(0f, -0.5f)
                                R.drawable.la_unica -> BiasAlignment(0f, -0.35f)
                                else -> Alignment.Center
                            },
                        )
                    } else {
                        Icon(painterResource(R.drawable.ic_photo), stringResource(R.string.list_photo_placeholder),
                            Modifier.align(Alignment.Center).size(36.dp), tint = DetailsText)
                    }
                    RatingBadge(restaurant.rating, Modifier.align(Alignment.TopEnd).padding(16.dp))
                }
                Spacer(Modifier.height(0.dp)) // Together with the two section gaps, gives the cover 16dp of space.
                Text(
                    restaurant.name, Modifier.semantics { heading() },
                    color = DetailsHeading, fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold,
                )
                if (restaurant.description.isNotBlank()) {
                    DetailsBody(restaurant.description)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    restaurant.tags.forEach { tag ->
                        Surface(
                            color = Color.White, shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, DetailsBorder),
                            modifier = Modifier.testTag("details-tag-" + tag),
                        ) {
                            Text(tag, Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = DetailsText, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Text(stringResource(R.string.details_my_photos), color = DetailsHeading,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (photos.isNotEmpty()) LazyRow(
                        Modifier.weight(1f, fill = false)
                            .widthIn(max = 84.dp * photos.size + 8.dp * (photos.size - 1))
                            .height(84.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(photos) { index, photo ->
                            RestaurantPhotoImage(
                                photo, stringResource(R.string.details_photo_number, index + 1, restaurant.name),
                                Modifier.size(84.dp).clip(RoundedCornerShape(8.dp))
                                    .testTag("details-photo-" + index)
                                    .clickable(onClickLabel = stringResource(R.string.details_view_photo)) { openPhoto(index) },
                                ContentScale.Crop,
                            )
                        }
                    }
                    Surface(
                        onClick = { addPhotoOpen = true }, modifier = Modifier.size(84.dp).testTag("details-add-photo"),
                        shape = RoundedCornerShape(8.dp), color = Color.White, border = BorderStroke(1.dp, DetailsBorder),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(stringResource(R.string.details_add_photo), color = DetailsText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                Spacer(Modifier.height(0.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ContactColumn(stringResource(R.string.destination_address), restaurant.address, Modifier.weight(1f))
                    ContactColumn(stringResource(R.string.destination_phone), restaurant.phone, Modifier.weight(1f))
                }
                Spacer(Modifier.height(0.dp))
                RestaurantMapPreview(restaurant, Modifier.fillMaxWidth().height(160.dp)
                    .clip(RoundedCornerShape(24.dp)).testTag("details-map-preview")
                    .clickable(onClickLabel = stringResource(R.string.details_full_map), onClick = onMap))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onMap, modifier = Modifier.weight(1f).height(44.dp).testTag("details-full-map"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)) {
                        Icon(painterResource(R.drawable.ic_map_pin), null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.details_full_map), fontSize = 14.sp, maxLines = 1)
                    }
                    Button(onClick = onEmail, modifier = Modifier.weight(1f).height(44.dp).testTag("details-email"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)) {
                        Icon(painterResource(R.drawable.ic_email), null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.details_share_email), fontSize = 14.sp, maxLines = 1)
                    }
                }
                Box(Modifier.fillMaxWidth().heightIn(min = 112.dp).clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(end = 40.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.details_notes), color = Color.Black, fontWeight = FontWeight.Bold,
                            fontSize = 16.sp, lineHeight = 24.sp)
                        Text(restaurant.notes.ifBlank { stringResource(R.string.details_notes_empty) },
                            color = Color.Black, fontSize = 16.sp, lineHeight = 24.sp,
                            fontWeight = if (restaurant.notes.isBlank()) FontWeight.Bold else FontWeight.Normal)
                    }
                    IconButton(onClick = { notesOpen = true }, modifier = Modifier.align(Alignment.BottomEnd).size(48.dp).testTag("details-edit-notes")) {
                        Surface(shape = CircleShape, color = Color.Transparent,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), modifier = Modifier.size(40.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(painterResource(R.drawable.ic_edit), stringResource(R.string.details_edit_notes),
                                    Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
    if (deleteOpen) {
        Dialog(onDismissRequest = { deleteOpen = false }) {
            Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = Color.White) {
                Column(
                    Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(stringResource(R.string.details_delete_title), Modifier.fillMaxWidth().semantics { heading() },
                        textAlign = TextAlign.Center, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.details_delete_body), Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center, fontSize = 14.sp, lineHeight = 20.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { deleteOpen = false }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                            Text(stringResource(R.string.details_cancel))
                        }
                        TextButton(onClick = onDelete, modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("confirm-delete")) {
                            Text(stringResource(R.string.details_delete), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
    if (notesOpen) {
        NotesEditor(restaurant.notes, onDismiss = { notesOpen = false }, onSave = {
            onUpdate(restaurant.copy(notes = it)); notesOpen = false
        })
    }
    val viewerPhotos = if (viewingCover) restaurantCoverGallery(restaurant) else photos
    if (photoOpen && viewerPhotos.isNotEmpty()) {
        RestaurantPhotoViewer(restaurant.name, viewerPhotos, photoIndex, onClose = { photoOpen = false })
    }
    if (addPhotoOpen) {
        VisitPhotoPicker(onDismiss = { addPhotoOpen = false }, onSave = { uri, caption ->
            onUpdate(restaurant.withVisitPhoto(uri, caption))
            addPhotoOpen = false
        })
    }
}

@Composable
private fun RatingBadge(rating: Float, modifier: Modifier) {
    val description = stringResource(R.string.details_rating_accessibility, ca.gbc.comp3074.p12.restaurantguide.data.formatRating(rating))
    Surface(modifier, shape = RoundedCornerShape(8.dp), color = Color.White) {
        Box(Modifier.size(width = 126.dp, height = 36.dp).clearAndSetSemantics { contentDescription = description },
            contentAlignment = Alignment.Center) {
            StarRating(rating, {}, interactive = false, starSize = 24.dp)
        }
    }
}

@Composable
private fun DetailsBody(text: String) {
    Text(text, color = DetailsText, fontSize = 14.sp, lineHeight = 20.sp)
}

@Composable
private fun ContactColumn(label: String, value: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = DetailsHeading, fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
        DetailsBody(value.ifBlank { stringResource(R.string.details_not_added) })
    }
}

@Composable
private fun NotesEditor(notes: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var draft by rememberSaveable { mutableStateOf(notes) }
    AlertDialog(
        onDismissRequest = onDismiss, shape = RoundedCornerShape(24.dp), containerColor = Color.White,
        title = { Text(stringResource(R.string.details_visit_notes)) },
        text = {
            OutlinedTextField(draft, { draft = it }, Modifier.fillMaxWidth().testTag("visit-notes-input"),
                label = { Text(stringResource(R.string.details_visit_notes)) },
                placeholder = { Text(stringResource(R.string.details_notes_hint)) },
                shape = RoundedCornerShape(12.dp), minLines = 4, maxLines = 8)
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.details_cancel)) } },
        confirmButton = { TextButton(onClick = { onSave(draft.trim()) }) { Text(stringResource(R.string.destination_save)) } },
    )
}
