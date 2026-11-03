package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class RedesignComponentsTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun dockMatchesReferenceWidthsAndDispatchesCallbacks() {
        var cancels = 0
        var saves = 0
        compose.setContent {
            RestaurantGuideTheme {
                Box(Modifier.requiredWidth(412.dp)) {
                    ActionDock(cancel = { cancels++ }, save = { saves++ },
                        cancelModifier = Modifier.testTag("cancel"), saveModifier = Modifier.testTag("save"))
                }
            }
        }
        compose.onNodeWithTag("cancel").assertWidthIsEqualTo(136.dp).assertHeightIsEqualTo(48.dp).performClick()
        compose.onNodeWithTag("save").assertWidthIsEqualTo(200.dp).assertHeightIsEqualTo(48.dp).performClick()
        compose.runOnIdle { assertEquals(1, cancels); assertEquals(1, saves) }
    }

    @Test fun disabledSaveDoesNotDispatchButCancelStillWorksAtNarrowWidth() {
        var saves = 0
        var cancels = 0
        compose.setContent {
            RestaurantGuideTheme {
                Box(Modifier.requiredWidth(320.dp)) {
                    ActionDock(cancel = { cancels++ }, save = { saves++ }, saveEnabled = false,
                        cancelModifier = Modifier.testTag("cancel"), saveModifier = Modifier.testTag("save"))
                }
            }
        }
        compose.onNodeWithTag("save").assertIsNotEnabled().performClick()
        compose.onNodeWithTag("cancel").assertHeightIsAtLeast(48.dp).performClick()
        compose.runOnIdle { assertEquals(0, saves); assertEquals(1, cancels) }
    }

    @Test fun dialogActionDismissesWithoutInvokingAnotherAction() {
        val open = mutableStateOf(true)
        var saves = 0
        compose.setContent {
            RestaurantGuideTheme {
                if (open.value) AppDialog(title = "Visit notes", body = "Keep this draft.",
                    onDismissRequest = { open.value = false }, actions = {
                        AppDialogButton("Cancel", { open.value = false })
                        AppDialogButton("Save", { saves++ })
                    })
            }
        }
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Visit notes").assertDoesNotExist()
        compose.runOnIdle { assertEquals(0, saves) }
    }

    @Test fun longDialogAtLargeFontKeepsActionsReachableByScrolling() {
        var saves = 0
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                RestaurantGuideTheme {
                    AppDialog(title = "Long visit notes", body = "A long memory. ".repeat(100),
                        onDismissRequest = {}, actions = {
                            AppDialogButton("Save", { saves++ }, Modifier.testTag("save"))
                        })
                }
            }
        }
        compose.onNodeWithTag("save").performScrollTo().assertIsDisplayed()
            .assertHeightIsAtLeast(48.dp).performClick()
        compose.runOnIdle { assertEquals(1, saves) }
    }
}
