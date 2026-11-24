package com.wordnerd.dictionary.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Single DataStore for the whole app (favourites + settings). */
internal val Context.dataStore by preferencesDataStore(name = "word_nerd")

class FavoritesRepository(private val context: Context) {
    private val key = stringSetPreferencesKey("favorites")

    val favorites: Flow<Set<String>> =
        context.dataStore.data.map { it[key] ?: emptySet() }

    suspend fun toggle(id: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[key] ?: emptySet()
            prefs[key] = if (id in current) current - id else current + id
        }
    }
}
