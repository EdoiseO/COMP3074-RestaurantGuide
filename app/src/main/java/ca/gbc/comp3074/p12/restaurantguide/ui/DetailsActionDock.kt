package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gbc.comp3074.p12.restaurantguide.R
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTokens

/** Reserved by Scaffold so the final notes and edit control can scroll clear of the dock. */
@Composable
internal fun DetailsActionDock(onShare: () -> Unit, onMap: () -> Unit, onDelete: () -> Unit) {
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 20.dp, end = 20.dp, bottom = 16.dp, top = 8.dp)) {
        Surface(Modifier.fillMaxWidth().testTag("details-action-dock"), shape = RoundedCornerShape(24.dp),
            color = RestaurantGuideTokens.surface, shadowElevation = 8.dp) {
            Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                DockIcon(R.drawable.ic_share, stringResource(R.string.details_share), onShare, false, "details-share")
                Button(onClick = onMap, modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("details-full-map"),
                    shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RestaurantGuideTokens.primary, contentColor = RestaurantGuideTokens.surface)) {
                    Icon(painterResource(R.drawable.ic_map), null, Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.details_open_maps), fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
                }
                DockIcon(R.drawable.ic_delete, stringResource(R.string.details_delete), onDelete, true, "details-delete")
            }
        }
    }
}

@Composable
private fun DockIcon(icon: Int, label: String, onClick: () -> Unit, destructive: Boolean, tag: String) {
    val color = if (destructive) RestaurantGuideTokens.danger else RestaurantGuideTokens.primary
    Surface(onClick = onClick, modifier = Modifier.size(48.dp).testTag(tag),
        shape = RoundedCornerShape(12.dp), color = RestaurantGuideTokens.surface,
        border = BorderStroke(1.dp, color.copy(alpha = .22f))) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), label, Modifier.size(24.dp), tint = color)
        }
    }
}
