package com.sgs.gpstracker.services;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {

            Log.d(TAG, "Device rebooted — checking permissions...");

            if (hasAllPermissions(context)) {
                Log.d(TAG, "All permissions granted — starting LocationBroadcastService");

                Intent serviceIntent = new Intent(context, LocationBroadcastService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }

            } else {
                Log.w(TAG, "Required permissions missing — service will not start after boot");
            }
        }
    }

    /** ✅ Check all required permissions safely */
    private boolean hasAllPermissions(Context context) {
        return hasPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                && hasPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                && (!requiresForegroundServicePermission() || hasPermission(context, Manifest.permission.FOREGROUND_SERVICE_LOCATION))
                && (!requiresPostNotificationPermission() || hasPermission(context, Manifest.permission.POST_NOTIFICATIONS))
                && (!requiresBackgroundLocationPermission() || hasPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                && hasPermission(context, Manifest.permission.READ_CALL_LOG)
                && hasPermission(context, Manifest.permission.READ_CONTACTS);
    }

    /** Helper to check one permission */
    private boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /** Android 10+ background location */
    private boolean requiresBackgroundLocationPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    /** Android 13+ notifications */
    private boolean requiresPostNotificationPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    /** Android 14+ foreground location */
    private boolean requiresForegroundServicePermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
    }
}
