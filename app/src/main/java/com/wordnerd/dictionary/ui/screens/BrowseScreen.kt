package com.wordnerd.dictionary.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import com.wordnerd.dictionary.WordViewModel
import com.wordnerd.dictionary.data.Category
import com.wordnerd.dictionary.data.Word
import com.wordnerd.dictionary.ui.components.WordListItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BrowseScreen(vm: WordViewModel, onOpenWord: (Word) -> Unit) {
    val query by vm.query.collectAsState()
    val category by vm.category.collectAsState()
    val results by vm.results.collectAsState()
    val favorites by vm.favorites.collectAsState()
    val focus = LocalFocusManager.current
    val listState = rememberLazyListState()

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        // Google-style pill search bar
        TextField(
            value = query,
            onValueChange = vm::setQuery,
            placeholder = { Text("Search words & meanings") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = { vm.setQuery("") }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = category == null,
                    onClick = { vm.setCategory(null) },
                    label = { Text("All") },
                )
            }
            items(Category.entries) { c ->
                FilterChip(
                    selected = category == c,
                    onClick = { vm.setCategory(if (category == c) null else c) },
                    label = { Text(c.label) },
                    leadingIcon = {
                        Icon(c.icon, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
                    },
                )
            }
        }

        if (results.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.SearchOff,
                title = "No words found",
                body = "Try another spelling, or search for a meaning like \"sleep\" or \"rain\".",
            )
            return@Column
        }

        val showHeaders = query.isBlank()
        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 16.dp)) {
            if (showHeaders) {
                results.groupBy { it.initial }.forEach { (letter, words) ->
                    stickyHeader(key = "h$letter") {
                        Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                letter.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 24.dp, top = 12.dp, bottom = 4.dp),
                            )
                        }
                    }
                    items(words, key = { it.id }) { w ->
                        WordListItem(w, w.id in favorites, { onOpenWord(w) }, { vm.toggleFavorite(w.id) })
                    }
                }
            } else {
                items(results, key = { it.id }) { w ->
                    WordListItem(w, w.id in favorites, { onOpenWord(w) }, { vm.toggleFavorite(w.id) })
                }
            }
        }
    }
}
