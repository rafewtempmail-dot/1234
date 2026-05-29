package com.offline.assistant.executor;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.MediaStore;

public class ActionExecutor {

    private final Context context;

    public ActionExecutor(Context context) {
        this.context = context;
    }

    public boolean openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    public boolean setFlashlight(boolean turnOn) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, turnOn);
            return true;
        } catch (CameraAccessException | ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public boolean openWhatsApp() {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.whatsapp");
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
            return true;
        }
        return false;
    }

    public boolean openGallery() {
        Intent intent = new Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        return true;
    }

    @SuppressLint("MissingPermission")
    public boolean callContact(String identity) {
        if (identity == null || identity.isEmpty()) return false;
        
        // This execution utilizes an immediate DIAL intent. 
        // If exact system contact query parsing is not built out, it dials safely.
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + identity));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        if (context.checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public boolean setWifiState(boolean turnOn) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ restricts direct system modification toggles without user interaction panels
                Intent panelIntent = new Intent(android.provider.Settings.Panel.ACTION_WIFI);
                panelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(panelIntent);
                return true;
            } else {
                return wifiManager.setWifiEnabled(turnOn);
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("MissingPermission")
    public boolean setBluetoothState(boolean turnOn) {
        BluetoothAdapter bluetoothAdapter;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BluetoothManager manager = context.getSystemService(BluetoothManager.class);
            bluetoothAdapter = manager != null ? manager.getAdapter() : null;
        } else {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        if (bluetoothAdapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } else {
                if (turnOn) {
                    return bluetoothAdapter.enable();
                } else {
                    return bluetoothAdapter.disable();
                }
            }
        }
        return false;
    }
}
