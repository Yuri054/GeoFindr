package com.wordnerd.dictionary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wordnerd.dictionary.data.ALL_WORDS
import com.wordnerd.dictionary.data.Category
import com.wordnerd.dictionary.data.FavoritesRepository
import com.wordnerd.dictionary.data.DailyNotificationSettings
import com.wordnerd.dictionary.data.SettingsRepository
import com.wordnerd.dictionary.data.Word
import com.wordnerd.dictionary.data.wordOfTheDay
import com.wordnerd.dictionary.notifications.DailyWordNotifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WordViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FavoritesRepository(app)

    val favorites: StateFlow<Set<String>> =
        repo.favorites.stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val wordOfTheDay: Word = wordOfTheDay()

    // ── Pronunciation (engine is created lazily on first use) ──
    private val speaker = Speaker(app)
    fun speak(text: String) = speaker.speak(text)
    override fun onCleared() = speaker.shutdown()

    // ── Daily notification ──
    private val settingsRepo = SettingsRepository(app)
    val daily: StateFlow<DailyNotificationSettings> =
        settingsRepo.daily.stateIn(viewModelScope, SharingStarted.Eagerly, DailyNotificationSettings())

    init {
        // Make sure the next reminder is queued (e.g. after an app update cleared jobs),
        // without pushing back one that is already pending.
        viewModelScope.launch {
            val s = settingsRepo.current()
            if (s.enabled) DailyWordNotifier.schedule(app, s.hour, s.minute, replace = false)
        }
    }

    fun setDailyEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepo.setEnabled(enabled)
        val s = settingsRepo.current()
        if (enabled) DailyWordNotifier.schedule(getApplication(), s.hour, s.minute)
        else DailyWordNotifier.cancel(getApplication())
    }

    fun setDailyTime(hour: Int, minute: Int) = viewModelScope.launch {
        settingsRepo.setTime(hour, minute)
        if (settingsRepo.current().enabled) DailyWordNotifier.schedule(getApplication(), hour, minute)
    }

    fun previewNotification() = DailyWordNotifier.show(getApplication(), wordOfTheDay)

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
        val inCategory = ALL_WORDS.filter { c == null || it.category == c }
        if (needle.isEmpty()) return@combine inCategory
        // Rank: headword prefix, headword substring, definition, then origin
        // (so "Yiddish" or "Greek" finds words by where they come from).
        inCategory.mapNotNull { w ->
            when {
                w.word.startsWith(needle, ignoreCase = true) -> 0
                w.word.contains(needle, ignoreCase = true) -> 1
                w.definition.contains(needle, ignoreCase = true) -> 2
                w.origin.contains(needle, ignoreCase = true) -> 3
                else -> null
            }?.let { rank -> rank to w }
        }.sortedBy { it.first }.map { it.second }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ALL_WORDS)

    fun toggleFavorite(id: String) = viewModelScope.launch { repo.toggle(id) }
}
