package ca.gbc.comp3074.p12.restaurantguide.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeLeft
import androidx.core.net.toUri
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import ca.gbc.comp3074.p12.restaurantguide.R
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GalleryZoomFlowTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val photos = listOf(
        RestaurantPhoto(resourceId = R.drawable.two_brothers_meal, title = "Meal", caption = "Order this again."),
        RestaurantPhoto(resourceId = R.drawable.two_brothers_chicken, title = "Chicken shawarma plate"),
        RestaurantPhoto(resourceId = R.drawable.two_brothers_falafel, title = "Falafel plate"),
    )

    @Test fun buttonsRespectBoundsAndPercentageResetsToFit() {
        showViewer()
        waitForPhoto()
        assertZoom(1f)
        compose.onNodeWithContentDescription("Zoom out").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Reset zoom").assertIsNotEnabled()
        repeat(4) { compose.onNodeWithContentDescription("Zoom in").performClick() }
        assertZoom(3f)
        compose.onNodeWithContentDescription("Zoom in").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Zoom out").assertIsEnabled().performClick()
        assertZoom(2.5f)
        compose.onNodeWithText("Order this again.").assertIsDisplayed()
        snapshot("gallery-zoom-controls")
        compose.onNodeWithContentDescription("Reset zoom").performClick()
        assertZoom(1f)
        assertEquals(Offset.Zero, panOffset())
        compose.onNodeWithContentDescription("Zoom in").assertIsEnabled()
    }

    @Test fun swipeAtFitAndArrowThumbnailChangesResetZoom() {
        showViewer()
        waitForPhoto()
        compose.onNodeWithTag("photo-zoom-viewport").performTouchInput { swipeLeft() }
        compose.onNodeWithText("2 / 3 · My photos").assertIsDisplayed()
        waitForPhoto()
        assertZoom(1f)
        compose.onNodeWithContentDescription("Zoom in").performClick()
        compose.onNodeWithContentDescription("Next photo").performClick()
        compose.onNodeWithText("3 / 3 · My photos").assertIsDisplayed()
        waitForPhoto()
        assertZoom(1f)
        compose.onNodeWithContentDescription("Zoom in").performClick()
        compose.onNodeWithContentDescription("View photo 1 of 3").performClick()
        compose.onNodeWithText("1 / 3 · My photos").assertIsDisplayed()
        waitForPhoto()
        assertZoom(1f)
        compose.onNodeWithContentDescription("Zoom in").performClick()
        compose.onNodeWithContentDescription("Previous photo").performClick()
        compose.onNodeWithText("3 / 3 · My photos").assertIsDisplayed()
        waitForPhoto()
        assertZoom(1f)
        assertEquals(Offset.Zero, panOffset())
    }

    @Test fun pinchPansWithinImageBoundsAndRestoresNormalSwipe() {
        showViewer()
        waitForPhoto()
        pinchOut()
        assertZoom(3f)
        compose.onNodeWithText("1 / 3 · My photos").assertIsDisplayed()
        repeat(4) {
            compose.onNodeWithTag("photo-zoom-viewport").performTouchInput {
                swipe(Offset(width * .4f, height * .4f), Offset(width * .75f, height * .65f), 600)
            }
        }
        val offset = panOffset()
        val limit = panLimit()
        assertTrue("Enlarged image should actually pan", offset.x > 0f)
        assertTrue("Pan X stays bounded by the fitted image", abs(offset.x) <= limit.x + .5f)
        assertTrue("Pan Y stays bounded by the fitted image", abs(offset.y) <= limit.y + .5f)
        assertEquals("Repeated pan reaches the image edge", limit.x, offset.x, .5f)
        compose.onNodeWithText("1 / 3 · My photos").assertIsDisplayed()
        snapshot("gallery-pinch-pan")
        compose.onNodeWithTag("photo-zoom-viewport").performTouchInput {
            pinch(
                start0 = Offset(width * .18f, center.y), end0 = Offset(width * .49f, center.y),
                start1 = Offset(width * .82f, center.y), end1 = Offset(width * .51f, center.y),
                durationMillis = 600,
            )
        }
        assertZoom(1f)
        assertEquals(Offset.Zero, panOffset())
        compose.onNodeWithTag("photo-zoom-viewport").performTouchInput { swipeLeft() }
        compose.onNodeWithText("2 / 3 · My photos").assertIsDisplayed()
        assertZoom(1f)
    }

    @Test fun selectedUriPhotoUsesTheSameZoomAndPanGestures() {
        val imageFile = compose.activity.cacheDir.resolve("gallery-zoom-fixture.png")
        val bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.BLUE
        canvas.drawRect(0f, 0f, 400f, 300f, paint)
        paint.color = Color.GREEN
        canvas.drawRect(400f, 0f, 800f, 300f, paint)
        paint.color = Color.RED
        canvas.drawRect(0f, 300f, 400f, 600f, paint)
        paint.color = Color.YELLOW
        canvas.drawRect(400f, 300f, 800f, 600f, paint)
        imageFile.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        try {
            showViewer(listOf(RestaurantPhoto(uri = imageFile.toUri().toString(), caption = "Visit photo")))
            waitForPhoto()
            compose.onNodeWithContentDescription("Zoom in").performClick()
            assertZoom(1.5f)
            pinchOut()
            assertZoom(3f)
            compose.onNodeWithTag("photo-zoom-viewport").performTouchInput {
                swipe(Offset(width * .4f, height * .4f), Offset(width * .7f, height * .6f), 600)
            }
            assertTrue(panOffset().x > 0f)
            compose.onNodeWithText("Visit photo").assertIsDisplayed()
            snapshot("gallery-uri-zoom")
            compose.onNodeWithContentDescription("Reset zoom").performClick()
            assertZoom(1f)
            compose.onNodeWithContentDescription("Close photos").performClick()
            compose.onNodeWithText("Gallery closed").assertIsDisplayed()
        } finally {
            imageFile.delete()
        }
    }

    @Test fun unavailablePhotoDisablesZoomAndKeepsCaption() {
        showViewer(listOf(RestaurantPhoto(uri = "content://unavailable-provider/gallery-photo", caption = "Remember the meal")))
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Photo unavailable").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("Zoom in").assertIsNotEnabled()
        compose.onNodeWithContentDescription("Zoom out").assertIsNotEnabled()
        assertZoom(1f)
        compose.onNodeWithText("Remember the meal").assertIsDisplayed()
        compose.onNodeWithContentDescription("Close photos").performClick()
        compose.onNodeWithText("Gallery closed").assertIsDisplayed()
    }

    @Test fun shortWideViewportKeepsImageControlsAndLongCaptionReachable() {
        val longCaption = ("Chicken shawarma plate — order this again. ".repeat(16)) + "Final memory."
        showViewer(photos.mapIndexed { index, photo ->
            if (index == 0) photo.copy(caption = longCaption) else photo
        }, Modifier.size(width = 400.dp, height = 320.dp))
        waitForPhoto()
        val viewport = compose.onNodeWithTag("photo-zoom-viewport").fetchSemanticsNode().boundsInRoot
        assertTrue("Compact gallery keeps at least a 48dp image/arrow area",
            viewport.height >= 48f * compose.activity.resources.displayMetrics.density)
        compose.onNodeWithContentDescription("Close photos").assertIsDisplayed()
        compose.onNodeWithContentDescription("Zoom in").assertIsDisplayed().performClick()
        compose.onNodeWithContentDescription("Reset zoom").performClick()
        compose.onNodeWithContentDescription("Next photo").assertIsDisplayed()
        val caption = compose.onNodeWithTag("photo-viewer-caption").fetchSemanticsNode().config[
            SemanticsProperties.VerticalScrollAxisRange,
        ]
        assertTrue("Long caption remains fully scrollable", caption.maxValue() > 0f)
        compose.onNodeWithTag("photo-viewer-caption").performTouchInput {
            swipe(Offset(center.x, height - 1f), Offset(center.x, 1f), 600)
        }
        compose.waitUntil(5_000) { caption.value() > 0f }
        snapshot("gallery-short-wide-long-caption")
        compose.onNodeWithContentDescription("View photo 2 of 3").assertIsDisplayed().performClick()
        compose.onNodeWithText("2 / 3 · My photos").assertIsDisplayed()
        waitForPhoto()
        assertZoom(1f)
        compose.onNodeWithContentDescription("Close photos").performClick()
        compose.onNodeWithText("Gallery closed").assertIsDisplayed()
    }

    @Test fun closeAndHardwareBackReturnToTheSameDetailsWhileZoomed() {
        compose.setContent { RestaurantGuideTheme { RestaurantListFlow() } }
        compose.onNodeWithTag("restaurant-list").performScrollToNode(hasTestTag("restaurant-card-1"))
        compose.onNodeWithTag("restaurant-card-1").performClick()
        compose.onNodeWithTag("details-photo-0").performScrollTo().performClick()
        waitForPhoto()
        compose.onNodeWithContentDescription("Zoom in").performClick()
        compose.onNodeWithContentDescription("Close photos").performClick()
        compose.onNodeWithTag("restaurant-details").assertIsDisplayed()
        compose.onNodeWithTag("details-photo-0").performClick()
        waitForPhoto()
        assertZoom(1f)
        compose.onNodeWithContentDescription("Zoom in").performClick()
        Espresso.pressBack()
        compose.onNodeWithTag("restaurant-details").assertIsDisplayed()
        compose.onNodeWithText("Two Brothers Shawarma").performScrollTo().assertIsDisplayed()
    }

    private fun showViewer(items: List<RestaurantPhoto> = photos, modifier: Modifier = Modifier) {
        compose.setContent {
            RestaurantGuideTheme {
                var open by remember { mutableStateOf(true) }
                if (open) RestaurantPhotoViewer("Two Brothers Shawarma", items, 0, onClose = { open = false }, modifier = modifier)
                else Text("Gallery closed")
            }
        }
    }

    private fun waitForPhoto() {
        compose.waitUntil(10_000) {
            compose.onNodeWithTag("photo-zoom-viewport").fetchSemanticsNode().config
                .contains(SemanticsActions.SetProgress)
        }
    }

    private fun assertZoom(expected: Float) {
        val range = compose.onNodeWithTag("photo-zoom-viewport").fetchSemanticsNode().config[
            SemanticsProperties.ProgressBarRangeInfo,
        ]
        assertEquals(expected, range.current, .02f)
        compose.onNodeWithText("${(expected * 100).toInt()}%").assertIsDisplayed()
    }

    private fun panOffset(): Offset = compose.onNodeWithTag("photo-zoom-viewport")
        .fetchSemanticsNode().config[PhotoPanOffsetKey]

    private fun panLimit(): Offset = compose.onNodeWithTag("photo-zoom-viewport")
        .fetchSemanticsNode().config[PhotoPanLimitKey]

    private fun pinchOut() {
        compose.onNodeWithTag("photo-zoom-viewport").performTouchInput {
            pinch(
                start0 = Offset(width * .42f, center.y), end0 = Offset(width * .18f, center.y),
                start1 = Offset(width * .58f, center.y), end1 = Offset(width * .82f, center.y),
                durationMillis = 600,
            )
        }
    }

    private fun snapshot(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        android.os.SystemClock.sleep(200)
        val bitmap = instrumentation.uiAutomation.takeScreenshot() ?: return
        val folder = instrumentation.targetContext.filesDir.resolve("list-test-screens").apply { mkdirs() }
        folder.resolve("$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
}
