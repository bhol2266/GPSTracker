package com.sgs.gpstracker;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.*;
import com.sgs.gpstracker.adapters.SmsLogAdapter;
import com.sgs.gpstracker.adapters.UserSliderAdapter;
import com.sgs.gpstracker.models.SmsModel;
import com.sgs.gpstracker.models.UserModel;
import com.sgs.gpstracker.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class SmsLogsActivity extends AppCompatActivity {

    private RecyclerView smsLogRecyclerView;
    private RecyclerView userSliderRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SmsLogAdapter smsLogAdapter;
    private UserSliderAdapter userSliderAdapter;

    private final List<SmsModel> smsLogs = new ArrayList<>();
    private final List<UserModel> users = new ArrayList<>();

    private UserModel selectedUser;

    private ValueEventListener smsLogListener;
    private DatabaseReference smsLogRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_logs);

        initViews();
        setupSmsLogRecyclerView();
        setupUserSlider();
        setupSwipeToRefresh();
        fetchAllUsers();
        fetchDefaultUserSmsLogs("ede5d58bfd52cb38"); // Default Device ID
    }

    private void initViews() {
        smsLogRecyclerView = findViewById(R.id.recyclerSmsLogs);
        userSliderRecyclerView = findViewById(R.id.userSlider);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void setupSmsLogRecyclerView() {
        smsLogAdapter = new SmsLogAdapter(this, smsLogs);
        smsLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        smsLogRecyclerView.setAdapter(smsLogAdapter);
    }

    private void setupUserSlider() {
        userSliderAdapter = new UserSliderAdapter(users, this::onUserSelected);
        userSliderRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        userSliderRecyclerView.setAdapter(userSliderAdapter);
    }

    private void setupSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (selectedUser != null) {
                triggerSmsLogRequest(selectedUser.getDeviceId());
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void fetchDefaultUserSmsLogs(String deviceId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(deviceId)
                .child("smslogs");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SmsModel> newLogs = new ArrayList<>();
                for (DataSnapshot logSnap : snapshot.getChildren()) {
                    SmsModel log = logSnap.getValue(SmsModel.class);
                    if (log != null) {
                        newLogs.add(log);
                    }
                }

                smsLogs.clear();
                smsLogs.addAll(newLogs);
                smsLogAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });
    }

    private void fetchSmsLogsForUser(String deviceId) {
        smsLogRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(deviceId)
                .child("smslogs");

        smsLogListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                new Thread(() -> {
                    List<SmsModel> newLogs = new ArrayList<>();
                    for (DataSnapshot logSnap : snapshot.getChildren()) {
                        SmsModel log = logSnap.getValue(SmsModel.class);
                        if (log != null) {
                            newLogs.add(log);
                        }
                    }

                    runOnUiThread(() -> {
                        smsLogs.clear();
                        smsLogs.addAll(newLogs);
                        smsLogAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }).start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
            }
        };

        smsLogRef.addValueEventListener(smsLogListener);
    }

    private void onUserSelected(UserModel user) {
        selectedUser = user;
        String deviceId = user.getDeviceId();

        triggerSmsLogRequest(deviceId);

        if (smsLogRef != null && smsLogListener != null) {
            smsLogRef.removeEventListener(smsLogListener);
        }

        fetchSmsLogsForUser(deviceId);
    }

    private void triggerSmsLogRequest(String deviceId) {
        DatabaseReference requestRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(deviceId)
                .child("smsRequest");

        requestRef.setValue(true);
    }

    private void fetchAllUsers() {
        FirebaseUtils.fetchAllUsers(userList -> {
            if (userList == null || userList.isEmpty()) return;

            users.clear();
            users.addAll(userList);
            userSliderAdapter.notifyDataSetChanged();

            onUserSelected(users.get(0));
        });
    }
}
