package ca.gbc.comp3074.p12.restaurantguide.ui

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gbc.comp3074.p12.restaurantguide.R
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTokens

// Figma 45:163 measured values for dialog fields and the photo chooser.
private val FieldFill = Color(0xFFFBF8F4)
private val FieldBorder = Color(0xFFDCD4C8)
private val FieldLabel = Color(0xFF4B2E83)
private val ChooserFill = Color(0xFFF6F2FB)
private val ChooserBorder = Color(0xFFB9A9D6)

/** Shared system-picker dialog for a restaurant cover, visit photo, or existing caption. */
@Composable
fun VisitPhotoPicker(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    initialUri: String? = null,
    initialCaption: String = "",
    includeCaption: Boolean = true,
    saving: Boolean = false,
    saveError: String? = null,
    title: String? = null,
    body: String? = if (title == null) stringResource(R.string.visit_photo_body) else null,
) {
    var uri by rememberSaveable { mutableStateOf(initialUri?.takeIf(String::isNotBlank)) }
    var caption by rememberSaveable { mutableStateOf(initialCaption) }
    var launchFailed by rememberSaveable { mutableStateOf(false) }
    var selectionRevision by rememberSaveable { mutableIntStateOf(0) }
    // A restored or newly selected URI must be decoded before it is saveable.
    var status by remember(uri) { mutableStateOf(PhotoLoadStatus.Loading) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { selected ->
        if (selected != null) {
            status = PhotoLoadStatus.Loading
            uri = selected.toString()
            selectionRevision += 1
            launchFailed = false
        }
    }
    val saveReady = uri != null && status == PhotoLoadStatus.Ready && !saving
    AppDialog(
        title = title ?: stringResource(R.string.details_add_visit_photo),
        onDismissRequest = { if (!saving) onDismiss() },
        modifier = Modifier.testTag("visit-photo-picker"),
        body = body,
        content = {
            if (saveError != null) Text(saveError, color = MaterialTheme.colorScheme.error, modifier = Modifier.testTag("photo-save-error"))
            uri?.let { selected ->
                key(selectionRevision) {
                    RestaurantPhotoImage(
                        RestaurantPhoto(uri = selected),
                        stringResource(R.string.details_selected_photo),
                        Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp))
                            .testTag("visit-photo-preview"),
                        ContentScale.Fit,
                        onStatusChanged = { status = it },
                    )
                }
            }
            PhotoChooserButton(
                modifier = Modifier.testTag("visit-photo-choose"),
                onClick = {
                    launchFailed = false
                    try {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    } catch (_: ActivityNotFoundException) {
                        launchFailed = true
                    } catch (_: SecurityException) {
                        launchFailed = true
                    }
                },
            )
            if (launchFailed || (uri != null && status == PhotoLoadStatus.Unavailable)) {
                Text(
                    stringResource(if (launchFailed) R.string.visit_photo_picker_unavailable else R.string.visit_photo_reselect),
                    color = MaterialTheme.colorScheme.error, fontSize = 14.sp, lineHeight = 20.sp,
                    modifier = Modifier.testTag("visit-photo-error"),
                )
            }
            if (includeCaption) {
                DialogTextField(caption, { caption = it }, stringResource(R.string.details_caption),
                    stringResource(R.string.visit_photo_caption_hint), Modifier.testTag("visit-photo-caption"))
            }
        },
    ) {
        AppDialogButton(stringResource(R.string.details_cancel), onDismiss, Modifier.testTag("visit-photo-cancel"), enabled = !saving)
        AppDialogButton(
            stringResource(R.string.destination_save),
            enabled = saveReady && !saving,
            modifier = Modifier.testTag("visit-photo-save"),
            onClick = {
                uri?.takeIf { status == PhotoLoadStatus.Ready }?.let { selected ->
                    onSave(selected, if (includeCaption) caption.trim() else "")
                }
            },
        )
    }
}

/** Full-width dashed chooser: 48 high, radius 12, camera icon and "Choose photo" on the left. */
@Composable
private fun PhotoChooserButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier.fillMaxWidth().heightIn(min = 48.dp).clip(shape).background(ChooserFill)
            .drawBehind {
                val stroke = 1.dp.toPx()
                drawRoundRect(
                    ChooserBorder, cornerRadius = CornerRadius(12.dp.toPx()),
                    topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
                    size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                    style = Stroke(stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))),
                )
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(painterResource(R.drawable.ic_camera), null, Modifier.size(18.dp), tint = RestaurantGuideTokens.dialogAction)
        Text(stringResource(R.string.visit_photo_choose), color = RestaurantGuideTokens.dialogAction,
            fontSize = 14.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Dialog field from Figma 45:163: label (Bold 12/17) above a filled input (48 high, radius 12,
 * FBF8F4 fill, 1px DCD4C8). Grows with multi-line text.
 */
@Composable
internal fun DialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: androidx.compose.ui.unit.Dp = 48.dp,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = FieldLabel, fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth().semantics { contentDescription = label },
            singleLine = singleLine,
            textStyle = TextStyle(color = RestaurantGuideTokens.ink, fontSize = 14.sp, lineHeight = 20.3.sp),
            cursorBrush = SolidColor(RestaurantGuideTokens.dialogAction),
            keyboardOptions = KeyboardOptions(capitalization = capitalization),
            decorationBox = { inner ->
                Box(
                    Modifier.fillMaxWidth().heightIn(min = minHeight)
                        .background(FieldFill, RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, FieldBorder), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart,
                ) {
                    if (value.isEmpty()) Text(placeholder, color = RestaurantGuideTokens.muted, fontSize = 14.sp, lineHeight = 20.3.sp)
                    inner()
                }
            },
        )
    }
}
