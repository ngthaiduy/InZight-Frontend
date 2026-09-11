package com.example.inzightapp.utils;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtils {

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String formatTime(LocalDateTime time) {
        if (time == null) return "";
        Duration diff = Duration.between(time, LocalDateTime.now());
        long minutes = diff.toMinutes();
        long hours = diff.toHours();
        long days = diff.toDays();

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours < 24) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        return days + " day" + (days > 1 ? "s" : "") + " ago";
    }
}
