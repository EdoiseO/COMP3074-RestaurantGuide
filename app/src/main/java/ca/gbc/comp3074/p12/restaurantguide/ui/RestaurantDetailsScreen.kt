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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTokens
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
    onShare: () -> Unit,
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

    Scaffold(
        containerColor = Color.White,
        bottomBar = { DetailsActionDock(onShare = onShare, onMap = onMap, onDelete = { deleteOpen = true }) },
    ) { safePadding ->
        Box(Modifier.fillMaxSize().padding(safePadding).consumeWindowInsets(safePadding).testTag("restaurant-details")) {
            Column(
                Modifier.fillMaxSize().verticalScroll(scrollState).testTag("details-scroll"),
                verticalArrangement = Arrangement.spacedBy((-28).dp),
            ) {
                Box(
                    Modifier.fillMaxWidth().height(300.dp)
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
                    RatingBadge(restaurant.rating, Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 44.dp))
                }
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(Color.White).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
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
                        ContactColumn(stringResource(R.string.destination_address), restaurant.address, R.drawable.ic_contact_pin, Modifier.weight(1f))
                        ContactColumn(stringResource(R.string.destination_phone), restaurant.phone, R.drawable.ic_phone, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(0.dp))
                    RestaurantMapPreview(restaurant, Modifier.fillMaxWidth().height(160.dp)
                        .clip(RoundedCornerShape(24.dp)).testTag("details-map-preview")
                        .clickable(onClickLabel = stringResource(R.string.details_full_map), onClick = onMap))
                    Box(Modifier.fillMaxWidth().heightIn(min = 112.dp).clip(RoundedCornerShape(24.dp))
                        .background(RestaurantGuideTokens.notesBackground).padding(16.dp)) {
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
                                        Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(shape = RoundedCornerShape(14.dp), color = Color.White) {
                    TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp).testTag("details-back")) {
                        Text(stringResource(R.string.destination_back), fontSize = 16.sp, fontWeight = FontWeight.Normal, color = DetailsHeading)
                    }
                }
                Surface(shape = RoundedCornerShape(14.dp), color = Color.White) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(48.dp).testTag("details-edit")) {
                        Icon(painterResource(R.drawable.ic_edit), stringResource(R.string.details_edit),
                            Modifier.size(24.dp), tint = RestaurantGuideTokens.ink)
                    }
                }
            }
        }
    }

    if (deleteOpen) {
        AppDialog(
            title = stringResource(R.string.details_delete_title),
            body = stringResource(R.string.details_delete_body),
            onDismissRequest = { deleteOpen = false },
            actions = {
                AppDialogButton(stringResource(R.string.details_cancel), { deleteOpen = false }, Modifier.testTag("delete-cancel"))
                AppDialogButton(stringResource(R.string.details_delete), onDelete, Modifier.testTag("confirm-delete"), destructive = true)
            },
        )
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
private fun ContactColumn(label: String, value: String, icon: Int, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = DetailsHeading, fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(painterResource(icon), null, Modifier.size(20.dp), tint = DetailsHeading)
            Text(value.ifBlank { stringResource(R.string.details_not_added) }, Modifier.weight(1f),
                color = DetailsText, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun NotesEditor(notes: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var draft by rememberSaveable { mutableStateOf(notes) }
    AppDialog(
        title = stringResource(R.string.details_edit_notes),
        body = stringResource(R.string.details_notes_body),
        onDismissRequest = onDismiss,
        content = {
            DialogTextField(draft, { draft = it }, stringResource(R.string.details_visit_notes),
                stringResource(R.string.details_notes_hint), Modifier.testTag("visit-notes-input"),
                singleLine = false, minHeight = 76.dp)
        },
        actions = {
            AppDialogButton(stringResource(R.string.details_cancel), onDismiss, Modifier.testTag("notes-cancel"))
            AppDialogButton(stringResource(R.string.destination_save), { onSave(draft.trim()) }, Modifier.testTag("notes-save"))
        },
    )
}
