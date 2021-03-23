package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

public class ServiceRestart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        boolean stop = prefs.getBoolean("stop", false);
        if(stop){
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try{
                context.startForegroundService(new Intent(context, MyService.class));
                //context.startForegroundService(new Intent(context, GlobalTouchService.class));
            }catch (Exception ignored){

            }
        }
    }
}
