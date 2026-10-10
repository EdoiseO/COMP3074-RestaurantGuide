package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gbc.comp3074.p12.restaurantguide.R
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant

@Composable
fun AddRestaurantEntry(onBack: () -> Unit, onSave: (Restaurant) -> Unit) {
    RestaurantFormEntry(onBack = onBack, onSave = onSave)
}

/** About shares the app's compact toolbar; its information scrolls independently. */
@Composable
fun AboutEntry(onBack: () -> Unit) {
    Scaffold(containerColor = Color.White) { contentPadding ->
        Column(Modifier.fillMaxSize().padding(contentPadding).testTag("about-screen")) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp).testTag("about-back")) {
                    Text(stringResource(R.string.destination_back), fontSize = 16.sp)
                }
                Text(
                    stringResource(R.string.destination_about_title),
                    Modifier.weight(1f).padding(start = 8.dp).semantics { heading() },
                    color = Color(0xFF4F378A), fontSize = 18.sp, lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Column(
                Modifier.fillMaxWidth().weight(1f)
                    .verticalScroll(rememberScrollState()).testTag("about-body")
                    .padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFF1ECF7),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.app_name), Modifier.semantics { heading() },
                            fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F378A),
                        )
                        Text(
                            stringResource(R.string.destination_project_info),
                            style = MaterialTheme.typography.labelLarge, color = Color(0xFF4F378A),
                        )
                        Text(
                            stringResource(R.string.destination_application_info),
                            Modifier.testTag("about-purpose"),
                            style = MaterialTheme.typography.bodyLarge, color = Color(0xFF49454F),
                        )
                    }
                }
                Surface(
                    Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE7E0EC)),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.destination_team_members), Modifier.semantics { heading() },
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F378A),
                        )
                        Text(
                            stringResource(R.string.destination_team_names), Modifier.testTag("about-team-names"),
                            style = MaterialTheme.typography.bodyLarge, color = Color(0xFF49454F),
                        )
                    }
                }
            }
        }
    }
}
