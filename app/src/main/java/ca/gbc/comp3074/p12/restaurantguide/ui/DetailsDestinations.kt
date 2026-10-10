package ca.gbc.comp3074.p12.restaurantguide.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gbc.comp3074.p12.restaurantguide.R
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import ca.gbc.comp3074.p12.restaurantguide.data.restaurantShareBody
import java.util.Locale

/** The same full form used for Add, prefilled from the selected record. */
@Composable
fun RestaurantEditEntry(restaurant: Restaurant, onBack: () -> Unit, onSave: (Restaurant) -> Unit) {
    RestaurantFormEntry(restaurant, onBack, onSave)
}

/** Real static images retain their attribution; fallback streets intentionally do not claim geocoding. */
@Composable
fun RestaurantMapPreview(restaurant: Restaurant, modifier: Modifier = Modifier, showPreviewLabel: Boolean = true) {
    val mapDescription = stringResource(R.string.details_map_preview_description, restaurant.name)
    if (restaurant.mapRes != null) {
        Image(
            painter = painterResource(restaurant.mapRes),
            contentDescription = mapDescription,
            modifier = modifier.clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Fit,
        )
    } else {
        val illustrativeDescription = stringResource(
            R.string.details_map_illustrative_description, restaurant.name,
            restaurant.address.ifBlank { stringResource(R.string.details_map_missing_address) },
        )
        Box(
            modifier = modifier.clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF1F2EF))
                .semantics { contentDescription = illustrativeDescription },
        ) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Color(0xFFDDEBDD), topLeft = Offset(size.width * .03f, size.height * .06f),
                    size = androidx.compose.ui.geometry.Size(size.width * .28f, size.height * .38f))
                drawRect(Color(0xFFE3EAF4), topLeft = Offset(size.width * .74f, 0f),
                    size = androidx.compose.ui.geometry.Size(size.width * .26f, size.height))
                val road = Color.White
                val roadEdge = Color(0xFFD0D4D0)
                listOf(.25f, .57f, .80f).forEach { x ->
                    drawLine(roadEdge, Offset(size.width*x, 0f), Offset(size.width*(x-.15f), size.height), 19.dp.toPx())
                    drawLine(road, Offset(size.width*x, 0f), Offset(size.width*(x-.15f), size.height), 15.dp.toPx())
                }
                listOf(.26f, .62f).forEach { y ->
                    drawLine(roadEdge, Offset(0f, size.height*y), Offset(size.width, size.height*(y+.09f)), 19.dp.toPx())
                    drawLine(road, Offset(0f, size.height*y), Offset(size.width, size.height*(y+.09f)), 15.dp.toPx())
                }
            }
            Icon(
                painterResource(R.drawable.ic_map_pin), contentDescription = null,
                tint = Color(0xFFE94235),
                modifier = Modifier.align(Alignment.Center).size(36.dp),
            )
            if (showPreviewLabel) Surface(
                color = Color.White.copy(alpha = .94f), shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            ) {
                Text(stringResource(R.string.details_map_preview_label),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

fun getDirectionsUri(restaurant: Restaurant): Uri = (
    "https://www.google.com/maps/dir/?api=1&destination=" +
        Uri.encode("${restaurant.name}, ${restaurant.address}")
).toUri()

fun restaurantEmailIntent(restaurant: Restaurant): Intent = Intent(Intent.ACTION_SENDTO).apply {
    data = "mailto:".toUri()
    putExtra(Intent.EXTRA_SUBJECT, restaurant.name)
    putExtra(Intent.EXTRA_TEXT, restaurantShareBody(restaurant))
}

/** Starts only an email draft. If the device has no mail handler, display a copyable preview. */
@Composable
fun RestaurantEmailEntry(restaurant: Restaurant, onBack: () -> Unit) {
    val context: Context = LocalContext.current
    var unavailable by rememberSaveable(restaurant.id) { mutableStateOf(false) }
    var attempted by rememberSaveable(restaurant.id) { mutableStateOf(false) }
    BackHandler(onBack = onBack)
    LaunchedEffect(restaurant.id) {
        if (!attempted) {
            attempted = true
            try {
                context.startActivity(restaurantEmailIntent(restaurant))
                onBack()
            } catch (_: ActivityNotFoundException) {
                unavailable = true
            }
        } else if (!unavailable) {
            // A restored activity may have already handed the draft to an external app.
            onBack()
        }
    }
    if (unavailable) {
        AlertDialog(
            onDismissRequest = onBack,
            modifier = Modifier.testTag("email-preview"),
            title = { Text(stringResource(R.string.details_email_unavailable_title)) },
            text = {
                Column(Modifier.height(360.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.details_email_unavailable_message))
                    SelectionContainer { Text(restaurantShareBody(restaurant)) }
                }
            },
            confirmButton = { TextButton(onClick = onBack) {
                Text(stringResource(R.string.details_email_close))
            } },
        )
    }
}
