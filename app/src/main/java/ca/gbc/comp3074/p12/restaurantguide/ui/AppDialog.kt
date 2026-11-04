package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTokens

/** Compact modal; content scrolls when keyboard, preview or large text exceeds available height. */
@Composable
fun AppDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    body: String? = null,
    maxWidth: Dp = 336.dp,
    content: (@Composable ColumnScope.() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(Modifier.safeDrawingPadding().imePadding().padding(24.dp)) {
            Surface(
                modifier = modifier.widthIn(max = maxWidth).fillMaxWidth().heightIn(max = maxHeight),
                shape = RoundedCornerShape(20.dp),
                color = RestaurantGuideTokens.surface,
                contentColor = RestaurantGuideTokens.ink,
            ) {
                Column(
                    Modifier.verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(if (content == null) 6.dp else 10.dp),
                ) {
                    Text(title, style = TextStyle(
                        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,
                        fontSize = 17.sp, lineHeight = 24.sp, color = RestaurantGuideTokens.ink,
                    ))
                    if (body != null) Text(body, style = TextStyle(
                        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal,
                        fontSize = 14.sp, lineHeight = 20.3.sp, color = RestaurantGuideTokens.muted,
                    ))
                    content?.invoke(this)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, androidx.compose.ui.Alignment.End),
                        content = actions,
                    )
                }
            }
        }
    }
}

/** Use in AppDialog.actions; text can wrap while the target remains at least 48dp high. */
@Composable
fun AppDialogButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (destructive) RestaurantGuideTokens.danger else RestaurantGuideTokens.dialogAction,
        ),
    ) {
        Text(text, style = TextStyle(
            fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,
            fontSize = 14.sp, lineHeight = 24.sp,
        ))
    }
}
