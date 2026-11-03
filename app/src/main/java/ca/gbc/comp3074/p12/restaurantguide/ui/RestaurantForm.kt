package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ca.gbc.comp3074.p12.restaurantguide.R
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import ca.gbc.comp3074.p12.restaurantguide.data.formatRating
import ca.gbc.comp3074.p12.restaurantguide.data.normalizeAddress
import ca.gbc.comp3074.p12.restaurantguide.data.validPhoneNumbers
import java.util.Locale

private val FormStringsSaver = listSaver<List<String>, String>(save = { it }, restore = { it })
private val FormResourcesSaver = listSaver<List<Int>, Int>(save = { it }, restore = { it })
private val FormPurple = Color(0xFF4F378A)
private val FormBorder = Color(0xFFCAC4D0)
private val FormMuted = Color(0xFF49454F)
private val FormInk = Color(0xFF1C1A21)
private val FormTitle = Color(0xFF25212B)
private val CoverFill = Color(0xFFF2EDF7)
private val TagFill = Color(0xFFE8DEF8)
private val TagText = Color(0xFF4A4459)
// Figma reports mixed 16/8 corners on the cover; the exact corner mapping is an estimate.
private val CoverShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 8.dp, bottomEnd = 8.dp)

/** Add: "New restaurant" title (Figma Bold 22/24). Edit: "← Edit restaurant" title that also goes back. */
@Composable
private fun FormHeader(isEdit: Boolean, onBack: () -> Unit, backTag: String) {
    if (isEdit) {
        Row(Modifier.padding(start = 8.dp, top = 8.dp).heightIn(min = 48.dp).clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onBack).padding(horizontal = 12.dp).testTag(backTag)
            .semantics { heading() },
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("←", color = FormPurple, fontSize = 22.sp, lineHeight = 28.sp)
            Text(stringResource(R.string.details_edit_title), color = FormPurple, fontSize = 22.sp, lineHeight = 28.sp)
        }
    } else {
        Text(stringResource(R.string.form_new_title), Modifier.padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 8.dp)
            .semantics { heading() }, color = FormTitle, fontSize = 22.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
    }
}

/** Always visible, including 0.0 / 5, so an unrated draft still reads as a value. */
@Composable
private fun RatingReadout(rating: Float) {
    Text(
        buildAnnotatedString {
            withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = FormInk)) { append(formatRating(rating)) }
            withStyle(SpanStyle(fontSize = 14.sp, color = FormMuted)) { append(" / 5") }
        },
        Modifier.padding(start = 12.dp),
    )
}

/** Add and Edit share the same draft. Leaving without Save never changes a saved record. */
@Composable
fun RestaurantFormEntry(restaurant: Restaurant? = null, onBack: () -> Unit, onSave: (Restaurant) -> Unit) {
    val draftKey = restaurant?.id ?: -1
    val prefix = if (restaurant == null) "add" else "edit"
    var name by rememberSaveable(draftKey) { mutableStateOf(restaurant?.name.orEmpty()) }
    var description by rememberSaveable(draftKey) { mutableStateOf(restaurant?.description.orEmpty()) }
    var address by rememberSaveable(draftKey) { mutableStateOf(restaurant?.address.orEmpty()) }
    var phone by rememberSaveable(draftKey) { mutableStateOf(restaurant?.phone.orEmpty()) }
    var notes by rememberSaveable(draftKey) { mutableStateOf(restaurant?.notes.orEmpty()) }
    var rating by rememberSaveable(draftKey) { mutableStateOf(restaurant?.rating ?: 0f) }
    var tags by rememberSaveable(draftKey, stateSaver = FormStringsSaver) { mutableStateOf(restaurant?.tags.orEmpty()) }
    var coverResource by rememberSaveable(draftKey) { mutableIntStateOf(restaurant?.photoRes ?: 0) }
    var coverUri by rememberSaveable(draftKey) { mutableStateOf(restaurant?.coverUri.orEmpty()) }
    var resources by rememberSaveable(draftKey, stateSaver = FormResourcesSaver) { mutableStateOf(restaurant?.galleryRes.orEmpty()) }
    var uris by rememberSaveable(draftKey, stateSaver = FormStringsSaver) { mutableStateOf(restaurant?.photoUris.orEmpty()) }
    var captions by rememberSaveable(draftKey, stateSaver = FormStringsSaver) {
        mutableStateOf(restaurant?.photoUris.orEmpty().indices.map { restaurant?.photoCaptions?.getOrNull(it).orEmpty() })
    }
    var nameError by rememberSaveable(draftKey) { mutableStateOf(false) }
    var showNameRequired by rememberSaveable { mutableStateOf(false) }
    var showPhoneError by rememberSaveable { mutableStateOf(false) }
    val phoneFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var phoneError by rememberSaveable(draftKey) { mutableStateOf(false) }
    var showCoverPicker by rememberSaveable { mutableStateOf(false) }
    var showVisitPicker by rememberSaveable { mutableStateOf(false) }
    var editingPhoto by rememberSaveable { mutableIntStateOf(-1) }
    var showTags by rememberSaveable { mutableStateOf(false) }
    var showAddress by rememberSaveable { mutableStateOf(false) }
    var showRating by rememberSaveable { mutableStateOf(false) }
    var ratingDraft by rememberSaveable { mutableStateOf(0f) }
    val scroll = rememberScrollState()
    BackHandler(onBack = onBack)

    val save = {
        nameError = name.isBlank()
        phoneError = !validPhoneNumbers(phone)
        if (nameError) showNameRequired = true
        else if (phoneError) showPhoneError = true
        if (!nameError && !phoneError) {
            val formattedAddress = normalizeAddress(address)
            val original = restaurant ?: Restaurant(0, "", "", "", "", emptyList(), 0f)
            onSave(original.copy(
                name = name.trim(), description = description.trim(), address = formattedAddress,
                phone = phone.trim(), notes = notes.trim(), tags = tags, rating = rating,
                photoRes = coverResource.takeIf { it != 0 }, coverUri = coverUri,
                galleryRes = resources, photoUris = uris, photoCaptions = captions,
                mapRes = original.mapRes.takeIf { formattedAddress == normalizeAddress(original.address) },
                fullMapRes = original.fullMapRes.takeIf { formattedAddress == normalizeAddress(original.address) },
                listSummary = tags.take(3).joinToString(" • ").ifBlank { description.trim() },
            ))
        }
    }

    Scaffold(
        containerColor = Color.White,
        // Figma 52:213 / 50:166: fixed Cancel/Save dock; it owns the keyboard and navigation insets.
        bottomBar = {
            ActionDock(cancel = onBack, save = save,
                cancelModifier = Modifier.testTag("$prefix-cancel"), saveModifier = Modifier.testTag("$prefix-save"))
        },
    ) { insets ->
        Column(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets).testTag("restaurant-form")) {
            FormHeader(isEdit = restaurant != null, onBack = onBack, backTag = "$prefix-back")
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(scroll).padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 24.dp)) {
                Box(Modifier.fillMaxWidth().height(180.dp).clip(CoverShape)
                    .background(CoverFill).clickable { showCoverPicker = true }.testTag("$prefix-cover")) {
                    if (coverUri.isNotBlank() || coverResource != 0) {
                        RestaurantPhotoImage(RestaurantPhoto(resourceId = coverResource.takeIf { it != 0 && coverUri.isBlank() }, uri = coverUri.takeIf(String::isNotBlank)),
                            stringResource(R.string.form_restaurant_photo), Modifier.fillMaxSize(), ContentScale.Crop)
                        Surface(Modifier.align(Alignment.TopEnd).padding(8.dp).clickable { showCoverPicker = true }, shape = RoundedCornerShape(8.dp), color = Color.White) {
                            Text(stringResource(R.string.form_change_photo), color = FormPurple, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 14.sp)
                        }
                        IconButton(onClick = { coverUri = ""; coverResource = 0 }, modifier = Modifier.align(Alignment.TopStart).padding(4.dp)) {
                            Surface(shape = RoundedCornerShape(50), color = Color.White) { Icon(painterResource(R.drawable.ic_close), stringResource(R.string.form_remove_cover), Modifier.size(24.dp), tint = FormPurple) }
                        }
                    } else {
                        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(painterResource(R.drawable.ic_photo), null, Modifier.size(24.dp), tint = FormMuted)
                            Text(stringResource(R.string.form_add_cover), color = FormMuted, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }
                }
                FormField(name, { name = it; if (it.isNotBlank()) nameError = false }, stringResource(R.string.destination_restaurant_name),
                    stringResource(R.string.form_name_hint), "$prefix-name", singleLine = true, error = nameError,
                    errorText = stringResource(R.string.destination_name_required))
                FormHeading(stringResource(R.string.details_my_photos), Modifier.padding(top = 12.dp, bottom = 12.dp))
                Row(Modifier.fillMaxWidth().testTag("$prefix-photos"), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val photoCount = resources.size + uris.size
                    if (photoCount > 0) LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                            .widthIn(max = 84.dp * photoCount + 8.dp * (photoCount - 1)),
                    ) {
                        itemsIndexed(resources, key = { index, res -> "resource-$index-$res" }) { index, res ->
                            FormPhoto(RestaurantPhoto(resourceId = res), "$prefix-resource-photo-$index", onEdit = null,
                                onRemove = { resources = resources.filterIndexed { i, _ -> i != index } })
                        }
                        itemsIndexed(uris, key = { index, _ -> "uri-$index" }) { index, uri ->
                            FormPhoto(RestaurantPhoto(uri = uri, caption = captions.getOrNull(index).orEmpty()), "$prefix-uri-photo-$index",
                                onEdit = { editingPhoto = index; showVisitPicker = true }, onRemove = {
                                    uris = uris.filterIndexed { i, _ -> i != index }; captions = captions.filterIndexed { i, _ -> i != index }
                                })
                        }
                    }
                    Surface(Modifier.size(84.dp).clickable { editingPhoto = -1; showVisitPicker = true }.testTag("$prefix-add-photo"),
                        shape = RoundedCornerShape(8.dp), color = Color.White, border = androidx.compose.foundation.BorderStroke(1.dp, FormBorder)) {
                        Box(contentAlignment = Alignment.Center) { Text(stringResource(R.string.details_add_photo), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF49454F)) }
                    }
                }
                FormField(description, { description = it }, stringResource(R.string.destination_description), stringResource(R.string.form_description_hint),
                    "$prefix-description", Modifier.padding(top = 16.dp), minLines = 4, compact = restaurant != null)
                FormHeading(stringResource(R.string.destination_your_rating), Modifier.padding(top = 12.dp, bottom = 8.dp))
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    // Figma 52:213 / 50:166: five 48px star glyphs across a 249×56 row. Narrow screens
                    // shrink the stars so the readout always keeps its room on the right.
                    val starSize = minOf(50.dp, (maxWidth - 96.dp) / 5)
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(role = Role.Button) { ratingDraft = rating; showRating = true }
                        .testTag("$prefix-rating"), verticalAlignment = Alignment.CenterVertically) {
                        StarRating(rating, {}, interactive = false, starSize = starSize)
                        Spacer(Modifier.weight(1f))
                        RatingReadout(rating)
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) {
                    tags.forEach { tag ->
                        // Figma 50:166: lavender M3 filter chip, 32 high, 16/6 padding. The ✕ keeps removal
                        // visible (Figma omits it); the touch area is expanded to 48dp.
                        Surface(shape = RoundedCornerShape(8.dp), color = TagFill,
                            modifier = Modifier.minimumInteractiveComponentSize()) {
                            Row(Modifier.widthIn(max = 320.dp).heightIn(min = 32.dp).clickable { tags = tags - tag }
                                .semantics { contentDescription = "Remove tag $tag" }.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(tag, color = TagText, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f, fill = false))
                                Icon(painterResource(R.drawable.ic_close), null, Modifier.size(16.dp), tint = TagText)
                            }
                        }
                    }
                    Surface(Modifier.minimumInteractiveComponentSize().clickable { showTags = true }.testTag("$prefix-add-tag"),
                        shape = RoundedCornerShape(8.dp), color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, FormBorder)) {
                        Text(stringResource(R.string.form_add_tag), Modifier.heightIn(min = 32.dp).padding(horizontal = 16.dp, vertical = 6.dp),
                            fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium, color = FormMuted)
                    }
                }
                FormField(address, { address = it }, stringResource(R.string.destination_address), stringResource(R.string.form_address_hint), "$prefix-address",
                    Modifier.padding(top = 4.dp), minLines = 2, trailing = {
                        IconButton(onClick = { showAddress = true }) { Icon(painterResource(R.drawable.ic_map_pin), stringResource(R.string.form_address_details), Modifier.size(20.dp), tint = FormPurple) }
                    })
                FormField(phone, { phone = it; phoneError = false }, stringResource(R.string.destination_phone), stringResource(R.string.form_phone_hint), "$prefix-phone",
                    Modifier.padding(top = 12.dp), minLines = 1, keyboardType = KeyboardType.Phone, error = phoneError,
                    errorText = stringResource(R.string.form_phone_error), focusRequester = phoneFocus, trailing = {
                        IconButton(onClick = { if (phone.isNotBlank() && !phone.endsWith('\n')) phone += "\n"; phoneFocus.requestFocus(); keyboard?.show() }) {
                            Icon(painterResource(R.drawable.ic_add), stringResource(R.string.form_add_phone), Modifier.size(20.dp), tint = FormPurple)
                        }
                    })
                FormField(notes, { notes = it }, stringResource(R.string.details_visit_notes), stringResource(R.string.details_notes_hint), "$prefix-notes",
                    Modifier.padding(top = 12.dp), minLines = 3)
            }
        }
    }
    // Figma 52:325: left-aligned text, "Got it" on the right.
    if (showNameRequired) AppDialog(stringResource(R.string.form_name_required_title), { showNameRequired = false },
        Modifier.testTag("name-required-dialog"), body = stringResource(R.string.form_name_required_body)) {
        AppDialogButton(stringResource(R.string.details_got_it), { showNameRequired = false }, Modifier.testTag("name-required-ok"))
    }
    if (showPhoneError) AppDialog(stringResource(R.string.form_phone_error_title), { showPhoneError = false },
        Modifier.testTag("phone-error-dialog"), body = stringResource(R.string.form_phone_error)) {
        AppDialogButton(stringResource(R.string.details_got_it), { showPhoneError = false; phoneFocus.requestFocus() }, Modifier.testTag("phone-error-ok"))
    }
    if (showCoverPicker) VisitPhotoPicker(onDismiss = { showCoverPicker = false }, onSave = { uri, _ -> coverUri = uri; coverResource = 0; showCoverPicker = false },
        initialUri = coverUri.takeIf(String::isNotBlank), includeCaption = false, title = stringResource(R.string.form_restaurant_photo))
    if (showVisitPicker) VisitPhotoPicker(onDismiss = { showVisitPicker = false }, onSave = { uri, caption ->
        if (editingPhoto in uris.indices) {
            uris = uris.mapIndexed { i, old -> if (i == editingPhoto) uri else old }
            captions = captions.mapIndexed { i, old -> if (i == editingPhoto) caption else old }
        } else { uris = uris + uri; captions = captions + caption }
        showVisitPicker = false
    }, initialUri = uris.getOrNull(editingPhoto), initialCaption = captions.getOrNull(editingPhoto).orEmpty())
    if (showTags) TagEntryDialog(onDismiss = { showTags = false }, onAdd = { tag ->
        if (tags.none { it.equals(tag, ignoreCase = true) }) tags = tags + tag
        showTags = false
    })
    if (showAddress) AddressEntryDialog(address, onDismiss = { showAddress = false }, onSave = { address = it; showAddress = false })
    // Figma 76:271: 352 wide; guidance on the left; stars (232×48 drag row) and "4.5 / 5" share one row.
    // Save changes only the form's draft rating; the form's own Save commits it.
    if (showRating) AppDialog(stringResource(R.string.destination_your_rating), { showRating = false },
        Modifier.testTag("rating-dialog"), body = stringResource(R.string.form_rating_guidance), maxWidth = 352.dp,
        content = {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                StarRating(ratingDraft, { ratingDraft = it }, Modifier.testTag("rating-swipe-stars"), starSize = 46.4.dp)
                Spacer(Modifier.weight(1f).widthIn(min = 8.dp))
                Text("${formatRating(ratingDraft)} / 5", Modifier.testTag("rating-draft-value"),
                    color = FormInk, fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }) {
        AppDialogButton(stringResource(R.string.details_cancel), { showRating = false }, Modifier.testTag("rating-cancel"))
        AppDialogButton(stringResource(R.string.destination_save), { rating = ratingDraft; showRating = false }, Modifier.testTag("rating-save"))
    }
}

@Composable
private fun FormHeading(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier.semantics { heading() }, color = FormPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp)
}

@Composable
private fun FormField(value: String, onChange: (String) -> Unit, label: String, hint: String, tag: String, modifier: Modifier = Modifier,
    singleLine: Boolean = false, minLines: Int = 1, error: Boolean = false, errorText: String = "", keyboardType: KeyboardType = KeyboardType.Text,
    focusRequester: FocusRequester? = null, compact: Boolean = false, trailing: (@Composable () -> Unit)? = null) {
    var editor by rememberSaveable(tag, stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }
    LaunchedEffect(value) {
        if (editor.text != value) editor = TextFieldValue(value, TextRange(value.length))
    }
    val fieldModifier = Modifier.fillMaxWidth().heightIn(min = when (minLines) { 4 -> 128.dp; 3 -> 96.dp; 2 -> 80.dp; else -> 56.dp })
        .testTag(tag).semantics { contentDescription = label }
    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFF49454F), fontSize = if (compact) 14.sp else 16.sp, lineHeight = if (compact) 20.sp else 24.sp)
    Box(modifier.fillMaxWidth()) {
        OutlinedTextField(editor, { editor = it; onChange(it.text) }, if (focusRequester != null) fieldModifier.focusRequester(focusRequester) else fieldModifier,
            placeholder = { Text(hint, style = textStyle) }, shape = RoundedCornerShape(12.dp), singleLine = singleLine, minLines = minLines, maxLines = if (singleLine) 1 else 8,
            isError = error, supportingText = if (error) ({ Text(errorText) }) else null, trailingIcon = trailing,
            textStyle = textStyle,
            keyboardOptions = KeyboardOptions(capitalization = if (singleLine) KeyboardCapitalization.Words else KeyboardCapitalization.Sentences, keyboardType = keyboardType))
        Text(label, Modifier.padding(start = 12.dp).offset(y = (-8).dp).background(Color.White).padding(horizontal = 4.dp).clearAndSetSemantics { },
            color = if (error) MaterialTheme.colorScheme.error else Color(0xFF49454F), fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun FormPhoto(photo: RestaurantPhoto, tag: String, onEdit: (() -> Unit)?, onRemove: () -> Unit) {
    Box(Modifier.size(84.dp).testTag(tag)) {
        val photoModifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
        RestaurantPhotoImage(photo, stringResource(R.string.form_visit_photo), if (onEdit != null) photoModifier.clickable(onClick = onEdit) else photoModifier, ContentScale.Crop)
        IconButton(onClick = onRemove, modifier = Modifier.align(Alignment.TopEnd).size(48.dp)) {
            Surface(shape = RoundedCornerShape(50), color = Color.White) { Icon(painterResource(R.drawable.ic_close), stringResource(R.string.form_remove_photo), Modifier.size(24.dp), tint = FormPurple) }
        }
    }
}

@Composable
private fun TagEntryDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var draft by rememberSaveable { mutableStateOf("") }
    AppDialog(stringResource(R.string.form_tag_title), onDismiss, Modifier.testTag("tag-dialog"), content = {
        DialogTextField(draft, { draft = it }, stringResource(R.string.form_tag_label), "", Modifier.testTag("tag-input"),
            capitalization = KeyboardCapitalization.Words)
    }) {
        AppDialogButton(stringResource(R.string.details_cancel), onDismiss, Modifier.testTag("tag-cancel"))
        AppDialogButton(stringResource(R.string.form_tag_add), { onAdd(draft.trim()) }, Modifier.testTag("tag-save"), enabled = draft.isNotBlank())
    }
}

@Composable
private fun AddressEntryDialog(initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    val pieces = initial.split(',').map(String::trim)
    val region = if (pieces.size >= 3) pieces.last() else ""
    var street by rememberSaveable { mutableStateOf(if (pieces.size >= 3) pieces.dropLast(2).joinToString(", ") else pieces.firstOrNull().orEmpty()) }
    var city by rememberSaveable { mutableStateOf(if (pieces.size >= 3) pieces[pieces.lastIndex - 1] else pieces.getOrNull(1).orEmpty()) }
    var province by rememberSaveable { mutableStateOf(region.substringBefore(' ')) }
    var postal by rememberSaveable { mutableStateOf(region.substringAfter(' ', "")) }
    AppDialog(stringResource(R.string.form_address_details), onDismiss, Modifier.testTag("address-dialog"), content = {
        DialogTextField(street, { street = it }, stringResource(R.string.form_street), "", Modifier.testTag("address-street"),
            capitalization = KeyboardCapitalization.Words)
        DialogTextField(city, { city = it }, stringResource(R.string.form_city), "", Modifier.testTag("address-city"),
            capitalization = KeyboardCapitalization.Words)
        DialogTextField(province, { province = it }, stringResource(R.string.form_province), "", Modifier.testTag("address-province"),
            capitalization = KeyboardCapitalization.Characters)
        DialogTextField(postal, { postal = it.uppercase(Locale.ROOT) }, stringResource(R.string.form_postal), "", Modifier.testTag("address-postal"),
            capitalization = KeyboardCapitalization.Characters)
    }) {
        AppDialogButton(stringResource(R.string.details_cancel), onDismiss, Modifier.testTag("address-cancel"))
        AppDialogButton(stringResource(R.string.destination_save), {
            onSave(normalizeAddress(listOf(street.trim(), city.trim(), listOf(province.trim(), postal.trim())
                .filter(String::isNotEmpty).joinToString(" ")).filter(String::isNotEmpty).joinToString(", ")))
        }, Modifier.testTag("address-save"), enabled = street.isNotBlank())
    }
}
