package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ca.gbc.comp3074.p12.restaurantguide.R
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import kotlinx.coroutines.launch

private val TagsSaver = listSaver<List<String>, String>(save = { it }, restore = { it })

@Composable
fun RestaurantListFlow() {
    val context = androidx.compose.ui.platform.LocalContext.current.applicationContext
    val owner: RestaurantViewModel = viewModel(factory = androidx.lifecycle.viewmodel.viewModelFactory {
        initializer { RestaurantViewModel(ca.gbc.comp3074.p12.restaurantguide.data.RestaurantRepository(
            ca.gbc.comp3074.p12.restaurantguide.data.AppDatabase.getDatabase(context).restaurantDao())) }
    })
    RestaurantListFlow(owner)
}

@Composable
fun RestaurantListFlow(viewModel: RestaurantViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val restaurants = state.restaurants
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTags by rememberSaveable(stateSaver = TagsSaver) { mutableStateOf(emptyList<String>()) }
    var route by rememberSaveable { mutableStateOf("list") }
    var selectedId by rememberSaveable { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val filtersState = rememberLazyListState()
    val detailsScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val navigate: (String) -> Unit = { destination ->
        focusManager.clearFocus()
        keyboard?.hide()
        viewModel.dismissWriteError()
        route = destination
    }
    val selectedRestaurant = restaurants.firstOrNull { it.id == selectedId }

    val write: (RestaurantWrite, Restaurant) -> Unit = { kind, record ->
        focusManager.clearFocus()
        keyboard?.hide()
        viewModel.write(kind, record)
    }
    LaunchedEffect(state.completed) {
        when (state.completed) {
            RestaurantWrite.ADD -> {
                query = ""; selectedTags = emptyList(); navigate("list")
                listState.scrollToItem(0)
            }
            RestaurantWrite.EDIT -> navigate("details")
            RestaurantWrite.DELETE -> navigate("list")
            else -> Unit
        }
        if (state.completed != null) viewModel.acknowledgeWrite()
    }
    BackHandler(enabled = route != "list") {
        if (!state.saving) navigate(if (route in listOf("edit", "map", "email", "share")) "details" else "list")
    }
    val error = when (state.writeError) {
        RestaurantWriteError.FAILED -> stringResource(R.string.storage_write_failed)
        RestaurantWriteError.MISSING -> stringResource(R.string.storage_record_missing)
        null -> null
    }
    if (state.loading || (state.loadFailed && route == "list")) {
        StorageStatus(loading = state.loading, onRetry = viewModel::reload)
        return
    }
    if (route in listOf("details", "edit", "map", "email", "share") && selectedRestaurant == null) {
        if (state.saving || state.completed != null) StorageStatus(loading = true, onRetry = {})
        else Column(Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp), verticalArrangement = Arrangement.Center) {
            Text(stringResource(R.string.storage_record_missing))
            TextButton(onClick = { navigate("list") }, modifier = Modifier.testTag("missing-record-back")) {
                Text(stringResource(R.string.destination_back))
            }
        }
        return
    }
    when (route) {
        "details" -> selectedRestaurant?.let {
            RestaurantDetailsScreen(
                it, detailsScrollState, onBack = { navigate("list") },
                onEdit = { navigate("edit") },
                onDelete = {
                    write(RestaurantWrite.DELETE, it)
                },
                onUpdate = { changed -> write(RestaurantWrite.DETAILS, changed) },
                saving = state.saving, saveError = error, saveRevision = state.revision,
                onClearSaveError = viewModel::dismissWriteError,
                onMap = { navigate("map") }, onShare = { navigate("share") },
            )
        }
        "edit" -> selectedRestaurant?.let {
            RestaurantEditEntry(it, onBack = { navigate("details") }, onSave = { changed ->
                write(RestaurantWrite.EDIT, changed)
            }, saving = state.saving, saveError = error)
        }
        "map" -> selectedRestaurant?.let {
            RestaurantMapEntry(it, onBack = { navigate("details") })
        }
        "email", "share" -> selectedRestaurant?.let {
            RestaurantShareEntry(it, onBack = { navigate("details") })
        }
        "add" -> AddRestaurantEntry(
            onBack = { navigate("list") },
            onSave = { draft ->
                write(RestaurantWrite.ADD, draft)
            },
            saving = state.saving, saveError = error,
        )
        "about" -> AboutEntry(onBack = { navigate("list") })
        else -> RestaurantListScreen(
            restaurants = restaurants,
            query = query,
            selectedTags = selectedTags.toSet(),
            onQueryChange = { query = it; scope.launch { listState.scrollToItem(0) } },
            onTagClick = { tag ->
                selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag
                scope.launch { listState.scrollToItem(0) }
            },
            onAllClick = { selectedTags = emptyList(); scope.launch { listState.scrollToItem(0) } },
            onClearSearchAndFilters = { query = ""; selectedTags = emptyList() },
            onRestaurantClick = {
                selectedId = it.id
                scope.launch { detailsScrollState.scrollTo(0) }
                navigate("details")
            },
            onAddClick = { navigate("add") },
            onAboutClick = { navigate("about") },
            listState = listState,
            filtersState = filtersState,
        )
    }
    // Prevent additional edits or Back while the captured draft is being committed.
    // A separate window also blocks input to a Notes/Photo dialog that is already open.
    if (state.saving) AppDialog(
        title = stringResource(R.string.storage_saving),
        onDismissRequest = {},
        content = { CircularProgressIndicator(Modifier.testTag("storage-saving")) },
        actions = {},
    )
}
@Composable
private fun StorageStatus(loading: Boolean, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp).testTag("storage-status"),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        if (loading) CircularProgressIndicator()
        Text(stringResource(if (loading) R.string.storage_loading else R.string.storage_load_failed))
        if (!loading) TextButton(onClick = onRetry, modifier = Modifier.testTag("storage-retry")) {
            Text(stringResource(R.string.storage_retry))
        }
    }
}
