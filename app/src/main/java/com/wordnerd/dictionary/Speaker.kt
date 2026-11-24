package com.wordnerd.dictionary

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Text-to-speech wrapper that only binds the (heavy, out-of-process) TTS engine the first
 * time something is actually spoken, and keeps a single instance for the whole app session.
 * Creating an engine per screen made the system spin up the TTS service — and load its
 * voice data — every time the app opened, even if the speaker button was never tapped.
 */
class Speaker(context: Context) {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: String? = null

    fun speak(text: String) {
        val engine = tts
        if (engine == null) {
            pending = text
            tts = TextToSpeech(appContext) { status ->
                ready = status == TextToSpeech.SUCCESS
                if (ready) {
                    tts?.language = Locale.US
                    pending?.let { say(it) }
                }
                pending = null
            }
        } else if (ready) {
            say(text)
        } else {
            pending = text
        }
    }

    private fun say(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word")
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        ready = false
    }
}
