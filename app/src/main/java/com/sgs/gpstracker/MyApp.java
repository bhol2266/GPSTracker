package com.sgs.gpstracker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.google.firebase.FirebaseApp;

public class MyApp extends Application {

    public static final String CHANNEL_ID = "location_channel";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase
        FirebaseApp.initializeApp(this);

        // Create notification channel for location tracking
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("This channel is used for foreground location tracking");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
