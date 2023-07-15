package com.example.reminderbylocation;

public class IsServiceLocation {
    private static boolean tracing;

    public static boolean isTracing() {
        return tracing;
    }

    public static void setIsTracing(boolean tracing) {
        IsServiceLocation.tracing = tracing;
    }
}
