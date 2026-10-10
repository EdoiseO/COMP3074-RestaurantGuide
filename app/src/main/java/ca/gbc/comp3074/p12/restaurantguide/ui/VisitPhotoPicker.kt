package ca.gbc.comp3074.p12.restaurantguide.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import ca.gbc.comp3074.p12.restaurantguide.R

/** Shared system-picker dialog for a restaurant cover, visit photo, or existing caption. */
@Composable
fun VisitPhotoPicker(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
    initialUri: String? = null,
    initialCaption: String = "",
    includeCaption: Boolean = true,
    title: String? = null,
) {
    var uri by rememberSaveable { mutableStateOf(initialUri?.takeIf(String::isNotBlank)) }
    var caption by rememberSaveable { mutableStateOf(initialCaption) }
    var launchFailed by rememberSaveable { mutableStateOf(false) }
    var selectionRevision by rememberSaveable { mutableIntStateOf(0) }
    // A restored or newly selected URI must be decoded before it is saveable.
    var status by remember(uri) { mutableStateOf(PhotoLoadStatus.Loading) }
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { selected ->
        if (selected != null) {
            status = PhotoLoadStatus.Loading
            uri = selected.toString()
            selectionRevision += 1
            launchFailed = false
        }
    }
    AlertDialog(
        modifier = Modifier.testTag("visit-photo-picker"),
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = { Text(title ?: stringResource(R.string.details_add_visit_photo)) },
        text = {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                Button(
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
                ) { Text(stringResource(R.string.details_choose_photo)) }
                if (launchFailed || (uri != null && status == PhotoLoadStatus.Unavailable)) {
                    Text(
                        stringResource(if (launchFailed) R.string.visit_photo_picker_unavailable else R.string.visit_photo_reselect),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("visit-photo-error"),
                    )
                }
                if (includeCaption) {
                    OutlinedTextField(caption, { caption = it }, Modifier.fillMaxWidth().testTag("visit-photo-caption"),
                        label = { Text(stringResource(R.string.details_caption)) },
                        placeholder = { Text(stringResource(R.string.details_caption_hint)) },
                        shape = RoundedCornerShape(12.dp), minLines = 2, maxLines = 4)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.details_cancel)) } },
        confirmButton = {
            TextButton(
                modifier = Modifier.testTag("visit-photo-save"),
                enabled = uri != null && status == PhotoLoadStatus.Ready,
                onClick = {
                    uri?.takeIf { status == PhotoLoadStatus.Ready }?.let { selected ->
                        if (selected.toUri().scheme == "content") {
                            try {
                                context.contentResolver.takePersistableUriPermission(
                                    selected.toUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION,
                                )
                            } catch (_: SecurityException) {
                                // A readable temporary grant is valid for this session. The loader
                                // reports an unavailable photo if a provider later revokes access.
                            }
                        }
                        onSave(selected, if (includeCaption) caption.trim() else "")
                    }
                },
            ) { Text(stringResource(R.string.destination_save)) }
        },
    )
}
