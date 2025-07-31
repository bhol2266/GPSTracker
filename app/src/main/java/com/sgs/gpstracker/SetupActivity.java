package com.sgs.gpstracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.sgs.gpstracker.services.LocationBroadcastService;
import com.sgs.gpstracker.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class SetupActivity extends AppCompatActivity {

    EditText nameInput;
    ImageView iconSelect;
    TextView deviceText;
    Button startBtn;

    String deviceName;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        FirebaseApp.initializeApp(this);

        nameInput = findViewById(R.id.nameInput);
        iconSelect = findViewById(R.id.iconSelect);
        deviceText = findViewById(R.id.deviceText);
        startBtn = findViewById(R.id.startBtn);

        nameInput.setText("Rosan"); // optional default

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        deviceName = model.startsWith(manufacturer) ? model : manufacturer + " " + model;
        deviceText.setText(deviceName);

        startBtn.setOnClickListener(v -> {
            name = nameInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            saveUserAndStart();

        });


    }

    private void saveUserAndStart() {
        String deviceId = getDeviceId(this);

        SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
        editor.putString("name", name);
        editor.putString("device", deviceName);
        editor.putString("deviceId", deviceId);
        editor.apply();

        FirebaseUtils.uploadUserToRealtimeDB(this, name, deviceName, deviceId,
                () -> {

                    Intent serviceIntent = new Intent(this, LocationBroadcastService.class);
                    serviceIntent.putExtra("deviceId", deviceId); // Pass deviceId to the service

                    if (isServiceRunning(LocationBroadcastService.class)) {
                        stopService(serviceIntent);
                    }

                    ContextCompat.startForegroundService(this, serviceIntent);


                    startActivity(new Intent(this, AdminPanelActivity.class));
                    finish();

                },
                () -> Toast.makeText(this, "Error saving user to database", Toast.LENGTH_SHORT).show());
    }


    public String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }



    private boolean isServiceRunning(Class<?> serviceClass) {
        android.app.ActivityManager manager = (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (android.app.ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



}
