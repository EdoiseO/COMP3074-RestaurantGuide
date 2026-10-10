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
import kotlinx.coroutines.launch

private val RestaurantsSaver = listSaver<List<Restaurant>, Any>(
    save = { records ->
        listOf("restaurants-v2") + records.flatMap { r ->
            listOf(r.id, r.name, r.description, r.address, r.phone, ArrayList(r.tags), r.rating,
                r.photoRes ?: 0, r.listSummary, r.notes, ArrayList(r.galleryRes), r.mapRes ?: 0,
                ArrayList(r.photoUris), ArrayList(r.photoCaptions), r.fullMapRes ?: 0)
        }
    },
    restore = { values ->
        val extended = values.firstOrNull() == "restaurants-v2"
        val fields = if (extended) values.drop(1) else values
        fields.chunked(if (extended) 15 else 9).map { v ->
            Restaurant(
                id = v[0] as Int, name = v[1] as String, description = v[2] as String,
                address = v[3] as String, phone = v[4] as String,
                tags = (v[5] as List<*>).map { it as String }, rating = v[6] as Int,
                photoRes = (v[7] as Int).takeIf { it != 0 },
                listSummary = v[8] as String,
                notes = if (extended) v[9] as String else "",
                galleryRes = if (extended) (v[10] as List<*>).map { it as Int } else emptyList(),
                mapRes = if (extended) (v[11] as Int).takeIf { it != 0 } else null,
                photoUris = if (extended) (v[12] as List<*>).map { it as String } else emptyList(),
                photoCaptions = if (extended) (v[13] as List<*>).map { it as String } else emptyList(),
                fullMapRes = if (extended) (v[14] as Int).takeIf { it != 0 } else null,
            )
        }
    },
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
            onSave = { name, description, tags, rating ->
                restaurants = listOf(
                    Restaurant(
                        id = (restaurants.maxOfOrNull { it.id } ?: 0) + 1,
                        name = name, description = description, address = "", phone = "",
                        tags = tags, rating = rating,
                    ),
                ) + restaurants
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
