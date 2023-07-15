package com.example.reminderbylocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.example.reminderbylocation.databinding.ActivityReminderDetailsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

public class ReminderDetails extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private SupportMapFragment supportMapFragment;

    private Reminder reminder;

    private ActivityReminderDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReminderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);        supportMapFragment.getMapAsync(this);
        initReminderFromIntent();
        setViews();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng centerMapLocation = new LatLng(0,0);
        setCameraPosition(0,centerMapLocation);
        setMap();
    }
    private void setCameraPosition(int zoom, LatLng myLocation) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLocation)
                .zoom(zoom)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void setViews() {
        binding.titleTxt.setText(reminder.getTitle());
        if(!reminder.getNotes().isEmpty())
            binding.notesTxt.setText(reminder.getNotes());
        binding.distanceTXT.setText(reminder.getRadius()+" KM");
    }

    private void setMap() {
        LatLng latLng = new LatLng(reminder.getLocation().getLat(),reminder.getLocation().getLng());
        googleMap.addMarker(new MarkerOptions().position(latLng).title(reminder.getFeatureName()));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,9));
        // Draw circle
        int radius = reminder.getRadius() * 1000; // Convert distance from km to meters
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeColor(Color.BLUE)
                .strokeWidth(2f)
                .fillColor(Color.parseColor("#03A9F4")); // Transparent red fill color
        googleMap.addCircle(circleOptions);
    }

    private void initReminderFromIntent() {
        Intent intent = getIntent();
        String jsonReminder = intent.getStringExtra("REMINDER_JSON");
        Gson gson = new Gson();
        reminder = gson.fromJson(jsonReminder, Reminder.class);
    }
}