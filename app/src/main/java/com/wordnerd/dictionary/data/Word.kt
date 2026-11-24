package com.wordnerd.dictionary.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(val label: String, val icon: ImageVector) {
    NATURE("Nature & Sky", Icons.Outlined.Forest),
    FEELINGS("Moods & Feelings", Icons.Outlined.Favorite),
    PEOPLE("People & Characters", Icons.Outlined.Face),
    WANDERING("Travel & Wandering", Icons.Outlined.Explore),
    WORDS("Words & Books", Icons.Outlined.AutoStories),
    BODY("Body & Senses", Icons.Outlined.Fingerprint),
    TIME("Time & Seasons", Icons.Outlined.Schedule),
    FOOD("Food & Drink", Icons.Outlined.Restaurant),
    MISCHIEF("Mischief & Nonsense", Icons.Outlined.TheaterComedy),
}

data class Word(
    val word: String,
    val pronunciation: String,
    val partOfSpeech: String,
    val definition: String,
    val origin: String,
    val example: String,
    val category: Category,
) {
    /** Stable identifier used for navigation and favourites. */
    val id: String get() = word.lowercase()
    val initial: Char get() = word.first().uppercaseChar()
}
