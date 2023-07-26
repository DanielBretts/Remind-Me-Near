package com.example.reminderbylocation.Fragments;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.reminderbylocation.Model.SimpleLocation;
import com.example.reminderbylocation.Activities.MainActivity;
import com.example.reminderbylocation.R;
import com.example.reminderbylocation.Model.Reminder;
import com.example.reminderbylocation.Utils.RemindersSharedPreferences;
import com.example.reminderbylocation.databinding.FragmentMapBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapViewFragment extends Fragment implements OnMapReadyCallback{
    private GoogleMap googleMap;
    private FragmentMapBinding binding;
    private SupportMapFragment supportMapFragment;
    private Address address = null;
    private int distance = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        binding.svPlaces.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                googleMap.clear();
                String location = binding.svPlaces.getQuery().toString();
                List<Address> addresses = null;
                if(location != null && !location.isEmpty()){
                    Geocoder geocoder = new Geocoder(getContext());
                    try{
                        addresses = geocoder.getFromLocationName(location,1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if(addresses.size()>0){
                        address = addresses.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                        googleMap.addMarker(new MarkerOptions().position(latLng).title(location));
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,12));
                        // Draw circle
                        int radius = distance * 1000; // Convert distance from km to meters
                        CircleOptions circleOptions = new CircleOptions()
                                .center(latLng)
                                .radius(radius)
                                .strokeColor(Color.BLUE)
                                .strokeWidth(2f)
                                .fillColor(Color.parseColor("#03A9F4")); // Transparent red fill color
                        googleMap.addCircle(circleOptions);
                    }else {
                        Toast.makeText(getContext(),location.concat(" Not found"),Toast.LENGTH_LONG).show();
                        address = null;
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        supportMapFragment.getMapAsync(this);

        binding.simpleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distance = progress;
                binding.distanceTXT.setText(String.valueOf(distance).concat(" Km"));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        binding.btnAddReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkIfInputsAreValid()){
                    saveReminder();
                    ((MainActivity)getActivity()).replaceFragments(RemindersFragment.class);
                }
            }

        });
    }

    private void saveReminder() {
        ArrayList<Reminder> list = RemindersSharedPreferences.getInstance().getList();
        list.add(new Reminder(new SimpleLocation(address.getLatitude(),address.getLongitude()),
                binding.reminderTitle.getText().toString(),
                binding.remindersETNotes.getText().toString(),
                address.getAddressLine(0),
                distance));
        RemindersSharedPreferences.getInstance().setList(list);
    }

    private boolean checkIfInputsAreValid() {
        if(binding.reminderTitle.getText().toString().isEmpty()) {
            Toast.makeText(getContext(),"You must add a title",Toast.LENGTH_SHORT).show();
            return  false;
        } else if (address == null) {
            Toast.makeText(getContext(),"You must search for a location",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }



    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        LatLng centerMapLocation = new LatLng(0,0);
        setCameraPosition(0,centerMapLocation);

    }

    private void setCameraPosition(int zoom, LatLng myLocation) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(myLocation)
                .zoom(zoom)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
}


