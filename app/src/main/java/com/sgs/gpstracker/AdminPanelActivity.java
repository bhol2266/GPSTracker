package com.sgs.gpstracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;
import com.sgs.gpstracker.adapters.UserSliderAdapter;
import com.sgs.gpstracker.models.UserModel;
import com.sgs.gpstracker.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdminPanelActivity extends AppCompatActivity {

    private GoogleMap map;
    private RecyclerView slider;
    private Button refreshBtn, tracking;

    private final List<UserModel> users = new ArrayList<>();
    private final HashMap<String, Marker> userMarkers = new HashMap<>();
    private boolean isListenerAttached = false;

    private UserSliderAdapter adapter;
    private UserModel selectedUser;

    private ValueEventListener selectedUserListener = null;
    private DatabaseReference selectedUserRef = null;


    private Handler trackingHandler;
    private Runnable trackingRunnable;
    private boolean isTracking = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        slider = findViewById(R.id.userSlider);
        refreshBtn = findViewById(R.id.refreshBtn);
        tracking = findViewById(R.id.tracking);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> map = googleMap);
        }

        adapter = new UserSliderAdapter(users, this::onUserSelected);
        slider.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        slider.setAdapter(adapter);

        refreshBtn.setOnClickListener(v -> {
            Intent intent = getIntent();
            finish(); // Finish current activity
            startActivity(intent); // Restart activity with same intent
        });


        updateTrackingButtonUI(); // Set initial state
        tracking.setOnClickListener(v -> {
            if (selectedUser == null) {
                Toast.makeText(this, "Please select a user to track", Toast.LENGTH_SHORT).show();
                return;
            }

            isTracking = !isTracking;

            if (isTracking) {
                trackingHandler = new Handler();
                trackingRunnable = new Runnable() {
                    @Override
                    public void run() {
                        triggerLocationRequest(selectedUser.getDeviceId());
                        trackingHandler.postDelayed(this, 2000); // Repeat every 2 seconds
                    }
                };
                trackingHandler.post(trackingRunnable);
            } else {
                if (trackingHandler != null && trackingRunnable != null) {
                    trackingHandler.removeCallbacks(trackingRunnable);
                }
            }

            updateTrackingButtonUI();
        });


        fetchAllUsers();
    }

    private void onUserSelected(UserModel user) {
        isTracking = false;
        if (trackingHandler != null && trackingRunnable != null) {
            trackingHandler.removeCallbacks(trackingRunnable);
        }
        updateTrackingButtonUI();

        selectedUser = user;

        // Remove previous listener if any
        if (selectedUserRef != null && selectedUserListener != null) {
            selectedUserRef.removeEventListener(selectedUserListener);
        }
        triggerLocationRequest(user.getDeviceId());
        // Listen only to this user's location updates
        selectedUserRef = FirebaseDatabase.getInstance().getReference("users").child(user.getDeviceId());
        selectedUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String deviceId = snapshot.child("deviceId").getValue(String.class);
                String name = snapshot.child("name").getValue(String.class);
                String deviceName = snapshot.child("deviceName").getValue(String.class);

                Double lat = snapshot.child("location").child("latitude").getValue(Double.class);
                Double lng = snapshot.child("location").child("longitude").getValue(Double.class);
                Long timestamp = snapshot.child("location").child("timestamp").getValue(Long.class);

                if (lat != null && lng != null && timestamp != null) {

                    user.setName(name);
                    user.setDeviceName(deviceName);

                    UserModel.Location location = new UserModel.Location();
                    location.setLatitude(lat);
                    location.setLongitude(lng);
                    location.setTimestamp(timestamp);
                    user.setLocation(location);

                    long currentTimee = System.currentTimeMillis();
                    user.setOnline((currentTimee - timestamp) <= 45000);

                    adapter.notifyItemChanged(users.indexOf(selectedUser));

                    updateMapWithUserLocation(deviceId, name, lat, lng);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        };

        selectedUserRef.addValueEventListener(selectedUserListener);
    }

    private void triggerLocationRequest(String deviceId) {
        DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference("users")
                .child(deviceId).child("locationRequest");
        requestRef.setValue(true);
    }

    private void listenForAllLocationUpdates() {
        if (isListenerAttached) return;
        isListenerAttached = true;

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String deviceId = userSnap.child("deviceId").getValue(String.class);
                    String name = userSnap.child("name").getValue(String.class);
                    String deviceName = userSnap.child("deviceName").getValue(String.class);
                    Double lat = userSnap.child("location").child("latitude").getValue(Double.class);
                    Double lng = userSnap.child("location").child("longitude").getValue(Double.class);
                    Long timestamp = userSnap.child("location").child("timestamp").getValue(Long.class);

                    if (deviceId != null && lat != null && lng != null && timestamp != null) {
                        for (int i = 0; i < users.size(); i++) {
                            UserModel user = users.get(i);

                            if (user.getDeviceId().equals(deviceId)) {
                                user.setName(name);
                                user.setDeviceName(deviceName);

                                UserModel.Location location = new UserModel.Location();
                                location.setLatitude(lat);
                                location.setLongitude(lng);
                                location.setTimestamp(timestamp);
                                user.setLocation(location);

                                long currentTime = System.currentTimeMillis();
                                user.setOnline((currentTime - timestamp) <= 45000);

                                adapter.notifyItemChanged(i);

                                updateMapWithUserLocation(deviceId, name, lat, lng);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void updateMapWithUserLocation(String deviceId, String name, double lat, double lng) {
        if (map == null) return;

        LatLng position = new LatLng(lat, lng);

        // Remove previous marker if exists
        if (userMarkers.containsKey(deviceId)) {
            userMarkers.get(deviceId).remove();
        }

        Bitmap markerBitmap = createCustomMarker(name);
        Marker marker = map.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                .anchor(0.5f, 1f));

        userMarkers.put(deviceId, marker);

        if (selectedUser != null && selectedUser.getDeviceId().equals(deviceId)) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 16));
        }
    }

    private Bitmap createCustomMarker(String userName) {
        View markerView = getLayoutInflater().inflate(R.layout.marker_label, null);
        TextView nameText = markerView.findViewById(R.id.markerLabel);
        nameText.setText(userName);

        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);
        return bitmap;
    }

    private void fetchAllUsers() {
        FirebaseUtils.fetchAllUsers(userList -> {
            if (userList == null || userList.isEmpty()) return;

            users.clear();
            users.addAll(userList);
            adapter.notifyDataSetChanged();

            onUserSelected(users.get(0));
        });
    }


    private void updateTrackingButtonUI() {
        if (isTracking) {
            tracking.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            tracking.setText("Tracking ON");
        } else {
            tracking.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
            tracking.setText("Tracking OFF");
        }
    }


    @Override
    protected void onDestroy() {
        if (selectedUserRef != null && selectedUserListener != null) {
            selectedUserRef.removeEventListener(selectedUserListener);
        }
        super.onDestroy();
    }
}
