package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.contentDescription
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
import ca.gbc.comp3074.p12.restaurantguide.data.formatRating
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTheme
import ca.gbc.comp3074.p12.restaurantguide.ui.theme.RestaurantGuideTokens

private val ListHeadingColor = Color(0xFF4F378A)
private val ListTextColor = Color(0xFF49454F)
private val FilterTextColor = Color(0xFF4A4459)
private val SelectedFilterColor = Color(0xFFE8DEF8)
private val ListBorderColor = Color(0xFFCAC4D0)

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
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
    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = RestaurantGuideTokens.homeBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                modifier = Modifier.size(72.dp).testTag("add-restaurant"),
            ) { Icon(painterResource(R.drawable.ic_add), stringResource(R.string.list_add), Modifier.size(28.dp)) }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).padding(horizontal = 20.dp).padding(top = 16.dp)) {
            FlowRow(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.app_name),
                    Modifier.semantics { heading() },
                    color = RestaurantGuideTokens.ink,
                    fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        pluralStringResource(R.plurals.list_saved_count, visibleRestaurants.size, visibleRestaurants.size),
                        Modifier.testTag("result-count"), color = ListHeadingColor,
                        fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium,
                    )
                    IconButton(onClick = onAboutClick, modifier = Modifier.testTag("list-about")) {
                        Icon(painterResource(R.drawable.ic_info), stringResource(R.string.list_about),
                            Modifier.size(20.dp), tint = ListHeadingColor)
                    }
                }
            }
            RestaurantSearchField(query, onQueryChange)
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
            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
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
                    TextButton(
                        onClick = if (restaurants.isEmpty()) onAddClick else onClearSearchAndFilters,
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                    ) {
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
private fun RestaurantSearchField(query: String, onQueryChange: (String) -> Unit) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val label = stringResource(R.string.list_search_label)
    val primary = MaterialTheme.colorScheme.primary
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, RestaurantGuideTokens.searchBorder, RoundedCornerShape(16.dp))
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_search), null, Modifier.size(24.dp), tint = ListTextColor)
        BasicTextField(
            value = query, onValueChange = onQueryChange,
            modifier = Modifier.weight(1f).padding(start = 12.dp, top = 16.dp, bottom = 16.dp)
                .testTag("restaurant-search").semantics { contentDescription = label },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = ListTextColor),
            cursorBrush = SolidColor(primary), singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus(); keyboard?.hide() }),
            decorationBox = { innerTextField ->
                Box {
                    if (query.isEmpty()) Text(stringResource(R.string.list_search_hint),
                        color = ListTextColor, style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    innerTextField()
                }
            },
        )
        IconButton(onClick = { onQueryChange("") }, modifier = Modifier.testTag("clear-search")) {
            Icon(painterResource(R.drawable.ic_cancel_outline), stringResource(R.string.list_clear_search), tint = ListTextColor)
        }
    }
}

@Composable
private fun TagFilter(label: String, selected: Boolean, onClick: () -> Unit, testId: String) {
    FilterChip(
        selected = selected, onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) { { Icon(painterResource(R.drawable.ic_check), null, Modifier.size(18.dp)) } } else null,
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = RestaurantGuideTokens.homeBackground,
            labelColor = FilterTextColor,
            selectedContainerColor = SelectedFilterColor,
            selectedLabelColor = FilterTextColor,
            selectedLeadingIconColor = FilterTextColor,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true, selected = selected,
            borderColor = ListBorderColor, selectedBorderColor = Color.Transparent,
        ),
        modifier = Modifier.testTag(testId),
    )
}

@Composable
private fun RestaurantCard(restaurant: Restaurant, onClick: () -> Unit) {
    Card(
        onClick = onClick, modifier = Modifier.fillMaxWidth().testTag("restaurant-card-" + restaurant.id),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, ListBorderColor),
    ) {
        if (restaurant.coverUri.isNotBlank() || restaurant.photoRes != null) {
            RestaurantPhotoImage(
                photo = if (restaurant.coverUri.isNotBlank()) {
                    RestaurantPhoto(uri = restaurant.coverUri)
                } else {
                    RestaurantPhoto(resourceId = restaurant.photoRes)
                },
                contentDescription = stringResource(R.string.list_photo, restaurant.name),
                modifier = Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Crop,
                alignment = when {
                    restaurant.coverUri.isNotBlank() -> Alignment.Center
                    restaurant.photoRes == R.drawable.midyeci_riza -> BiasAlignment(0f, -0.5f)
                    restaurant.photoRes == R.drawable.la_unica -> BiasAlignment(0f, -0.35f)
                    else -> Alignment.Center
                },
            )
        } else {
            Box(
                Modifier.fillMaxWidth().height(160.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(painterResource(R.drawable.ic_photo), null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.list_photo_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    restaurant.name, Modifier.weight(1f), color = RestaurantGuideTokens.ink,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp), fontWeight = FontWeight.Bold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("★", color = RestaurantGuideTokens.star, fontSize = 18.sp)
                    Text(formatRating(restaurant.rating), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = RestaurantGuideTokens.ink)
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
private fun EmptyRestaurantListPreview() { RestaurantGuideTheme { RestaurantListScreen(emptyList(), "", emptySet(), {}, {}, {}, {}, {}, {}, {}, rememberLazyListState(), rememberLazyListState()) } }

@Preview(name = "No search results", showBackground = true, widthDp = 412, heightDp = 940)
@Composable
private fun NoResultsRestaurantListPreview() {
    RestaurantGuideTheme {
        RestaurantListScreen(DemoRestaurants.restaurants, "zzzz", emptySet(), {}, {}, {}, {}, {}, {}, {}, rememberLazyListState(), rememberLazyListState())
    }
}
