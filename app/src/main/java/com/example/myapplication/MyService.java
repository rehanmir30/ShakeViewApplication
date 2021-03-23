package com.example.myapplication;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.PrintWriter;
import java.net.Socket;

public class MyService extends Service{
    private SensorManager mSensorManager;

    private ShakeEventListener mSensorListener;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeEventListener(getApplicationContext());
        mSensorListener.setOnShakeListener(() -> {

            int i = 1;

            SharedPreferences prefs = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
            int totalShakes = prefs.getInt("totalShakes", 0);
            totalShakes++;

            SharedPreferences.Editor editor = getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();
            editor.putLong("lastShake", System.currentTimeMillis()/1000);
            editor.putInt("totalShakes", totalShakes );
            editor.apply();

            /*Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));*/


            Intent myIntent = new Intent(getApplicationContext(), BlackScreenService.class);
            myIntent.putExtra("kill", true);
            getApplicationContext().startForegroundService(myIntent);

            /*Intent intent = new Intent("kill");
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);*/
        });
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent(this, ServiceRestart.class);

        sendBroadcast(broadcastIntent);
    }


}
