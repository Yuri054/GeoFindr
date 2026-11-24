package com.wordnerd.dictionary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wordnerd.dictionary.data.ALL_WORDS
import com.wordnerd.dictionary.data.Category
import com.wordnerd.dictionary.data.FavoritesRepository
import com.wordnerd.dictionary.data.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.random.Random

class WordViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FavoritesRepository(app)

    val favorites: StateFlow<Set<String>> =
        repo.favorites.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    /** Word of the day: a fixed shuffle of the lexicon, stepped one word per calendar day. */
    val wordOfTheDay: Word = run {
        val order = ALL_WORDS.shuffled(Random(20261123))
        order[Math.floorMod(LocalDate.now().toEpochDay(), order.size.toLong()).toInt()]
    }

    private val _discover = MutableStateFlow(pickRandom(exclude = wordOfTheDay))
    val discover: StateFlow<Word> = _discover.asStateFlow()

    fun shuffle() { _discover.value = pickRandom(exclude = _discover.value) }

    private fun pickRandom(exclude: Word): Word = ALL_WORDS.filter { it != exclude }.random()

    // ── Browse ──
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category.asStateFlow()

    fun setQuery(q: String) { _query.value = q }
    fun setCategory(c: Category?) { _category.value = c }

    val results: StateFlow<List<Word>> = combine(_query, _category) { q, c ->
        val needle = q.trim()
        ALL_WORDS.filter { w ->
            (c == null || w.category == c) &&
                (needle.isEmpty() ||
                    w.word.contains(needle, ignoreCase = true) ||
                    w.definition.contains(needle, ignoreCase = true))
        }.sortedBy { w ->
            // Headword matches first, then definition matches.
            if (needle.isNotEmpty() && !w.word.startsWith(needle, ignoreCase = true)) 1 else 0
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ALL_WORDS)

    fun toggleFavorite(id: String) = viewModelScope.launch { repo.toggle(id) }
}
