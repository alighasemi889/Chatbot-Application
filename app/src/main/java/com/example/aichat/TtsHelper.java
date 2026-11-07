package com.example.aichat;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

/** Reads assistant replies aloud when the user enabled it. Uses the device's TTS engine. */
public final class TtsHelper {
    private final TextToSpeech tts;
    private boolean ready;
    private String pendingText;
    private String pendingLang;

    public TtsHelper(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            ready = status == TextToSpeech.SUCCESS;
            if (ready && pendingText != null) {
                speak(pendingText, pendingLang);
                pendingText = null;
            }
        });
    }

    public void speak(String text, String langTag) {
        if (!ready) {
            pendingText = text;
            pendingLang = langTag;
            return;
        }
        int r = tts.setLanguage(Locale.forLanguageTag(langTag));
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) return;
        tts.speak(text.replace("```", ""), TextToSpeech.QUEUE_FLUSH, null, "reply");
    }

    public void shutdown() {
        tts.stop();
        tts.shutdown();
    }
}
