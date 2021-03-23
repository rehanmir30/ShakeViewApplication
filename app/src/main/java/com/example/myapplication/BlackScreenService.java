package com.example.myapplication;

import android.accessibilityservice.GestureDescription;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;

import static android.app.Activity.RESULT_OK;
import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

public class BlackScreenService extends Service {


    public BlackScreenService() {
    }

    private WindowManager mWindowManager;
    private View mChatHeadView;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_service";
            String CHANNEL_NAME = "My Background Service";

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setCategory(Notification.CATEGORY_SERVICE).setPriority(PRIORITY_MIN).build();

            startForeground(101, notification);
        }

        if(intent != null){
            if(intent.getExtras() == null){
                mChatHeadView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_black, null);

                LinearLayout mLayout = mChatHeadView.findViewById(R.id.layout);
                ImageView mSettings = mChatHeadView.findViewById(R.id.settings);
                TextView mShakes = mChatHeadView.findViewById(R.id.sessionShakes);
                TextView mTime = mChatHeadView.findViewById(R.id.sessionTime);
                ImageView share=mChatHeadView.findViewById(R.id.share);

                Boolean button=true;


                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());



                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (button == true){

                            stopService(new Intent(getApplicationContext(), MyService.class));
                            Intent dialogIntent = new Intent(getApplicationContext(), ShareActivity.class);
                            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(dialogIntent);
                            mWindowManager.removeView(mChatHeadView);
                            clearView();

                        }

                    }

                });


                mSettings.setOnClickListener(v -> {
                    Intent dialogIntent = new Intent(this, MainActivity.class);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(dialogIntent);

                    mWindowManager.removeView(mChatHeadView);
                    clearView();
                });
                mLayout.setOnClickListener(v -> {
                    mWindowManager.removeView(mChatHeadView);
                    clearView();
                });


                SharedPreferences prefs = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
                int totalShakes = prefs.getInt("totalShakes", 0);
                mTime.setText("Total Session Shakes: " + totalShakes);

                final Handler ha=new Handler();
                ha.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        //call function
                        SharedPreferences prefs = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
                        long startTime = prefs.getLong("started", System.currentTimeMillis()/1000);

                        long passed =  System.currentTimeMillis()/1000 - startTime;

                        mShakes.setText("Total Session Time: " + splitToComponentTimes(passed));
                        ha.postDelayed(this, 1000);
                    }
                }, 0);

                //Add the view to the window.
                final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);

                //Specify the chat head position
                //Initially view will be added to top-left corner
                params.gravity = Gravity.TOP | Gravity.LEFT;
                params.x = 0;
                params.y = 0;

                //Add the view to the window
                mWindowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
                mWindowManager.addView(mChatHeadView, params);
                SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).edit();
                editor.putBoolean("showing", true);
                editor.apply();
                return super.onStartCommand(intent, flags, startId);
            }
            boolean state = intent.getExtras().getBoolean("kill", false);
            if(state){
                if(mChatHeadView != null && mWindowManager != null){
                    try{
                        mWindowManager.removeView(mChatHeadView);
                    }catch(Exception e){
                        return super.onStartCommand(intent, flags, startId);
                    }
                }

                clearView();
                return super.onStartCommand(intent, flags, startId);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                stopForeground(true);
            } else {
                stopSelf();
            }
        }

        return super.onStartCommand(intent, flags, startId);

    }



    public static String splitToComponentTimes(long longVal)
    {
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};

        String minStr, hourStr, secStr;
        if(mins < 10){
            minStr = "0" + mins;
        }else{
            minStr = String.valueOf(mins);
        }
        if(secs < 10){
            secStr = "0" + secs;
        }else{
            secStr = String.valueOf(secs);
        }
        return minStr + ":" + secStr;
    }
    void clearView(){
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("SETTINGS", Context.MODE_PRIVATE).edit();
        editor.putBoolean("showing", false);
        editor.putLong("lastShake", System.currentTimeMillis()/1000);
        editor.apply();
    }
    @Override
    public void onCreate() {
        super.onCreate();

    }


}
