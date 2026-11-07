package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gbc.comp3074.p12.restaurantguide.R
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTokens

/**
 * Put in Scaffold.bottomBar; apply and consume Scaffold's content padding on the form.
 * Owns its horizontal/bottom spacing and navigation/IME insets; do not pad it for IME twice.
 * At 412dp width: surface 372x72, Cancel 136x48, Save 200x48. Grows for large text.
 */
@Composable
fun ActionDock(
    cancel: () -> Unit,
    save: () -> Unit,
    modifier: Modifier = Modifier,
    saveEnabled: Boolean = true,
    cancelEnabled: Boolean = true,
    cancelLabel: String = stringResource(R.string.details_cancel),
    saveLabel: String = stringResource(R.string.destination_save),
    cancelModifier: Modifier = Modifier,
    saveModifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth()
        .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
        .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)) {
        Surface(
            Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
            color = RestaurantGuideTokens.surface, shadowElevation = 8.dp,
        ) {
            Row(
                Modifier.padding(12.dp).height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val labelStyle = TextStyle(fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp)
                OutlinedButton(
                    onClick = cancel,
                    enabled = cancelEnabled,
                    modifier = cancelModifier.weight(136f).heightIn(min = 48.dp).fillMaxHeight(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, RestaurantGuideTokens.dockBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RestaurantGuideTokens.primary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                ) { Text(cancelLabel, style = labelStyle) }
                Button(
                    onClick = save,
                    modifier = saveModifier.weight(200f).heightIn(min = 48.dp).fillMaxHeight(),
                    enabled = saveEnabled, shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RestaurantGuideTokens.primary,
                        contentColor = RestaurantGuideTokens.surface),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                ) { Text(saveLabel, style = labelStyle) }
            }
        }
    }
}
