package com.offline.assistant;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.offline.assistant.command.CommandProcessor;
import com.offline.assistant.executor.ActionExecutor;
import com.offline.assistant.voice.VoiceRecognizerManager;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements VoiceRecognizerManager.VoiceListener {

    private static final int PERMISSION_REQUEST_CODE = 101;

    private TextView tvStatus;
    private TextView tvCommandOutput;
    private FloatingActionButton btnMic;

    private VoiceRecognizerManager voiceManager;
    private CommandProcessor processor;
    private ActionExecutor executor;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        initCoreEngine();
        checkAndRequestPermissions();
    }

    private void initUI() {
        tvStatus = findViewById(R.id.tvStatus);
        tvCommandOutput = findViewById(R.id.tvCommandOutput);
        btnMic = findViewById(R.id.btnMic);

        btnMic.setOnClickListener(v -> {
            if (checkRuntimePermissionsSilently()) {
                voiceManager.startListening();
            } else {
                checkAndRequestPermissions();
            }
        });
    }

    private void initCoreEngine() {
        processor = new CommandProcessor();
        executor = new ActionExecutor(this);
        voiceManager = new VoiceRecognizerManager(this, this);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.getDefault());
            }
        });
    }

    private void speak(String prompt) {
        if (tts != null) {
            tts.speak(prompt, TextToSpeech.QUEUE_FLUSH, null, "UtteranceID");
        }
    }

    @Override
    public void onStatusChanged(String status) {
        runOnUiThread(() -> tvStatus.setText("Status: " + status));
    }

    @Override
    public void onResultReady(String recognizedText) {
        runOnUiThread(() -> {
            tvCommandOutput.setText("Heard: \"" + recognizedText + "\"");
            executeParsedCommand(recognizedText);
        });
    }

    @Override
    public void onErrorOccurred(String errorMsg) {
        runOnUiThread(() -> {
            tvStatus.setText("Status: Idle");
            tvCommandOutput.setText("Error: " + errorMsg);
            speak(errorMsg);
        });
    }

    private void executeParsedCommand(String input) {
        CommandProcessor.CommandResult result = processor.process(input);
        boolean performanceSuccess = false;
        String actionSpeechResponse = "";

        switch (result.type) {
            case OPEN_CAMERA:
                performanceSuccess = executor.openCamera();
                actionSpeechResponse = performanceSuccess ? "Opening Camera" : "Failed to open camera";
                break;
            case FLASHLIGHT_ON:
                performanceSuccess = executor.setFlashlight(true);
                actionSpeechResponse = performanceSuccess ? "Flashlight turned on" : "Failed to manipulate flashlight";
                break;
            case FLASHLIGHT_OFF:
                performanceSuccess = executor.setFlashlight(false);
                actionSpeechResponse = performanceSuccess ? "Flashlight turned off" : "Failed to change flashlight status";
                break;
            case OPEN_WHATSAPP:
                performanceSuccess = executor.openWhatsApp();
                actionSpeechResponse = performanceSuccess ? "Opening WhatsApp" : "WhatsApp is not installed on this device";
                break;
            case OPEN_GALLERY:
                performanceSuccess = executor.openGallery();
                actionSpeechResponse = performanceSuccess ? "Opening Gallery" : "Failed to look up gallery data";
                break;
            case CALL_CONTACT:
                performanceSuccess = executor.callContact(result.payload);
                actionSpeechResponse = performanceSuccess ? "Calling " + result.payload : "Unable to place call";
                break;
            case WIFI_ON:
                performanceSuccess = executor.setWifiState(true);
                actionSpeechResponse = performanceSuccess ? "Enabling Wi-Fi" : "Unable to toggle Wi-Fi configurations";
                break;
            case WIFI_OFF:
                performanceSuccess = executor.setWifiState(false);
                actionSpeechResponse = performanceSuccess ? "Disabling Wi-Fi" : "Unable to shut down Wi-Fi service";
                break;
            case BLUETOOTH_ON:
                performanceSuccess = executor.setBluetoothState(true);
                actionSpeechResponse = performanceSuccess ? "Enabling Bluetooth" : "Bluetooth modification failed";
                break;
            case BLUETOOTH_OFF:
                performanceSuccess = executor.setBluetoothState(false);
                actionSpeechResponse = performanceSuccess ? "Disabling Bluetooth" : "Bluetooth shut down failed";
                break;
            case UNKNOWN:
            default:
                actionSpeechResponse = "Command not supported";
                break;
        }

        speak(actionSpeechResponse);
        tvCommandOutput.append("\n\nSystem Response: " + actionSpeechResponse);
    }

    private boolean checkRuntimePermissionsSilently() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
    }

    private void checkAndRequestPermissions() {
        ArrayList<String> targetedPermissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            targetedPermissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            targetedPermissions.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            targetedPermissions.add(Manifest.permission.CALL_PHONE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                targetedPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }

        if (!targetedPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, targetedPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                Toast.makeText(this, "Permissions required for features to execute properly.", Toast.LENGTH_LONG).show;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (voiceManager != null) voiceManager.destroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
