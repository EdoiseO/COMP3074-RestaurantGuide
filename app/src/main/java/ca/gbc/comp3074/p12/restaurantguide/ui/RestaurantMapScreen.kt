package ca.gbc.comp3074.p12.restaurantguide.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gbc.comp3074.p12.restaurantguide.R
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant

/** The selected saved location, with a preview and an external directions handoff. */
@Composable
fun RestaurantMapEntry(restaurant: Restaurant, onBack: () -> Unit) {
    val context = LocalContext.current
    val selectedMap = restaurant.fullMapRes ?: restaurant.mapRes
    var directionsUnavailable by rememberSaveable(restaurant.id) { mutableStateOf(false) }
    BackHandler(onBack = onBack)

    Scaffold(containerColor = Color.White) { insets ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(insets).testTag("full-map-screen")) {
            // Only the saved information scrolls. Back and directions remain available
            // when a long address or a short window leaves less room for the map.
            val infoMaxHeight = maxOf(48.dp, maxHeight * .22f)
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack, modifier = Modifier.testTag("map-back")) {
                        Text(stringResource(R.string.destination_back), fontSize = 16.sp)
                    }
                    Text(stringResource(R.string.details_map_title),
                        Modifier.padding(start = 8.dp).weight(1f).semantics { heading() },
                        fontSize = 18.sp, lineHeight = 24.sp,
                        fontWeight = FontWeight.Medium, color = Color(0xFF4F378A))
                }
                Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 12.dp)
                    .clip(RoundedCornerShape(24.dp)).background(Color(0xFFF1F2EF))) {
                    RestaurantMapPreview(
                        restaurant.copy(mapRes = selectedMap),
                        Modifier.fillMaxSize().testTag("full-map-preview"),
                        showPreviewLabel = false,
                    )
                    Surface(Modifier.align(Alignment.TopEnd).padding(12.dp),
                        shape = RoundedCornerShape(12.dp), color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE7E0EC))) {
                        Text(stringResource(R.string.details_map_preview_label),
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp).testTag("map-static-label"),
                            color = Color(0xFF4F378A), style = MaterialTheme.typography.labelMedium)
                    }
                }
                Surface(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp), color = Color(0xFFF1ECF7)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(Modifier.fillMaxWidth().heightIn(max = infoMaxHeight)
                            .verticalScroll(rememberScrollState()).testTag("map-info"),
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(restaurant.name, Modifier.testTag("map-name").semantics { heading() },
                                fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold,
                                color = Color(0xFF4F378A))
                            if (selectedMap == null) {
                                Text(stringResource(R.string.details_map_illustrative_note),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(restaurant.address.ifBlank { stringResource(R.string.details_map_missing_address) },
                                Modifier.testTag("map-address"), style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF49454F))
                        }
                        Button(onClick = {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, getDirectionsUri(restaurant)))
                            } catch (_: ActivityNotFoundException) {
                                directionsUnavailable = true
                            }
                        }, enabled = restaurant.address.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("get-directions"),
                            shape = RoundedCornerShape(24.dp)) {
                            Icon(painterResource(R.drawable.ic_map_pin), null, Modifier.size(20.dp))
                            Text(stringResource(R.string.details_get_directions), Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        }
    }
    if (directionsUnavailable) {
        AlertDialog(onDismissRequest = { directionsUnavailable = false },
            shape = RoundedCornerShape(24.dp), containerColor = Color.White,
            title = { Text(stringResource(R.string.details_directions_unavailable_title),
                fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.details_directions_unavailable_message)) },
            confirmButton = { TextButton(onClick = { directionsUnavailable = false }) {
                Text(stringResource(R.string.details_got_it))
            } })
    }
}
