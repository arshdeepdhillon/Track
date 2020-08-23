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

/**
 * Starting point of Track activity.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
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

    //TODO private long startTime, endTime;
    private Button startBttn, stopBtn, pauseBtn;
    private TextView durationValue, speedValue, latValue, logValue, altitudeValue;
    private final static int REQUEST_CODE_LOCATION_PERMISSION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        startBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "startBttn click listener: Clicked");
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                } else {
                    Log.d(TAG, "startBttn clicked: Starting Location Service");
                    startLocationService();
                    startBttn.setVisibility(View.INVISIBLE);
                    stopBtn.setVisibility(View.VISIBLE);
                    pauseBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "stopBtn click listener: Clicked");
                if (isLocationServiceRunning()) {
                    Log.d(TAG, "stopBtn click listener: Stopping Location Service");
                    stopLocationService();
                    startBttn.setVisibility(View.VISIBLE);
                    stopBtn.setVisibility(View.INVISIBLE);
                    pauseBtn.setVisibility(View.INVISIBLE);
                } else {
                    Log.d(TAG, "stopBtn click listener: Not stopping Location Service because its not running");
                }
            }
        });

        pauseBtn = findViewById(R.id.pauseBtn);
        //TODO pauseBtn
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "pauseBtn click listener: Clicked");
                Toast.makeText(MainActivity.this, "Not implemented yet!", Toast.LENGTH_SHORT).show();
            }
        });

        // Start listening for gps updates
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.track.services.LocationServices." + Constants.GPS_UPDATE);
        registerReceiver(br, filter);
    }


    /**
     * Initializes variables to be used by {@link MainActivity}.
     */
    private void init() {
        startBttn = findViewById(R.id.startBtn);
        stopBtn = findViewById(R.id.stopBtn);
        speedValue = findViewById(R.id.speedValue);
        latValue = findViewById(R.id.latitudeValue);
        logValue = findViewById(R.id.longitudeValue);
        durationValue = findViewById(R.id.elapsedValue);
        altitudeValue = findViewById(R.id.altitudeValue);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // if grantResults.length is empty then the request was likely interrupted so treat is as cancelled.
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: Permission Granted");
                startLocationService();
            } else {
                Log.d(TAG, "onRequestPermissionsResult: Denied Permission");
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Checks if {@link LocationServices} service is running.
     *
     * @return true if the {@link LocationServices} service is running otherwise false
     */
    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        AtomicBoolean isServiceRunning = new AtomicBoolean(false);
        if (activityManager != null) {
            Log.d(TAG, "isLocationServiceRunning: activityManager size: " + activityManager.getRunningServices(Integer.MAX_VALUE).size());
            for (ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
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


    /**
     * Starts {@link LocationServices} service.
     */
    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationServices.class);
            intent.setAction("startLocationService");
            startService(intent);

            Log.d(TAG, "startLocationService: Requested startLocationService to be started");
            Toast.makeText(this, "Location Service Started!", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Stops {@link LocationServices} service.
     */
    private void stopLocationService() {
        Log.d(TAG, "stopLocationService: Location service is running: " + isLocationServiceRunning());
        if (isLocationServiceRunning()) {
            Intent intent = new Intent(getApplicationContext(), LocationServices.class);
            intent.setAction("stopLocationService");
            startService(intent);

            Log.d(TAG, "stopLocationService: Requested stopLocationService to be started");
            Toast.makeText(this, "Location Service Stopped!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        // Stop listening for gps updates
        unregisterReceiver(br);
        stopLocationService();
    }
}
