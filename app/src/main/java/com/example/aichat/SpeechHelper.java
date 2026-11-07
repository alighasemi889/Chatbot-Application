package com.example.aichat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;

/** Speech-to-text using the device's recognizer. Language is fa-IR or en-US. */
public final class SpeechHelper {

    public interface Callback {
        void onResult(String text);
        void onError(int messageRes);
    }

    private SpeechRecognizer recognizer;

    public static boolean isAvailable(Activity activity) {
        return SpeechRecognizer.isRecognitionAvailable(activity);
    }

    public void start(Activity activity, String langTag, Callback cb) {
        if (!isAvailable(activity)) {
            cb.onError(R.string.speech_unavailable);
            return;
        }
        destroy();
        recognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (list != null && !list.isEmpty() && !list.get(0).trim().isEmpty()) {
                    cb.onResult(list.get(0));
                } else {
                    cb.onError(R.string.speech_no_result);
                }
            }

            @Override
            public void onError(int error) {
                cb.onError(error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
                        ? R.string.speech_no_result : R.string.speech_error);
            }

            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });

        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag);
        i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        recognizer.startListening(i);
    }

    public void destroy() {
        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }
    }
}
