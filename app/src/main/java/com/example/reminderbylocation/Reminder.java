package com.example.reminderbylocation;

import com.example.reminderbylocation.Fragments.RemindersAdapter;

import java.util.Random;

public class Reminder {
    SimpleLocation location;
    String title;
    String notes;

    String featureName;
    int radius;
    private int id;

    public Reminder(SimpleLocation location, String title, String notes, String featureName, int radius) {
        this.location = location;
        this.title = title;
        this.notes = notes;
        this.radius = radius;
        this.featureName = featureName;
        this.id = new Random().nextInt();
    }

    public SimpleLocation getLocation() {
        return location;
    }

    public void setLocation(SimpleLocation location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "location=" + location +
                ", title='" + title + '\'' +
                ", notes='" + notes + '\'' +
                ", featureName='" + featureName + '\'' +
                ", radius=" + radius +
                '}';
    }

    public int getId() {
        return this.id;
    }
}
