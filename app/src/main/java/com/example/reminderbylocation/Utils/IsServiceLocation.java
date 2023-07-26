package com.example.reminderbylocation.Utils;

public class IsServiceLocation {
    private static boolean tracing;

    public static boolean isTracing() {
        return tracing;
    }

    public static void setIsTracing(boolean tracing) {
        IsServiceLocation.tracing = tracing;
    }
}
