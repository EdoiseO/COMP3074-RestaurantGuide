package ca.gbc.comp3074.p12.restaurantguide.ui

import android.accessibilityservice.AccessibilityService
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import ca.gbc.comp3074.p12.restaurantguide.R
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

/** Direct URI validation tests are deliberately separate from the real external picker test. */
class PhotoSelectionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun readableMediaStorePhotoCanSaveItsCaption() = withImageFixture { uri, _ ->
        val seed = Restaurant(
            id = 41, name = "Fixture restaurant", description = "Shawarma plates and falafel.",
            address = "123 Test St, Toronto, ON M5V 2T6", phone = "416-555-0147\n437-555-0101",
            tags = listOf("Shawarma", "Halal"), rating = 4.5f, notes = "First visit.",
            listSummary = "Shawarma • Halal", coverUri = uri.toString(),
            galleryRes = listOf(R.drawable.two_brothers_chicken),
            photoUris = listOf(uri.toString()), photoCaptions = listOf("Original caption."),
        )
        var saved: Restaurant? = null
        compose.setContent {
            RestaurantGuideTheme {
                RestaurantEditEntry(seed, onBack = {}, onSave = { saved = it })
            }
        }
        compose.onNodeWithTag("edit-uri-photo-0").performScrollTo()
        // Thumbnail loading changes its image description. Tap the tile away
        // from its top-right Remove target, just as a user edits the caption.
        compose.onNodeWithTag("edit-uri-photo-0").performTouchInput {
            click(Offset(width * .25f, height * .75f))
        }
        awaitSaveReady()
        compose.onNodeWithTag("visit-photo-caption").performTextReplacement("  Extra pickles — order again.  ")
        compose.onNodeWithTag("visit-photo-save").performClick()
        closeSoftKeyboard()
        compose.onNodeWithTag("edit-save").performClick()
        compose.runOnIdle {
            assertEquals("Only the edited caption should change when the complete form is saved",
                seed.copy(photoCaptions = listOf("Extra pickles — order again.")), saved)
        }
    }

    @Test fun unreadableUriCannotSaveAndCanBeReselected() {
        var saved = false
        compose.setContent {
            RestaurantGuideTheme {
                VisitPhotoPicker(onDismiss = {}, onSave = { _, _ -> saved = true },
                    initialUri = "content://unavailable-provider/photo-selection-test")
            }
        }
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("visit-photo-error").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Photo unavailable").assertIsDisplayed()
        compose.onNodeWithTag("visit-photo-save").assertIsNotEnabled()
        compose.onNodeWithTag("visit-photo-choose").assertIsEnabled()
        compose.runOnIdle { assertEquals(false, saved) }
    }

    @Test fun longCaptionCanBeScrolledAndReadWithoutEllipsis() {
        val caption = (1..60).joinToString("\n") { "Dish $it: extra pickles; order this again." }
        compose.setContent {
            RestaurantGuideTheme {
                RestaurantPhotoViewer("Visit", listOf(RestaurantPhoto(resourceId = R.drawable.two_brothers_meal, caption = caption)), 0, {})
            }
        }
        compose.onNodeWithTag("photo-viewer-caption").assertTextEquals(caption)
        val scroll = compose.onNodeWithTag("photo-viewer-caption").fetchSemanticsNode()
            .config[SemanticsProperties.VerticalScrollAxisRange]
        assertTrue("Long caption must expose a scroll range", scroll.maxValue() > 0f)
        compose.onNodeWithTag("photo-viewer-caption").performTouchInput { swipeUp() }
        compose.waitUntil(5_000) {
            compose.onNodeWithTag("photo-viewer-caption").fetchSemanticsNode()
                .config[SemanticsProperties.VerticalScrollAxisRange].value() > 0f
        }
        compose.onNodeWithText("Use arrows or choose a photo").assertIsDisplayed()
    }

    @Test fun realSystemPickerReselectsPhotoThenSaveAndRestorationKeepCaption() = withImageFixture { fixtureUri, displayName ->
        val restoration = StateRestorationTester(compose)
        var returnedUri: String? = null
        restoration.setContent {
            RestaurantGuideTheme {
                var open by rememberSaveable { mutableStateOf(true) }
                var selected by rememberSaveable { mutableStateOf("") }
                var caption by rememberSaveable { mutableStateOf("") }
                if (open) {
                    VisitPhotoPicker(
                        initialUri = "content://unavailable-provider/reselect-test",
                        onDismiss = { open = false },
                        onSave = { photo, text -> selected = photo; caption = text; returnedUri = photo; open = false },
                    )
                } else if (selected.isNotEmpty()) {
                    RestaurantPhotoViewer("Picker fixture", listOf(RestaurantPhoto(uri = selected, caption = caption)), 0, {})
                }
            }
        }
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("visit-photo-error").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("visit-photo-save").assertIsNotEnabled()
        compose.onNodeWithTag("visit-photo-choose").performClick()
        selectFixtureInSystemPicker(displayName)
        awaitSaveReady()
        compose.onNodeWithTag("visit-photo-error").assertDoesNotExist()
        compose.onNodeWithTag("visit-photo-caption").performTextInput("A real picker photo — keep this.")
        compose.onNodeWithTag("visit-photo-save").performClick()
        compose.onNodeWithText("A real picker photo — keep this.").assertIsDisplayed()
        assertNotNull(returnedUri)
        val resolver = compose.activity.contentResolver
        // The system picker can wrap/rename a MediaStore item (for example 46.png).
        // Prove image identity using the unmodified bytes, rather than its display name.
        val sourceBytes = resolver.openInputStream(fixtureUri)!!.use { it.readBytes() }
        val returnedBytes = resolver.openInputStream(Uri.parse(returnedUri))!!.use { it.readBytes() }
        val sourceHash = sha256(sourceBytes)
        val returnedHash = sha256(returnedBytes)
        val proof = JSONObject().put("sourceUri", fixtureUri.toString()).put("returnedUri", returnedUri)
            .put("sourceBytes", sourceBytes.size).put("returnedBytes", returnedBytes.size)
            .put("sourceSha256", sourceHash).put("returnedSha256", returnedHash)
        InstrumentationRegistry.getInstrumentation().targetContext.filesDir.resolve("list-test-screens")
            .apply { mkdirs() }.resolve("photo-picker-byte-proof.json").writeText(proof.toString(2))
        assertEquals("The external picker must return the fixture image's bytes", sourceBytes.size, returnedBytes.size)
        assertEquals("The external picker must return the fixture image's SHA-256", sourceHash, returnedHash)
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("A real picker photo — keep this.").assertIsDisplayed()
        compose.onNodeWithText("1 / 1 · My photos").assertIsDisplayed()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Loading photo").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("Photo unavailable").assertDoesNotExist()
        snapshot("photo-picker-success-gallery")
    }

    private fun awaitSaveReady() {
        compose.waitUntil(10_000) {
            !compose.onNodeWithTag("visit-photo-save").fetchSemanticsNode().config.contains(SemanticsProperties.Disabled)
        }
        compose.onNodeWithTag("visit-photo-save").assertIsEnabled()
    }

    private fun withImageFixture(block: (Uri, String) -> Unit) {
        assumeTrue("The scoped MediaStore fixture requires API 29+", Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        val resolver = InstrumentationRegistry.getInstrumentation().targetContext.contentResolver
        val name = "restaurant-guide-picker-${UUID.randomUUID()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/RestaurantGuideTests")
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        try {
            val bitmap = Bitmap.createBitmap(40, 30, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.CYAN) }
            try {
                resolver.openOutputStream(uri)!!.use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            } finally { bitmap.recycle() }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            block(uri, name)
        } finally { resolver.delete(uri, null, null) }
    }

    /** No picker result injection: click a visible item in Android's own external picker. */
    private fun selectFixtureInSystemPicker(displayName: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val deadline = SystemClock.uptimeMillis() + 15_000
        var selected = false
        var capturedSelection = false
        var lastTree = "No active accessibility window"
        while (SystemClock.uptimeMillis() < deadline) {
            val root = automation.rootInActiveWindow
            if (root != null) {
                val nodes = descendants(root)
                lastTree = nodes.joinToString("\n") { "${it.packageName} ${it.className} ${it.viewIdResourceName} ${it.text} ${it.contentDescription}" }
                val external = root.packageName?.toString() != compose.activity.packageName
                if (selected && !external) return
                if (external) {
                    if (!selected) {
                        // Single-select pickers describe images by capture date rather than filename.
                        // The new fixture is the most recent item; verify its returned bytes above.
                        val candidate = nodes.firstOrNull {
                            it.isVisibleToUser && (it.text?.contains(displayName) == true || it.contentDescription?.contains(displayName) == true)
                        } ?: nodes.firstOrNull {
                            it.isVisibleToUser && it.contentDescription?.toString()
                                ?.matches(Regex("(?is)^(photo|image)( taken|,| from| on).*")) == true
                        }
                        candidate?.let { item ->
                            selected = clickableAncestor(item)?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
                        }
                    } else {
                        val confirmation = nodes.firstOrNull {
                            it.isVisibleToUser && it.text?.toString()
                                ?.matches(Regex("(?i)^(add|done|select|open)$")) == true
                        }
                        confirmation?.let {
                            if (!capturedSelection) {
                                snapshot("photo-picker-selected", lastTree)
                                capturedSelection = true
                            }
                            clickableAncestor(it)?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                    }
                }
            }
            SystemClock.sleep(100)
        }
        automation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        throw AssertionError("Could not complete the actual Photo Picker selection. Last visible tree:\n${lastTree.take(8_000)}")
    }

    private fun descendants(node: AccessibilityNodeInfo): List<AccessibilityNodeInfo> = buildList {
        add(node)
        for (index in 0 until node.childCount) node.getChild(index)?.let { addAll(descendants(it)) }
    }

    private fun clickableAncestor(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node
        repeat(7) {
            if (current?.isClickable == true) return current
            current = current?.parent
        }
        return null
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes)
        .joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }

    private fun snapshot(name: String, tree: String? = null) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val folder = instrumentation.targetContext.filesDir.resolve("list-test-screens").apply { mkdirs() }
        tree?.let { folder.resolve("$name.txt").writeText(it) }
        val bitmap = instrumentation.uiAutomation.takeScreenshot() ?: return
        try {
            folder.resolve("$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } finally { bitmap.recycle() }
    }
}
