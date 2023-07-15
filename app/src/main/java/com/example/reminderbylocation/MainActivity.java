package com.example.reminderbylocation;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.reminderbylocation.Fragments.MapViewFragment;
import com.example.reminderbylocation.Fragments.RemindersFragment;
import com.example.reminderbylocation.databinding.ActivityMainBinding;
import com.example.reminderbylocation.services.LocationService;

public class MainActivity extends AppCompatActivity {
    private MapViewFragment mapFragment;

    private RemindersFragment remindersFragment;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IsServiceLocation.setIsTracing(true);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        checkPermission();
        initView();
        startLocationService();
    }


    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_FRAME_map);
        if (fragment instanceof MapViewFragment) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_FRAME_map, remindersFragment)
                    .commit();
        }else if(fragment instanceof RemindersFragment){
            super.onBackPressed();
        }
    }

    private void initView() {
        remindersFragment = new RemindersFragment();
        mapFragment = new MapViewFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.main_FRAME_map, remindersFragment).commit();
        binding.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationService();
                IsServiceLocation.setIsTracing(false);
            }
        });
        binding.start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationService();
                IsServiceLocation.setIsTracing(true);
            }
        });

    }

    private void checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager != null){
            for(ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)){
                if(LocationService.class.getName().equals(service.service.getClassName())){
                    if(service.foreground)
                        return true;
                }
            }
            return false;
        }
        return false;
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(),LocationService.class);
            intent.setAction(Constants.START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this,"Location Started!",Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService(){
        if(isLocationServiceRunning()){
            Intent intent = new Intent(getApplicationContext(),LocationService.class);
            intent.setAction(Constants.STOP_LOCATION_SERVICE);
            stopService(intent);
            Toast.makeText(this,"Location Stopped!",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Switch between fragments by inflating the activity's frame
     *
     * @param  fragmentClass   inflated the frame with this fragment
     */
    public void replaceFragments(Class fragmentClass) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_FRAME_map, fragment)
                .commit();
    }
}