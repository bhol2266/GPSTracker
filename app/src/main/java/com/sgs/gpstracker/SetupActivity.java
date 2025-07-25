package com.sgs.gpstracker;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.FirebaseApp;
import com.sgs.gpstracker.services.LocationBroadcastService;

import java.util.ArrayList;
import java.util.List;

public class SetupActivity extends AppCompatActivity {

    EditText nameInput;
    ImageView iconSelect;
    TextView deviceText;
    Button startBtn;

    String deviceName;
    String name;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        FirebaseApp.initializeApp(this);

        nameInput = findViewById(R.id.nameInput);
        iconSelect = findViewById(R.id.iconSelect);
        deviceText = findViewById(R.id.deviceText);
        startBtn = findViewById(R.id.startBtn);

        nameInput.setText("Rosan");

        // Generate device name
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        deviceName = model.startsWith(manufacturer) ? model : manufacturer + " " + model;

        // Set device name to UI
        deviceText.setText(deviceName);

        startBtn.setOnClickListener(v -> {
            name = nameInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (hasRequiredPermissions()) {
                saveUserAndStart();
            } else {
                requestRequiredPermissions();
            }
        });
    }

    private void saveUserAndStart() {
        SharedPreferences.Editor editor = getSharedPreferences("user", MODE_PRIVATE).edit();
        editor.putString("name", name);
        editor.putString("device", deviceName);
        editor.apply();

        startService(new Intent(this, LocationBroadcastService.class));
        startActivity(new Intent(this, AdminPanelActivity.class));
    }

    private boolean hasRequiredPermissions() {
        boolean fineLocation = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean fgServiceLocation = Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        return fineLocation && fgServiceLocation;
    }

    private void requestRequiredPermissions() {
        List<String> permissions = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION);
            }
        }
        ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                saveUserAndStart();
            } else {
                Toast.makeText(this, "Permissions are required to start location tracking", Toast.LENGTH_LONG).show();
            }
        }
    }
}
