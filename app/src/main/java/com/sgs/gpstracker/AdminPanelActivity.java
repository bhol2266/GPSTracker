package com.sgs.gpstracker;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.database.*;

import com.sgs.gpstracker.R;
import com.sgs.gpstracker.models.UserLocation;
import com.sgs.gpstracker.adapters.UserSliderAdapter;

import java.util.*;

public class AdminPanelActivity extends AppCompatActivity {

    GoogleMap map;
    RecyclerView slider;
    Button refreshBtn;
    List<UserLocation> users = new ArrayList<>();
    UserSliderAdapter adapter;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        slider = findViewById(R.id.userSlider);
        refreshBtn = findViewById(R.id.refreshBtn);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(googleMap -> map = googleMap);

        adapter = new UserSliderAdapter(users, this::onUserSelected);
        slider.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        slider.setAdapter(adapter);

        refreshBtn.setOnClickListener(v -> fetchUsers());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchUsers();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void fetchUsers() {
        FirebaseDatabase.getInstance().getReference("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        users.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            UserLocation user = child.getValue(UserLocation.class);
                            users.add(user);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });
    }

    private void onUserSelected(UserLocation user) {
        map.clear();
        LatLng pos = new LatLng(user.getLatitude(), user.getLongitude());
        map.addMarker(new MarkerOptions().position(pos).title(user.getName()));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16));
    }
}
