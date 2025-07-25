package com.sgs.gpstracker.utils;

import android.content.Context;
import android.location.Location;
import android.provider.Settings;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseUtils {

    public static void updateLocationToFirebase(Context context, Location location) {
        String userId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(userId);

        Map<String, Object> map = new HashMap<>();
        map.put("latitude", location.getLatitude());
        map.put("longitude", location.getLongitude());
        map.put("timestamp", System.currentTimeMillis());

        ref.updateChildren(map);
    }
}
