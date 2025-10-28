package com.sgs.gpstracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionsCheckActivity extends AppCompatActivity {

    private static final int REQUEST_ALL_PERMISSIONS = 111;
    private static final int REQUEST_BACKGROUND_LOCATION = 112;

    Button requestPermissionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_check);

        requestPermissionBtn = findViewById(R.id.permissionBtn);
        if (hasAllPermissions()) {
            gotoDashboard();
        }
        requestPermissionBtn.setOnClickListener(v -> {
            if (hasAllPermissions()) {
                Toast.makeText(this, "All permissions already granted!", Toast.LENGTH_SHORT).show();
            } else {
                requestAllPermissions();
            }
            requestIgnoreBatteryOptimization(); // Ask for battery optimization exclusion
        });
    }

    private boolean hasAllPermissions() {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                && (!requiresForegroundServicePermission() || hasPermission(Manifest.permission.FOREGROUND_SERVICE_LOCATION))
                && (!requiresPostNotificationPermission() || hasPermission(Manifest.permission.POST_NOTIFICATIONS))
                && (!requiresBackgroundLocationPermission() || hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                && hasPermission(Manifest.permission.READ_CALL_LOG)
                && hasPermission(Manifest.permission.READ_CONTACTS);
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean requiresBackgroundLocationPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    private boolean requiresForegroundServicePermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
    }

    private boolean requiresPostNotificationPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    private void requestAllPermissions() {
        List<String> permissions = new ArrayList<>();

        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (!hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (requiresForegroundServicePermission() && !hasPermission(Manifest.permission.FOREGROUND_SERVICE_LOCATION))
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION);
        if (requiresPostNotificationPermission() && !hasPermission(Manifest.permission.POST_NOTIFICATIONS))
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        if (!hasPermission(Manifest.permission.READ_CALL_LOG))
            permissions.add(Manifest.permission.READ_CALL_LOG);
        if (!hasPermission(Manifest.permission.READ_CONTACTS))
            permissions.add(Manifest.permission.READ_CONTACTS);

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), REQUEST_ALL_PERMISSIONS);
        } else {
            // If all except background are granted, request background directly
            if (requiresBackgroundLocationPermission() && !hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_BACKGROUND_LOCATION);
            }
        }
    }

    private void requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            String packageName = getPackageName();
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ALL_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                // Request background location next if needed
                if (requiresBackgroundLocationPermission()
                        && !hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_BACKGROUND_LOCATION);
                } else {
                    Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
                    gotoDashboard();
                }
            } else {
                Toast.makeText(this, "Some permissions were denied!", Toast.LENGTH_LONG).show();
            }

        } else if (requestCode == REQUEST_BACKGROUND_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Background location permission granted!", Toast.LENGTH_SHORT).show();
                gotoDashboard();
            } else {
                Toast.makeText(this, "Background location permission denied!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void gotoDashboard() {
        showPasswordDialog("5555");  // current password
    }

    private void showPasswordDialog(String correctPassword) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Password");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Add buttons but don't handle validation here
        builder.setPositiveButton("OK", null); // we'll override later
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // do nothing, prevent closing if you don’t want Cancel
            // dialog.cancel(); // <-- remove if you want to force password entry
        });

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // disable back press/outside dismiss

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String enteredPassword = input.getText().toString().trim();
                if (enteredPassword.equals(correctPassword)) {
                    dialog.dismiss();
                    startActivity(new Intent(PermissionsCheckActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                    input.setText(""); // clear input (optional)
                }
            });

            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setOnClickListener(v -> {
                // Do nothing OR show a toast instead
                Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show();
            });
        });

        dialog.show();
    }

}
