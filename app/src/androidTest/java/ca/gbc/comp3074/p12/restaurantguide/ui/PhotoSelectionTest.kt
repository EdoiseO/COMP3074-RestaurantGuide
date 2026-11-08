package ca.gbc.comp3074.p12.restaurantguide.ui

import android.accessibilityservice.AccessibilityService
import android.app.UiAutomation
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.os.SystemClock
import android.provider.MediaStore
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import ca.gbc.comp3074.p12.restaurantguide.data.*
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
import androidx.compose.ui.test.onNodeWithContentDescription
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
import org.junit.Assert.assertNotEquals
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

    @Test fun readablePhotoWithoutAPersistableGrantSavesWithWarning() = withImageFixture { uri, _ ->
        val store = FakeRestaurantStore()
        val owner = RestaurantViewModel(store, PhotoWriteCoordinator(AndroidPhotoGrants(compose.activity.contentResolver)))
        compose.setContent { RestaurantGuideTheme { RestaurantListFlow(owner) } }
        val record = Restaurant(0, "Session cafe", "", "", "", emptyList(), 0f,
            photoUris = listOf(uri.toString()), photoCaptions = listOf("Keep this caption"))
        compose.runOnIdle { owner.write(RestaurantWrite.ADD, record) }
        compose.waitUntil(10_000) { compose.onAllNodesWithTag("photo-session-warning").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("photo-session-ok").performClick()
        compose.runOnIdle {
            assertEquals(listOf("Keep this caption"), store.allRestaurants.value.single().photoCaptions)
            owner.viewModelScope.cancel()
        }
    }

    @Test fun cancelledPickerAndFormLeaveTheSavedAlbumAndGrantsAlone() = withImageFixture { _, displayName ->
        val resolver = compose.activity.contentResolver
        val originalGrants = resolver.persistedUriPermissions.map { it.uri }.toSet()
        val seed = Restaurant(
            id = 41, name = "Fixture restaurant", description = "", address = "", phone = "",
            tags = emptyList(), rating = 4.5f, galleryRes = listOf(R.drawable.two_brothers_chicken),
        )
        compose.setContent {
            RestaurantGuideTheme { TestRestaurantListFlow(listOf(seed)) }
        }
        compose.onNodeWithTag("restaurant-card-41").performClick()
        compose.onNodeWithTag("details-edit").performClick()
        compose.onNodeWithTag("edit-add-photo").performScrollTo().performClick()
        compose.onNodeWithTag("visit-photo-choose").performClick()
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        compose.waitUntil(10_000) {
            val activePackage = automation.rootInActiveWindow?.packageName?.toString()
            activePackage != null && activePackage != compose.activity.packageName
        }
        automation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        compose.waitUntil(10_000) {
            automation.rootInActiveWindow?.packageName?.toString() == compose.activity.packageName
        }
        compose.waitForIdle()
        compose.onNodeWithTag("visit-photo-save").assertIsNotEnabled()
        compose.onNodeWithTag("visit-photo-choose").performClick()
        selectFixtureInSystemPicker(displayName)
        awaitSaveReady()
        compose.onNodeWithTag("visit-photo-save").performClick()
        compose.onNodeWithTag("edit-uri-photo-0").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("edit-cancel").performClick()
        compose.onNodeWithTag("details-edit").performClick()
        compose.onNodeWithTag("edit-resource-photo-0").performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("edit-uri-photo-0").assertDoesNotExist()
        assertEquals(originalGrants, resolver.persistedUriPermissions.map { it.uri }.toSet())
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

    @Test fun realSystemPickerReselectsPhotoThenSaveAndRestorationKeepCaption() {
        val stageReload = InstrumentationRegistry.getArguments().getString("stagePhotoReload") == "true"
        if (stageReload) reloadProofFile().delete()
        withImageFixture(keepAfterSuccess = stageReload) { fixtureUri, displayName ->
            saveAndRestoreRealPickerPhoto(fixtureUri, displayName, stageReload)
        }
    }

    private fun saveAndRestoreRealPickerPhoto(fixtureUri: Uri, displayName: String, stageReload: Boolean) {
        val restoration = StateRestorationTester(compose)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "picker-reload-${UUID.randomUUID()}"
        val database = AppDatabase.builder(context, databaseName).build()
        val store = RestaurantRepository(database.restaurantDao())
        val owner = RestaurantViewModel(store, PhotoWriteCoordinator(AndroidPhotoGrants(context.contentResolver)))
        var returnedUri: String? = null
        var savedId: Int? = null
        restoration.setContent {
            RestaurantGuideTheme {
                val state by owner.state.collectAsState()
                var open by rememberSaveable { mutableStateOf(true) }
                var selected by rememberSaveable { mutableStateOf("") }
                var caption by rememberSaveable { mutableStateOf("") }
                LaunchedEffect(state.completed, state.restaurants) {
                    val row = state.restaurants.firstOrNull { it.id == state.createdId }
                    if (state.completed == RestaurantWrite.ADD && row != null) {
                        selected = row.photoUris.single(); returnedUri = selected
                        caption = row.photoCaptions.single(); savedId = row.id
                        open = false; owner.acknowledgeWrite()
                    }
                }
                if (open) {
                    VisitPhotoPicker(
                        initialUri = "content://unavailable-provider/reselect-test",
                        saving = state.saving,
                        onDismiss = { open = false },
                        onSave = { photo, text -> owner.write(RestaurantWrite.ADD,
                            Restaurant(0, "Picker fixture", "", "", "", emptyList(), 0f,
                                photoUris = listOf(photo), photoCaptions = listOf(text))) },
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
        val resolver = compose.activity.contentResolver
        try {
            compose.waitUntil(10_000) { compose.runOnIdle { returnedUri != null } }
            val selectedUri = Uri.parse(returnedUri)
            assertTrue(resolver.persistedUriPermissions.any { it.uri == selectedUri && it.isReadPermission })
            // The picker may wrap the source in a different URI. Compare the unmodified bytes.
            val sourceBytes = resolver.openInputStream(fixtureUri)!!.use { it.readBytes() }
            val returnedBytes = resolver.openInputStream(selectedUri)!!.use { it.readBytes() }
            val sourceHash = sha256(sourceBytes)
            assertEquals(sourceBytes.size, returnedBytes.size)
            assertEquals(sourceHash, sha256(returnedBytes))
            restoration.emulateSavedInstanceStateRestore()
            assertPhotoDecoded()
            snapshot("photo-picker-success-gallery")
            if (stageReload) {
                val proof = JSONObject().put("sourceUri", fixtureUri.toString()).put("returnedUri", returnedUri)
                    .put("sourceBytes", sourceBytes.size).put("sourceSha256", sourceHash)
                    .put("pickerPid", Process.myPid()).put("databaseName", databaseName).put("restaurantId", savedId)
                reloadProofFile().writeText(proof.toString())
            }
        } finally {
            owner.viewModelScope.cancel()
            database.close()
            if (!stageReload || !reloadProofFile().isFile) {
                context.deleteDatabase(databaseName)
                returnedUri?.let { resolver.releasePersistableUriPermission(Uri.parse(it), Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            }
        }
    }

    // Run the real-picker test with stagePhotoReload=true, adb shell am force-stop the app,
    // then run this method with verifyPhotoReload=true (without opening the unrelated default database). Separate instrumentation runs are
    // required because force-stop terminates the target process and its running tests.
    @Test fun verifyRealPickerPhotoAfterForceStop() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("verifyPhotoReload") == "true")
        val proofFile = reloadProofFile()
        val proof = JSONObject(proofFile.readText())
        val resolver = compose.activity.contentResolver
        val selectedUri = Uri.parse(proof.getString("returnedUri"))
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = proof.getString("databaseName")
        val database = AppDatabase.builder(context, databaseName).build()
        try {
            val saved = runBlocking { RestaurantRepository(database.restaurantDao()).observeRestaurant(proof.getInt("restaurantId")).first() }!!
            assertEquals(listOf(selectedUri.toString()), saved.photoUris)
            assertEquals(listOf("A real picker photo — keep this."), saved.photoCaptions)
            assertNotEquals(proof.getInt("pickerPid"), Process.myPid())
            assertTrue(resolver.persistedUriPermissions.any { it.uri == selectedUri && it.isReadPermission })
            val reloadedBytes = resolver.openInputStream(selectedUri)!!.use { it.readBytes() }
            assertEquals(proof.getInt("sourceBytes"), reloadedBytes.size)
            assertEquals(proof.getString("sourceSha256"), sha256(reloadedBytes))
            compose.setContent {
                RestaurantGuideTheme {
                    RestaurantPhotoViewer(
                        "Picker fixture",
                        listOf(RestaurantPhoto(uri = selectedUri.toString(), caption = "A real picker photo — keep this.")),
                        0, {},
                    )
                }
            }
            assertPhotoDecoded()
        } finally {
            database.close()
            context.deleteDatabase(databaseName)
            if (resolver.persistedUriPermissions.any { it.uri == selectedUri && it.isReadPermission }) {
                resolver.releasePersistableUriPermission(selectedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            resolver.delete(Uri.parse(proof.getString("sourceUri")), null, null)
            proofFile.delete()
        }
    }

    private fun reloadProofFile() =
        InstrumentationRegistry.getInstrumentation().targetContext.filesDir.resolve("photo-reload-fixture.json")

    private fun assertPhotoDecoded() {
        compose.onNodeWithText("A real picker photo — keep this.").assertIsDisplayed()
        compose.onNodeWithText("1 / 1 · My photos").assertIsDisplayed()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Loading photo").fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithText("Photo unavailable").assertDoesNotExist()
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.photo_zoom_in)).assertIsEnabled()
    }

    private fun awaitSaveReady() {
        compose.waitUntil(10_000) {
            !compose.onNodeWithTag("visit-photo-save").fetchSemanticsNode().config.contains(SemanticsProperties.Disabled)
        }
        compose.onNodeWithTag("visit-photo-save").assertIsEnabled()
    }

    private fun withImageFixture(keepAfterSuccess: Boolean = false, block: (Uri, String) -> Unit) {
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
        var completed = false
        try {
            val hash = name.hashCode()
            val fixtureColor = Color.rgb((hash ushr 16) and 255, (hash ushr 8) and 255, hash and 255)
            val bitmap = Bitmap.createBitmap(40, 30, Bitmap.Config.ARGB_8888).apply { eraseColor(fixtureColor) }
            try {
                resolver.openOutputStream(uri)!!.use { assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
            } finally { bitmap.recycle() }
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
            block(uri, name)
            completed = true
        } finally {
            if (!keepAfterSuccess || !completed) resolver.delete(uri, null, null)
        }
    }

    /**
     * No picker result injection: the real external picker returns the fixture.
     *
     * API 33+ shows Android's Photo Picker, whose items are clickable and described by date.
     * Older versions fall back to DocumentsUI (ACTION_OPEN_DOCUMENT). There, file tiles are
     * not clickable views: activation goes through DocumentsUI's own gesture and key handlers,
     * so ACTION_CLICK does nothing, and the picker reopens on whichever root it used last
     * (Recent can be empty and Downloads never lists Pictures). For DocumentsUI the test
     * switches to device storage, searches for the exact fixture name, focuses the single
     * result (not its Preview button) and presses Enter, the documented keyboard activation.
     */
    private fun selectFixtureInSystemPicker(displayName: String) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val deadline = SystemClock.uptimeMillis() + 30_000
        var selected = false
        var capturedSelection = false
        var docs = DocsStep.ChooseRoot
        var stepTime = 0L
        var lastTree = "No active accessibility window"
        while (SystemClock.uptimeMillis() < deadline) {
            val root = automation.rootInActiveWindow
            if (root != null) {
                val nodes = descendants(root)
                lastTree = nodes.joinToString("\n") { "${it.packageName} ${it.className} ${it.viewIdResourceName} ${it.text} ${it.contentDescription}" }
                val packageName = root.packageName?.toString()
                val external = packageName != compose.activity.packageName
                if (selected && !external) return
                if (external && !selected && packageName == DOCUMENTS_UI) {
                    // Give each DocumentsUI step time to settle before acting on the next screen.
                    if (SystemClock.uptimeMillis() - stepTime > 700) {
                        val next = documentsUiStep(docs, nodes, displayName, automation)
                        if (next != docs) { docs = next; stepTime = SystemClock.uptimeMillis() }
                        if (docs == DocsStep.Activated) {
                            snapshot("photo-picker-selected", lastTree)
                            selected = true
                        }
                    }
                } else if (external) {
                    if (!selected) {
                        // Photo Picker describes images by capture date rather than filename.
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
        throw AssertionError("Could not complete the actual system picker selection (DocumentsUI step $docs). Last visible tree:\n${lastTree.take(8_000)}")
    }

    private enum class DocsStep { ChooseRoot, OpenSearch, EnterQuery, Activate, Activated }

    /** Performs at most one DocumentsUI action and returns the step that follows it. */
    private fun documentsUiStep(
        step: DocsStep, nodes: List<AccessibilityNodeInfo>, displayName: String, automation: UiAutomation,
    ): DocsStep {
        fun byId(id: String) = nodes.filter { it.isVisibleToUser && it.viewIdResourceName == "$DOCUMENTS_UI:id/$id" }
        // Root rows and list-view files use android:id/title; grid tiles use DocumentsUI's own id.
        fun isTitle(node: AccessibilityNodeInfo) =
            node.viewIdResourceName == "android:id/title" || node.viewIdResourceName == "$DOCUMENTS_UI:id/title"
        val drawerRoots = byId("roots_list").firstOrNull()
        return when (step) {
            DocsStep.ChooseRoot -> if (drawerRoots == null) {
                nodes.firstOrNull { it.isVisibleToUser && it.contentDescription?.toString() == "Show roots" }
                    ?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                step
            } else {
                // The device-storage root: every root except the virtual Recent/Downloads/media roots.
                val virtualRoots = setOf("Recent", "Downloads", "Images", "Videos", "Audio", "Documents")
                val storageTitle = descendants(drawerRoots).firstOrNull {
                    isTitle(it) && it.text?.toString() !in virtualRoots
                }
                val row = storageTitle?.let { rowInList(it, drawerRoots) }
                if (row != null && (row.performAction(AccessibilityNodeInfo.ACTION_CLICK) || tapCenter(row, automation))) {
                    DocsStep.OpenSearch
                } else step
            }
            DocsStep.OpenSearch -> when {
                drawerRoots != null -> step
                byId("search_src_text").isNotEmpty() -> DocsStep.EnterQuery
                else -> {
                    byId("option_menu_search").firstOrNull()?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    step
                }
            }
            DocsStep.EnterQuery -> {
                val field = byId("search_src_text").firstOrNull() ?: return DocsStep.OpenSearch
                val args = Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, displayName)
                }
                if (field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) {
                    pressKey(automation, KeyEvent.KEYCODE_ENTER)
                    DocsStep.Activate
                } else step
            }
            DocsStep.Activate -> {
                // Search results only: exactly one tile for the fixture. Image grid tiles have no
                // id but are described as "<name>, <size>, <time>"; folder/list rows use item_root.
                val described = nodes.filter {
                    it.isVisibleToUser && it.contentDescription?.toString()?.startsWith("$displayName,") == true
                }
                val tiles = described.ifEmpty { byId("item_root") }
                val match = tiles.singleOrNull()?.takeIf { tile ->
                    tile in described || descendants(tile).any { isTitle(it) && it.text?.toString() == displayName }
                }
                // DocumentsUI focuses the single result after a search; ACTION_FOCUS returns false
                // for a node that already has focus, so accept either.
                if (match != null && (match.isFocused || match.performAction(AccessibilityNodeInfo.ACTION_FOCUS))) {
                    SystemClock.sleep(200)
                    pressKey(automation, KeyEvent.KEYCODE_ENTER)
                    DocsStep.Activated
                } else step
            }
            DocsStep.Activated -> step
        }
    }

    /** The direct child of [list] that contains [node] (a ListView row). */
    private fun rowInList(node: AccessibilityNodeInfo, list: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var current: AccessibilityNodeInfo? = node
        repeat(8) {
            val parent = current?.parent ?: return null
            if (parent == list) return current
            current = parent
        }
        return null
    }

    /** A real touch on a drawer row, used only if the row rejects ACTION_CLICK. */
    private fun tapCenter(node: AccessibilityNodeInfo, automation: UiAutomation): Boolean {
        val bounds = Rect().also(node::getBoundsInScreen)
        val time = SystemClock.uptimeMillis()
        val x = bounds.exactCenterX(); val y = bounds.exactCenterY()
        val down = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, x, y, 0).apply { source = InputDevice.SOURCE_TOUCHSCREEN }
        val up = MotionEvent.obtain(time, time + 60, MotionEvent.ACTION_UP, x, y, 0).apply { source = InputDevice.SOURCE_TOUCHSCREEN }
        return try {
            automation.injectInputEvent(down, true) && automation.injectInputEvent(up, true)
        } finally { down.recycle(); up.recycle() }
    }

    private fun pressKey(automation: UiAutomation, keyCode: Int) {
        val time = SystemClock.uptimeMillis()
        automation.injectInputEvent(KeyEvent(time, time, KeyEvent.ACTION_DOWN, keyCode, 0), true)
        automation.injectInputEvent(KeyEvent(time, time, KeyEvent.ACTION_UP, keyCode, 0), true)
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

    private companion object { const val DOCUMENTS_UI = "com.android.documentsui" }

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
