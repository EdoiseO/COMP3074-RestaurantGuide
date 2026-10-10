package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gbc.comp3074.p12.restaurantguide.R
import ca.gbc.comp3074.p12.restaurantguide.data.DemoRestaurants
import ca.gbc.comp3074.p12.restaurantguide.data.Restaurant
import ca.gbc.comp3074.p12.restaurantguide.data.filterRestaurants
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme

@Composable
fun RestaurantListScreen(
    restaurants: List<Restaurant>,
    query: String,
    selectedTags: Set<String>,
    onQueryChange: (String) -> Unit,
    onTagClick: (String) -> Unit,
    onAllClick: () -> Unit,
    onClearSearchAndFilters: () -> Unit,
    onRestaurantClick: (Restaurant) -> Unit,
    onAddClick: () -> Unit,
    onAboutClick: () -> Unit,
    listState: LazyListState,
    filtersState: LazyListState,
) {
    val visibleRestaurants = filterRestaurants(restaurants, query, selectedTags)
    val allTags = restaurants.flatMap { it.tags }.distinct()
    val categories = listOf("Shawarma", "Turkish", "Mexican").filter { it in allTags }
    val availableTags = categories + allTags.filterNot { it in categories }.sorted()
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    Scaffold(
        modifier = Modifier.imePadding(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(64.dp).testTag("add-restaurant"),
            ) { Icon(painterResource(R.drawable.ic_add), stringResource(R.string.list_add), Modifier.size(28.dp)) }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.app_name),
                    Modifier.weight(1f).semantics { heading() },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium,
                )
                TextButton(onClick = onAboutClick) { Text(stringResource(R.string.list_about)) }
            }
            OutlinedTextField(
                value = query, onValueChange = onQueryChange,
                label = { Text(stringResource(R.string.list_search_label)) },
                placeholder = { Text(stringResource(R.string.list_search_hint)) },
                trailingIcon = {
                    if (query.isNotEmpty()) IconButton(onClick = { onQueryChange("") }) {
                        Icon(painterResource(R.drawable.ic_close), stringResource(R.string.list_clear_search))
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("restaurant-search"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus(); keyboard?.hide() }),
            )
            LazyRow(
                state = filtersState,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("tag-filters"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(key = "reset-all") {
                    TagFilter(stringResource(R.string.list_all), selectedTags.isEmpty(), onAllClick, "filter-All")
                }
                items(availableTags, key = { "tag-" + it }) { tag ->
                    val label = if (tag.equals("All", ignoreCase = true)) stringResource(R.string.list_all_tag_label, tag) else tag
                    TagFilter(label, tag in selectedTags, { onTagClick(tag) }, "filter-tag-" + tag)
                }
            }
            val countText = if (query.isBlank() && selectedTags.isEmpty()) {
                pluralStringResource(R.plurals.list_saved_count, visibleRestaurants.size, visibleRestaurants.size)
            } else {
                pluralStringResource(R.plurals.list_result_count, visibleRestaurants.size, visibleRestaurants.size)
            }
            Text(
                countText, Modifier.padding(top = 8.dp, bottom = 16.dp).testTag("result-count"),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (visibleRestaurants.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp)).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        stringResource(if (restaurants.isEmpty()) R.string.empty_title else R.string.list_no_results),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(stringResource(if (restaurants.isEmpty()) R.string.list_empty_hint else R.string.list_no_results_hint))
                    TextButton(onClick = if (restaurants.isEmpty()) onAddClick else onClearSearchAndFilters) {
                        Text(stringResource(if (restaurants.isEmpty()) R.string.list_add else R.string.list_clear_filters))
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f).testTag("restaurant-list"),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(visibleRestaurants, key = { it.id }) { restaurant ->
                        RestaurantCard(restaurant, onClick = { onRestaurantClick(restaurant) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TagFilter(label: String, selected: Boolean, onClick: () -> Unit, testId: String) {
    FilterChip(
        selected = selected, onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) { { Icon(painterResource(R.drawable.ic_check), null, Modifier.size(18.dp)) } } else null,
        shape = RoundedCornerShape(10.dp),
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.testTag(testId),
    )
}

@Composable
private fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
    Card(
        onClick = onClick, modifier = Modifier.fillMaxWidth().testTag("restaurant-card-" + restaurant.id),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0xFFCAC4D0)),
    ) {
        if (restaurant.photoRes != null) {
            Image(
                painterResource(restaurant.photoRes), stringResource(R.string.list_photo, restaurant.name),
                Modifier.fillMaxWidth().height(136.dp), contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                Modifier.fillMaxWidth().height(136.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(painterResource(R.drawable.ic_photo), null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.list_photo_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    restaurant.name, Modifier.weight(1f), color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp), fontWeight = FontWeight.Bold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(restaurant.rating.toString() + ".0", style = MaterialTheme.typography.titleMedium)
                    Text("★", color = Color(0xFFFFB300), fontSize = 18.sp)
                }
            }
            Text(
                restaurant.listSummary,
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 940)
@Composable
private fun RestaurantListPreview() {
    RestaurantGuideTheme {
        RestaurantListScreen(DemoRestaurants.restaurants, "", emptySet(), {}, {}, {}, {}, {}, {}, {}, rememberLazyListState(), rememberLazyListState())
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyRestaurantListPreview() { RestaurantGuideTheme { RestaurantListFlow(emptyList()) } }
