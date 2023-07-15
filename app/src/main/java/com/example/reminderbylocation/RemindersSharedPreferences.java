package com.example.reminderbylocation;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RemindersSharedPreferences {
    public static final String DB_FILE = "DB_FILE";
    private static RemindersSharedPreferences instance = null;
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;

    private RemindersSharedPreferences(Context context) {
        preferences = context.getSharedPreferences(DB_FILE,context.MODE_PRIVATE);
    }

    public static void init(Context context){
        if(instance == null)
            instance = new RemindersSharedPreferences(context);
    }

    public static RemindersSharedPreferences getInstance(){
        return instance;
    }


    public void setList(List<Reminder> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor = preferences.edit();
        editor.putString(DB_FILE, json);
        editor.commit();
    }

    public ArrayList<Reminder> getList() {
        ArrayList<Reminder> list = null;
        String serializedObject = preferences.getString(DB_FILE, null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Reminder>>() {
            }.getType();
            list = gson.fromJson(serializedObject, type);
        }
        return list==null ? new ArrayList<Reminder>() : list;
    }

}
