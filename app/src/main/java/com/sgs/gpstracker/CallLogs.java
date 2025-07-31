package com.sgs.gpstracker;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.*;
import com.sgs.gpstracker.adapters.CallLogAdapter;
import com.sgs.gpstracker.adapters.UserSliderAdapter;
import com.sgs.gpstracker.models.CallLogModel;
import com.sgs.gpstracker.models.UserModel;
import com.sgs.gpstracker.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class CallLogs extends AppCompatActivity {

    private RecyclerView callLogRecyclerView;
    private RecyclerView userSliderRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private CallLogAdapter callLogAdapter;
    private UserSliderAdapter userSliderAdapter;

    private final List<CallLogModel> callLogs = new ArrayList<>();
    private final List<UserModel> users = new ArrayList<>();

    private UserModel selectedUser;

    private ValueEventListener callLogListener;
    private DatabaseReference callLogRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_logs);

        initViews();
        setupCallLogRecyclerView();
        setupUserSlider();
        setupSwipeToRefresh();
        fetchAllUsers();
        fetchDefaultUserCallLogs("ede5d58bfd52cb38"); // Default Device ID
    }

    private void initViews() {
        callLogRecyclerView = findViewById(R.id.recyclerCallLogs);
        userSliderRecyclerView = findViewById(R.id.userSlider);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }

    private void setupCallLogRecyclerView() {
        callLogAdapter = new CallLogAdapter(callLogs);
        callLogRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        callLogRecyclerView.setAdapter(callLogAdapter);
    }

    private void setupUserSlider() {
        userSliderAdapter = new UserSliderAdapter(users, this::onUserSelected);
        userSliderRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        userSliderRecyclerView.setAdapter(userSliderAdapter);
    }

    private void setupSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (selectedUser != null) {
                triggerCallLogRequest(selectedUser.getDeviceId());
                // Do not stop refresh here — wait for Firebase to respond
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void fetchDefaultUserCallLogs(String deviceId) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(deviceId)
                .child("calllogs");

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CallLogModel> newLogs = new ArrayList<>();
                for (DataSnapshot logSnap : snapshot.getChildren()) {
                    CallLogModel log = logSnap.getValue(CallLogModel.class);
                    if (log != null) {
                        newLogs.add(log);
                    }
                }

                callLogs.clear();
                callLogs.addAll(newLogs);
                callLogAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void fetchCallLogsForUser(String deviceId) {
        callLogRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(deviceId)
                .child("calllogs");

        callLogListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                new Thread(() -> {
                    List<CallLogModel> newLogs = new ArrayList<>();
                    for (DataSnapshot logSnap : snapshot.getChildren()) {
                        CallLogModel log = logSnap.getValue(CallLogModel.class);
                        if (log != null) {
                            newLogs.add(log);
                        }
                    }

                    runOnUiThread(() -> {
                        callLogs.clear();
                        callLogs.addAll(newLogs);
                        callLogAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    });
                }).start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                runOnUiThread(() -> swipeRefreshLayout.setRefreshing(false));
            }
        };

        callLogRef.addValueEventListener(callLogListener);
    }

    private void onUserSelected(UserModel user) {
        selectedUser = user;
        String deviceId = user.getDeviceId();

        triggerCallLogRequest(deviceId);

        if (callLogRef != null && callLogListener != null) {
            callLogRef.removeEventListener(callLogListener);
        }

        fetchCallLogsForUser(deviceId);
    }

    private void triggerCallLogRequest(String deviceId) {
        DatabaseReference requestRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(deviceId)
                .child("callogRequest");

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
