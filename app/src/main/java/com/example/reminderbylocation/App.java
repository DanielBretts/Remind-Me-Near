package com.example.reminderbylocation;

import android.app.Application;
import android.util.Log;

public class App extends Application {
    @Override
    public void onCreate() {
        RemindersSharedPreferences.init(this);
        super.onCreate();
    }
}
