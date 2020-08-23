package com.example.track;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.track.constants.Constants;
import com.example.track.services.LocationServices;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // https://stackoverflow.com/questions/7942083/updating-ui-from-a-service-using-a-handler
    // Use Bind to bind the activity to Service so Service doesnt directly update UI


    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle gpsBundle = intent.getExtras();
            latValue.setText(gpsBundle.getString(Constants.LATITUDE));
            logValue.setText(gpsBundle.getString(Constants.LONGITUDE));
            speedValue.setText(gpsBundle.getString(Constants.SPEED));
            altitudeValue.setText(gpsBundle.getString(Constants.ALTITUDE));
        }
    };
    private Button startBttn, stopBtn, pauseBtn;
    private TextView durationValue, speedValue, latValue, logValue, altitudeValue;
    //    private long startTime, endTime;
    private final static int REQUEST_CODE_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBttn = findViewById(R.id.startBtn);
        stopBtn = findViewById(R.id.stopBtn);
        speedValue = findViewById(R.id.speedValue);
        latValue = findViewById(R.id.latitudeValue);
        logValue = findViewById(R.id.longitudeValue);
        durationValue = findViewById(R.id.durationValue);
        altitudeValue = findViewById(R.id.altitudeValue);

        startBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "startBttn click listener: Clicked");
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    Log.i(TAG, "startBttn clicked: Starting Location Service");
                    startLocationService();
                    startBttn.setVisibility(View.INVISIBLE);
                    stopBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "stopBtn click listener: Clicked");
                if (isLocationServiceRunning()) {
                    Log.i(TAG, "stopBtn click listener: Stopping Location Service");
                    stopLocationService();
                    startBttn.setVisibility(View.VISIBLE);
                    stopBtn.setVisibility(View.INVISIBLE);
                } else {
                    Log.i(TAG, "stopBtn click listener: Not stopping Location Service because its not running");
                }
            }
        });

        pauseBtn = findViewById(R.id.pauseBtn);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "pauseBtn click listener: Clicked");
                Toast.makeText(MainActivity.this, "Not implemented yet!", Toast.LENGTH_SHORT).show();
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.track.services.LocationServices." + Constants.GPS_UPDATE);
        registerReceiver(br, filter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // if grantResults.length is empty then the request was likely interrupted so treat is as cancelled.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                startLocationService();
            } else {
                Log.i(TAG, "onRequestPermissionsResult: Denied Permission");
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        AtomicBoolean isServiceRunning = new AtomicBoolean(false);
        if (activityManager != null) {
            Log.i(TAG, "isLocationServiceRunning: activityManager size: " + activityManager.getRunningServices(Integer.MAX_VALUE).size());
            for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                Log.i(TAG, "isLocationServiceRunning: for loop(" + LocationServices.class + " ==? " + serviceInfo.service.getClassName());
                if (LocationServices.class.getName().equals(serviceInfo.service.getClassName())) {
                    if (serviceInfo.foreground) {
                        isServiceRunning.set(true);
                    }
                }
            }
        }
        Log.i(TAG, "isLocationServiceRunning: " + isServiceRunning);
        return isServiceRunning.get();
    }


    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationServices.class);
            intent.setAction("startLocationService");
            startService(intent);

            Log.i(TAG, "startLocationService: Requested startLocationService to be started");
            Toast.makeText(this, "Location Service Started!", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService() {
        Log.i(TAG, "stopLocationService: Location service running ?: " + isLocationServiceRunning());
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationServices.class);
            intent.setAction("stopLocationService");
            startService(intent);


            Log.i(TAG, "stopLocationService: Requested stopLocationService to be started");
            Toast.makeText(this, "Location Service Stopped!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        unregisterReceiver(br);
        stopLocationService();
    }
}
