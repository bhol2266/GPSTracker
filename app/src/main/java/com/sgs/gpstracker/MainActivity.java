package com.sgs.gpstracker;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sgs.gpstracker.services.LocationBroadcastService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Button gotoSetup=findViewById(R.id.gotoSetup);
        gotoSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SetupActivity.class);
                startActivity(intent);
            }
        });

        Button gotoAdminPanel=findViewById(R.id.gotoAdminPanel);
        gotoAdminPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AdminPanelActivity.class);
                startActivity(intent);
            }
        }); Button gotoCallogs=findViewById(R.id.gotoCallogs);
        gotoCallogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CallLogs.class);
                startActivity(intent);
            }
        });

        TextView serviceClassText=findViewById(R.id.serviceClassText);
        if (isMyServiceRunning(LocationBroadcastService.class)) {
            serviceClassText.setText( "Service is running");
        } else {
            serviceClassText.setText( "Not Running");
        }



        Button gotoSMS=findViewById(R.id.gotoSMS);
        gotoSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), SmsLogsActivity.class);
                startActivity(intent);
            }
        });



    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}