package com.sgs.gpstracker.utils;

import android.content.Context;
import android.location.Location;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;
import com.sgs.gpstracker.models.UserModel;
import com.sgs.gpstracker.services.LocationBroadcastService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FirebaseUtils {



    public static void uploadUserToRealtimeDB(Context context, String name, String deviceName, String deviceId,
                                              Runnable onSuccess, Runnable onFailure) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users").child(deviceId);

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("deviceName", deviceName);
        userMap.put("deviceId", deviceId);

        ref.updateChildren(userMap)
                .addOnSuccessListener(unused -> {
                    Log.d("FirebaseUpload", "User data uploaded successfully.");
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseUpload", "Failed to upload user data: " + e.getMessage(), e);
                    Toast.makeText(context, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    onFailure.run();
                });
    }

    public static void updateUserLocation(String deviceId, double latitude, double longitude) {
        DatabaseReference locationRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(deviceId)
                .child("location");

        Map<String, Object> locationUpdates = new HashMap<>();
        locationUpdates.put("latitude", latitude);
        locationUpdates.put("longitude", longitude);
        locationUpdates.put("timestamp", System.currentTimeMillis());

        locationRef.updateChildren(locationUpdates);
    }


    public interface UserLocationCallback {
        void onResult(List<UserModel> users);
    }
    public static void fetchAllUsers(UserLocationCallback callback) {
        FirebaseDatabase.getInstance().getReference("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<UserModel> users = new ArrayList<>();

                        for (DataSnapshot child : snapshot.getChildren()) {
                            String deviceId = child.child("deviceId").getValue(String.class);
                            String deviceName = child.child("deviceName").getValue(String.class);
                            String name = child.child("name").getValue(String.class);
                            Double lat = child.child("location/latitude").getValue(Double.class);
                            Double lng = child.child("location/longitude").getValue(Double.class);
                            Long timestamp = child.child("location/timestamp").getValue(Long.class);

                            if (name != null && lat != null && lng != null && timestamp != null) {
                                UserModel user = new UserModel();
                                user.setDeviceId(deviceId != null ? deviceId : "");
                                user.setDeviceName(deviceName != null ? deviceName : "");
                                user.setName(name);

                                UserModel.Location location = new UserModel.Location();
                                location.setLatitude(lat);
                                location.setLongitude(lng);
                                location.setTimestamp(timestamp);
                                user.setLocation(location);

                                // Check if the saved timestamp is older than 30 seconds
                                long currentTime = System.currentTimeMillis();
                                boolean isOnline = (currentTime - timestamp) <= 45000; // 30 seconds = 30 * 1000

                                Log.d("deviceId", "isOnline: " + isOnline);
                                user.setOnline(isOnline);

                                users.add(user);
                            }
                        }

                        callback.onResult(users);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onResult(new ArrayList<>());
                    }
                });
    }



    public interface UsersCallback {
        void onResult(List<UserModel> users);
    }

    public interface FirestoreCallback {
        void onSuccess();
        void onFailure();
    }

}
