package com.sgs.gpstracker.services;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sgs.gpstracker.MyApp;
import com.sgs.gpstracker.R;

import java.util.HashMap;
import java.util.Map;

public class LocationBroadcastService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler = new Handler();
    private Runnable locationTask;

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startForegroundServiceWithNotification();
        startLocationBroadcast();
    }

    private void startForegroundServiceWithNotification() {
        Notification notification = new NotificationCompat.Builder(this, MyApp.CHANNEL_ID)
                .setContentTitle("Live Location Broadcasting")
                .setContentText("Your location is being shared securely.")
                .setSmallIcon(R.drawable.ic_location)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        startForeground(1, notification);
    }

    private void startLocationBroadcast() {
        locationTask = new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                        ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.FOREGROUND_SERVICE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    handler.postDelayed(this, 1000);
                    return;
                }

                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        String userId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                        SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
                        String name = prefs.getString("name", "Unknown");
                        String device = prefs.getString("device", "Android");

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);

                        Map<String, Object> map = new HashMap<>();
                        map.put("name", name);
                        map.put("device", device);
                        map.put("latitude", location.getLatitude());
                        map.put("longitude", location.getLongitude());
                        map.put("timestamp", System.currentTimeMillis());

                        ref.updateChildren(map);
                    }
                });

                handler.postDelayed(this, 1000);
            }
        };

        handler.post(locationTask);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(locationTask);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
