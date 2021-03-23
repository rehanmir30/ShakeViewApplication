package com.example.myapplication;

import android.accessibilityservice.GestureDescription;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import static android.content.Context.WINDOW_SERVICE;

public class AlarmService extends BroadcastReceiver {
    public AlarmService() {
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        Long lastShake = prefs.getLong("lastShake", System.currentTimeMillis()/1000);
        int time = prefs.getInt("time", 60) / 2;
        boolean showing = prefs.getBoolean("showing", false);
        boolean stop = prefs.getBoolean("stop", false);

        if(stop){
            return;
        }
        Long currentTS = System.currentTimeMillis()/1000;

        if(currentTS - lastShake > time && !showing){
            context.startForegroundService(new Intent(context, BlackScreenService.class));
        }


        Intent myIntent = new Intent(context, AlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,  0, myIntent, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis() + time * 1000, pendingIntent);

    }

}