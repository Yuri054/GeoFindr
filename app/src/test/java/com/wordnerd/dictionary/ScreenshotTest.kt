package com.wordnerd.dictionary

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onNodeWithContentDescription
import com.github.takahirom.roborazzi.captureRoboImage
import com.wordnerd.dictionary.data.ALL_WORDS
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi")
class ScreenshotTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun wordIdsAreUnique() {
        assertEquals(ALL_WORDS.size, ALL_WORDS.map { it.id }.toSet().size)
    }

    @Test fun walkthrough() {
        rule.onRoot().captureRoboImage("build/screenshots/1_today.png")

        rule.onNodeWithText("Browse").performClick()
        rule.waitForIdle()
        rule.onRoot().captureRoboImage("build/screenshots/2_browse.png")

        rule.onNodeWithText("Search words & meanings").performTextInput("bed")
        rule.waitForIdle()
        rule.onRoot().captureRoboImage("build/screenshots/3_search.png")

        rule.onNodeWithText("Clinomania").performClick()
        rule.waitForIdle()
        rule.onRoot().captureRoboImage("build/screenshots/4_detail.png")
        rule.onNodeWithContentDescription("Save word").performClick()
        rule.waitForIdle()
        rule.onNodeWithContentDescription("Back").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Saved").performClick()
        rule.waitForIdle()
        rule.onRoot().captureRoboImage("build/screenshots/5_saved.png")
    }
}
