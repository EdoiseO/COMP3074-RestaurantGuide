package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
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
        records.flatMap { r ->
            listOf(r.id, r.name, r.description, r.address, r.phone, ArrayList(r.tags), r.rating, r.photoRes ?: 0, r.listSummary)
        }
    },
    restore = { values ->
        values.chunked(9).map { v ->
            Restaurant(
                id = v[0] as Int, name = v[1] as String, description = v[2] as String,
                address = v[3] as String, phone = v[4] as String,
                tags = (v[5] as List<*>).map { it as String }, rating = v[6] as Int,
                photoRes = (v[7] as Int).takeIf { it != 0 },
                listSummary = v[8] as String,
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
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val navigate: (String) -> Unit = { destination ->
        focusManager.clearFocus()
        keyboard?.hide()
        route = destination
    }
    BackHandler(enabled = route != "list") { navigate("list") }
    when (route) {
        "details" -> restaurants.firstOrNull { it.id == selectedId }?.let {
            RestaurantDetailsEntry(it, onBack = { navigate("list") })
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
            onRestaurantClick = { selectedId = it.id; navigate("details") },
            onAddClick = { navigate("add") },
            onAboutClick = { navigate("about") },
            listState = listState,
            filtersState = filtersState,
        )
    }
}
