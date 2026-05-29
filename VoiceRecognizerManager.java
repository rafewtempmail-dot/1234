package com.offline.assistant.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.ArrayList;
import java.util.Locale;

public class VoiceRecognizerManager {

    public interface VoiceListener {
        void onStatusChanged(String status);
        void onResultReady(String recognizedText);
        void onErrorOccurred(String errorMsg);
    }

    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private final VoiceListener listener;
    private boolean isListening = false;

    public VoiceRecognizerManager(Context context, VoiceListener listener) {
        this.listener = listener;
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            setupIntent();
            setupListener();
        } else {
            listener.onErrorOccurred("Speech Recognition not available on this device.");
        }
    }

    private void setupIntent() {
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        // Hints system to optimize for local on-device engines if available
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
    }

    private void setupListener() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                isListening = true;
                listener.onStatusChanged("Listening...");
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                isListening = false;
                listener.onStatusChanged("Processing...");
            }

            @Override
            public void onError(int error) {
                isListening = false;
                String message;
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO: message = "Audio recording error"; break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: message = "Permissions missing"; break;
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    case SpeechRecognizer.ERROR_NETWORK: message = "Network/Offline issue parsing language model"; break;
                    case SpeechRecognizer.ERROR_NO_MATCH: message = "No match found. Please try again."; break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: message = "No speech detected."; break;
                    default: message = "Error listening. Try again."; break;
                }
                listener.onErrorOccurred(message);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    listener.onResultReady(matches.get(0));
                } else {
                    listener.onErrorOccurred("No speech recognized.");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    public void startListening() {
        if (speechRecognizer != null && !isListening) {
            speechRecognizer.startListening(recognizerIntent);
        }
    }

    public void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
