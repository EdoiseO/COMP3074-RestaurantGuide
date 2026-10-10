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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import ca.gbc.comp3074.p12.restaurantguide.data.DemoRestaurants
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import ca.gbc.comp3074.p12.restaurantguide.data.restoreRestaurantState
import ca.gbc.comp3074.p12.restaurantguide.data.saveRestaurantState
import kotlinx.coroutines.launch

private val RestaurantsSaver = listSaver<List<Restaurant>, Any>(
    save = { saveRestaurantState(it) },
    restore = ::restoreRestaurantState,
)

private val TagsSaver = listSaver<List<String>, String>(save = { it }, restore = { it })

@Composable
fun RestaurantListFlow(initialRestaurants: List<Restaurant> = DemoRestaurants.restaurants) {
    var restaurants by rememberSaveable(stateSaver = RestaurantsSaver) { mutableStateOf(initialRestaurants) }
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
    val updateRestaurant: (Restaurant) -> Unit = { changed ->
        restaurants = restaurants.map { if (it.id == changed.id) changed else it }
    }
    BackHandler(enabled = route != "list") {
        navigate(if (route in listOf("edit", "map", "email")) "details" else "list")
    }
    when (route) {
        "details" -> selectedRestaurant?.let {
            RestaurantDetailsScreen(
                it, detailsScrollState, onBack = { navigate("list") },
                onEdit = { navigate("edit") },
                onDelete = {
                    restaurants = restaurants.filterNot { record -> record.id == selectedId }
                    navigate("list")
                },
                onUpdate = updateRestaurant,
                onMap = { navigate("map") }, onEmail = { navigate("email") },
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
        "email" -> selectedRestaurant?.let {
            RestaurantEmailEntry(it, onBack = { navigate("details") })
        }
        "add" -> AddRestaurantEntry(
            onBack = { navigate("list") },
            onSave = { draft ->
                val created = draft.copy(id = (restaurants.maxOfOrNull { it.id } ?: 0) + 1)
                restaurants = listOf(created) + restaurants
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
