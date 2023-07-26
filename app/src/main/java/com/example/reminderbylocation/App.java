package com.example.reminderbylocation;

import android.app.Application;

import com.example.reminderbylocation.Utils.RemindersSharedPreferences;

public class App extends Application {
    @Override
    public void onCreate() {
        RemindersSharedPreferences.init(this);
        super.onCreate();
    }
}
