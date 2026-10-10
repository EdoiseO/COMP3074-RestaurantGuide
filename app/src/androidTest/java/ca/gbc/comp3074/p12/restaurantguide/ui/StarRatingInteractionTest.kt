package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performSemanticsAction
import ca.gbc.comp3074.p12.restaurantguide.data.formatRating
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
class StarRatingInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun keyboardAdjustsInHalfStepsWithoutIncreasingAnUnratedDecrement() {
        compose.setContent { RestaurantGuideTheme { RatingFixture() } }
        compose.onNodeWithTag("rating-test-stars").performKeyInput {
            keyDown(Key.DirectionLeft)
            keyUp(Key.DirectionLeft)
        }
        compose.onNodeWithTag("rating-test-value").assertTextEquals("0.0")
        compose.onNodeWithTag("rating-test-stars").performKeyInput {
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }
        compose.onNodeWithTag("rating-test-value").assertTextEquals("0.5")
        compose.onNodeWithTag("rating-test-stars").performKeyInput {
            keyDown(Key.DirectionRight)
            keyUp(Key.DirectionRight)
        }
        compose.onNodeWithTag("rating-test-value").assertTextEquals("1.0")
        compose.onNodeWithTag("rating-test-stars").performKeyInput {
            keyDown(Key.DirectionLeft)
            keyUp(Key.DirectionLeft)
        }
        compose.onNodeWithTag("rating-test-value").assertTextEquals("0.5")
        compose.onNodeWithTag("rating-test-stars").performKeyInput {
            keyDown(Key.DirectionLeft)
            keyUp(Key.DirectionLeft)
        }
        compose.onNodeWithTag("rating-test-value").assertTextEquals("0.5")
    }

    @Test
    fun accessibilityAdjustmentSnapsAndReportsUnratedNoOpAccurately() {
        compose.setContent { RestaurantGuideTheme { RatingFixture() } }
        compose.onNodeWithTag("rating-test-stars").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Not rated"),
        )
        var handled: Boolean? = null
        compose.onNodeWithTag("rating-test-stars").performSemanticsAction(SemanticsActions.SetProgress) {
            handled = it(0f)
        }
        assertEquals(false, handled)
        compose.onNodeWithTag("rating-test-value").assertTextEquals("0.0")
        compose.onNodeWithTag("rating-test-stars").performSemanticsAction(SemanticsActions.SetProgress) {
            handled = it(4.49f)
        }
        assertEquals(true, handled)
        compose.onNodeWithTag("rating-test-value").assertTextEquals("4.5")
        compose.onNodeWithTag("rating-test-stars").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "4.5 out of 5 stars"),
        )
        compose.onNodeWithTag("rating-test-stars").performSemanticsAction(SemanticsActions.SetProgress) {
            handled = it(4.5f)
        }
        assertEquals(false, handled)
    }

    @Composable
    private fun RatingFixture() {
        var rating by remember { mutableStateOf(0f) }
        val focus = remember { FocusRequester() }
        Column {
            StarRating(
                rating = rating,
                onRatingChange = { rating = it },
                modifier = Modifier.focusRequester(focus).testTag("rating-test-stars"),
            )
            Text(formatRating(rating), Modifier.testTag("rating-test-value"))
        }
        LaunchedEffect(Unit) { focus.requestFocus() }
    }
}
