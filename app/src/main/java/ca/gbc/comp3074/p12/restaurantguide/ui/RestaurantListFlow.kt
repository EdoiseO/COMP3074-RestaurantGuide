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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.gbc.comp3074.p12.restaurantguide.data.DemoRestaurants
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import kotlinx.coroutines.launch

private val TagsSaver = listSaver<List<String>, String>(save = { it }, restore = { it })

@Composable
fun RestaurantListFlow(
    initialRestaurants: List<Restaurant> = DemoRestaurants.restaurants,
    viewModel: RestaurantViewModel = viewModel()
) {
    // Collect persistent restaurants from Room database via ViewModel
    val restaurants by viewModel.restaurants.collectAsState()

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
        route = destination
    }
    val selectedRestaurant = restaurants.firstOrNull { it.id == selectedId }

    // Delegate updates directly to the Room database ViewModel
    val updateRestaurant: (Restaurant) -> Unit = { changed ->
        viewModel.updateRestaurant(changed)
    }

    BackHandler(enabled = route != "list") {
        navigate(if (route in listOf("edit", "map", "email", "share")) "details" else "list")
    }
    when (route) {
        "details" -> selectedRestaurant?.let {
            RestaurantDetailsScreen(
                it, detailsScrollState, onBack = { navigate("list") },
                onEdit = { navigate("edit") },
                onDelete = {
                    viewModel.deleteRestaurant(it) // Permanently delete from Room database
                    navigate("list")
                },
                onUpdate = updateRestaurant,
                onMap = { navigate("map") }, onShare = { navigate("share") },
            )
        }
        "edit" -> selectedRestaurant?.let {
            RestaurantEditEntry(it, onBack = { navigate("details") }, onSave = { changed ->
                updateRestaurant(changed)
                navigate("details")
            })
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
                // id = 0 lets Room auto-generate the primary key uniquely
                val created = draft.copy(id = 0)
                viewModel.addRestaurant(created) // Persist new restaurant into Room database
                query = ""
                selectedTags = emptyList()
                navigate("list")
                scope.launch { listState.scrollToItem(0) }
            },
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
}