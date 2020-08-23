package com.example.track.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import com.example.track.R;
import com.example.track.constants.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import java.text.DecimalFormat;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class LocationServices extends Service {
    // Responsible for retrieving the location
//    private FusedLocationProviderClient mFusedLPClient;
    private static final DecimalFormat df = new DecimalFormat("#.00");
    private static final String TAG = "LocationServices";
    private static final long UPDATE_LOCATION_INTERVEL = 1000 * 2;
    private static final long SHORT_INTERVAL = 1000;
    private LocationCallback locationCallBack = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            Log.d(TAG, "onLocationResult: Got location result");
            Location location = locationResult.getLastLocation();
            if (location != null) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                double altitude = location.getAltitude();
                double speed = location.getSpeed();
                Intent gpsIntent = new Intent();
                gpsIntent.setAction("com.example.track.services.LocationServices." + Constants.GPS_UPDATE);
                Bundle gpsBundle = new Bundle();
                gpsBundle.putString(Constants.LONGITUDE, String.valueOf(df.format(longitude)));
                gpsBundle.putString(Constants.LATITUDE, String.valueOf(df.format(latitude)));
                gpsBundle.putString(Constants.SPEED, String.valueOf(df.format(speed)));
                gpsBundle.putString(Constants.ALTITUDE, String.valueOf(df.format(altitude)));
                gpsIntent.putExtras(gpsBundle);
                sendBroadcast(gpsIntent);
            }
        }
    };


    private void startLocationServiceHelper() {
        final String CHANNEL_ID = "my_channel_01";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Location Service")
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentText("Running")
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_MAX);

        // SDK above 26 requires the dev to show the GPS service running in notification tray
        if (Build.VERSION.SDK_INT >= 26) {
            if (notificationManager != null && notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Location Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is now used by Location Service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        // LocationRequest is used to constantly retrieving the location with a set interval
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_LOCATION_INTERVEL);
        locationRequest.setFastestInterval(SHORT_INTERVAL);

        FusedLocationProviderClient fusedLPClient = new FusedLocationProviderClient(this);
        fusedLPClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.getMainLooper());

        // same as above
        // com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallBack, Looper.getMainLooper());

        Log.i(TAG, "User has proper location permissions");

        startForeground(175, notificationBuilder.build());
    }

    private void stopLocationService() {
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallBack);
        stopForeground(true);
        stopSelf();
        Log.i(TAG, "stopLocationService: Stopped Location Service");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * Starting point when the service starts.
     */
    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
        Log.d(TAG, "OnStartCommand: Called");
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("startLocationService")) {
                    startLocationServiceHelper();
                } else if (action.equals("stopLocationService")) {
                    stopLocationService();
                }
            }
        }
        // The OS will not recreate this service after it is killed.
        // return START_NOT_STICKY;

        return super.onStartCommand(intent, flag, startId);
    }
}
