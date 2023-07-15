package com.example.reminderbylocation.services;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.reminderbylocation.Constants;
import com.example.reminderbylocation.IsServiceLocation;
import com.example.reminderbylocation.SimpleLocation;
import com.example.reminderbylocation.MainActivity;
import com.example.reminderbylocation.R;
import com.example.reminderbylocation.Reminder;
import com.example.reminderbylocation.RemindersSharedPreferences;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LocationService extends Service {

    private static NotificationManager nm;

    private int lastShownNotificationId = -1;
    public static String CHANNEL_ID_FOREGROUND = "FOREGROUND.LOCATION.SERVICE";
    public static String CHANNEL_ID_REMINDERS = "REMINDERS.SERVICE";
    public static String MAIN_ACTION = "NOTIFICATION_INTENT";
    private NotificationCompat.Builder foregroundBuilder;
    private NotificationCompat.Builder notificationBuilder;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private double lat;
    private double lng;

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }

        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                lat = locationResult.getLastLocation().getLatitude();
                lng = locationResult.getLastLocation().getLongitude();
                checkIfNearReminders();
                foregroundBuilder
                        .setContentText(getAddressFromLocation(lat,lng));
                nm.notify(Constants.SERVICE_ID, foregroundBuilder.build());
            }
        }
    };

    private void checkIfNearReminders() {
        ArrayList<Reminder> list = RemindersSharedPreferences.getInstance().getList();
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        LatLng centerLatLng = new LatLng(lat, lng);
        for (Reminder reminder : list) {
            float[] distance = new float[1];
            SimpleLocation location = reminder.getLocation();
            LatLng currentLatLng = new LatLng(location.getLat(), location.getLng());

            Location.distanceBetween(centerLatLng.latitude, centerLatLng.longitude,
                    currentLatLng.latitude, currentLatLng.longitude, distance);
            Log.d("checkIfNearReminders: ", distance[0] +" "+ reminder.getRadius());
            if (distance[0] < reminder.getRadius()*1000) {
                invokeNotification(reminder,reminder.getId());
            }
            boundsBuilder.include(currentLatLng);
        }
    }

    private String getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                // Retrieve address details from the Address object
                String addressLine = address.getAddressLine(0);
                return addressLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void invokeNotification(Reminder reminder,int id) {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Create the notification
        notificationBuilder
                .setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setContentTitle(reminder.getTitle().concat(" in ").concat(reminder.getFeatureName()))
                .setContentText(reminder.getNotes())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Display the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(id, notificationBuilder.build());


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        nm = (NotificationManager) this.getSystemService(Service.NOTIFICATION_SERVICE);
        if (intent != null){
            String action = intent.getAction();
            if(action.equals(Constants.START_LOCATION_SERVICE)){
                notifyToUserForForegroundService();
                startGPS();
            }else if(action.equals(Constants.STOP_LOCATION_SERVICE)){
                stopLocationService();
            }
        }
        return START_STICKY;
    }

    private void startGPS() {
        // Run GPS
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = new LocationRequest.Builder(1000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMinUpdateDistanceMeters(1.0f)
                    .setMinUpdateIntervalMillis(10000)
                    .setMaxUpdateDelayMillis(TimeUnit.MINUTES.toMillis(1))
                    .build();

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    private void stopLocationService(){
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }


    // // // // // // // // // // // // // // // // Notification  // // // // // // // // // // // // // // //

    private void notifyToUserForForegroundService() {
        // On notification click
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, Constants.SERVICE_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        foregroundBuilder = getNotificationBuilder(this,
                CHANNEL_ID_FOREGROUND,
                NotificationManagerCompat.IMPORTANCE_LOW); //Low importance prevent visual appearance for this notification channel on top

        foregroundBuilder
                .setContentIntent(pendingIntent) // Open activity
                .setOngoing(true)
                .setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.baseline_notifications_active_24))
                .setContentTitle("Your Location");

        Notification notification = foregroundBuilder.build();

        startForeground(Constants.SERVICE_ID, notification);

        if (Constants.SERVICE_ID != lastShownNotificationId) {
            // Cancel previous notification
            final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.cancel(lastShownNotificationId);
        }
        lastShownNotificationId = Constants.SERVICE_ID;

        notificationBuilder = getNotificationBuilder(this,CHANNEL_ID_REMINDERS,NotificationManagerCompat.IMPORTANCE_HIGH);


    }

    public static NotificationCompat.Builder getNotificationBuilder(Context context, String channelId, int importance) {
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance);
            builder = new NotificationCompat.Builder(context, channelId);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
        return builder;
    }

    @TargetApi(26)
    private static void prepareChannel(Context context, String id, int importance) {
        final String appName = context.getString(R.string.app_name);
        String notifications_channel_description = "Running map channel";
        String description = notifications_channel_description;

        if(nm != null) {
            NotificationChannel nChannel = nm.getNotificationChannel(id);

            if (nChannel == null) {
                nChannel = new NotificationChannel(id, appName, importance);
                nChannel.setDescription(description);

                // from another answer
                nChannel.enableLights(true);
                nChannel.setLightColor(Color.BLUE);

                nm.createNotificationChannel(nChannel);
            }
        }
    }

    private void updateNotification(String content) {
        foregroundBuilder.setContentText(content);
        final NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.notify(Constants.SERVICE_ID, foregroundBuilder.build());
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}