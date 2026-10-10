package ca.gbc.comp3074.p12.restaurantguide.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import ca.gbc.comp3074.p12.restaurantguide.R
import java.util.Locale

@Composable
fun AddRestaurantEntry(
    onBack: () -> Unit,
    onSave: (name: String, description: String, tags: List<String>, rating: Int) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var tags by rememberSaveable { mutableStateOf("") }
    var rating by rememberSaveable { mutableIntStateOf(1) }
    var nameError by rememberSaveable { mutableStateOf(false) }

    ListDestination(
        title = stringResource(R.string.destination_add_title),
        onBack = onBack,
        action = {
            TextButton(onClick = {
                nameError = name.isBlank()
                if (!nameError) {
                    val parsedTags = tags.split(',')
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .distinctBy { it.lowercase(Locale.ROOT) }
                    onSave(name.trim(), description.trim(), parsedTags, rating)
                }
            }) {
                Text(stringResource(R.string.destination_save))
            }
        },
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (it.isNotBlank()) nameError = false
            },
            label = { Text(stringResource(R.string.destination_restaurant_name)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = nameError,
            supportingText = if (nameError) {
                { Text(stringResource(R.string.destination_name_required)) }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(stringResource(R.string.destination_description)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        )
        OutlinedTextField(
            value = tags,
            onValueChange = { tags = it },
            label = { Text(stringResource(R.string.destination_tags)) },
            supportingText = { Text(stringResource(R.string.destination_tags_help)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.destination_your_rating),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..5).forEach { value ->
                    FilterChip(
                        selected = rating == value,
                        onClick = { rating = value },
                        label = { Text(stringResource(R.string.destination_rating, value)) },
                    )
                }
            }
        }
    }
}

@Composable
fun AboutEntry(onBack: () -> Unit) {
    ListDestination(title = stringResource(R.string.destination_about_title), onBack = onBack) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(stringResource(R.string.destination_project_info))
        Text(stringResource(R.string.destination_application_info))
        Text(
            text = stringResource(R.string.destination_team_members),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() },
        )
        Text(stringResource(R.string.destination_team_names))
    }
}

@Composable
private fun ListDestination(
    title: String,
    onBack: () -> Unit,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold { contentPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) {
                    Text(stringResource(R.string.destination_back))
                }
                Text(
                    text = title,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 2,
                )
                action?.invoke()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
        }
    }
}
