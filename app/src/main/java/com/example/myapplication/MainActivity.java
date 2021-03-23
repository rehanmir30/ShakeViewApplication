package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 2323;

    Button mStartService;
    Button mStopService;
    ImageButton mHelp;
    TextView mInfoStart;
    TextView mInfoStop;
    ImageView mImgLogo;



    private BottomSheetBehavior sheetBehavior;
    private CardView bottom_sheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStopService = findViewById(R.id.stop_service);
        mStartService = findViewById(R.id.start_service);
        mInfoStart = findViewById(R.id.textInfoStart);
        mInfoStop = findViewById(R.id.textInfoStop);
        mImgLogo = findViewById(R.id.imgLogo);
        mImgLogo.setImageResource(R.drawable.logo);
        mHelp = findViewById(R.id.imageButton);


        mHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://googlee.com";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        RequestPermission();
        SeekBarListener();
        StartServiceListener();
        StopServiceListener();
        ExitListener();
        sessionShakes();
        castListener();


        getCurrentState();
    }

    private void castListener() {
        Button mCast = findViewById(R.id.cast);
        mCast.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_CAST_SETTINGS);
            startActivity(intent);
        });

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void getCurrentState() {
        if (isMyServiceRunning(MyService.class)) {
            mStopService.setVisibility(View.VISIBLE);
            mStartService.setVisibility(View.GONE);
            mInfoStart.setVisibility(View.GONE);
            mInfoStop.setVisibility(View.VISIBLE);

        } else {
            mStopService.setVisibility(View.GONE);
            mStartService.setVisibility(View.VISIBLE);
            mInfoStart.setVisibility(View.VISIBLE);
            mInfoStop.setVisibility(View.GONE);
        }

    }

    private void sessionShakes() {
        TextView mSessionShakes = findViewById(R.id.sessionShakes);
        TextView mSessionTime = findViewById(R.id.sessionTime);

        final Handler haTime = new Handler();
        haTime.postDelayed(new Runnable() {

            @Override
            public void run() {
                //call function
                SharedPreferences prefs = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
                long startTime = prefs.getLong("started", System.currentTimeMillis() / 1000);

                long passed = System.currentTimeMillis() / 1000 - startTime;

                mSessionShakes.setText("Total Session Time: " + splitToComponentTimes(passed));
                getCurrentState();
                haTime.postDelayed(this, 1000);
            }
        }, 0);

        final Handler haShakes = new Handler();
        haShakes.postDelayed(new Runnable() {
            @Override
            public void run() {
                //call function
                SharedPreferences prefs = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
                int totalShakes = prefs.getInt("totalShakes", 0);
                mSessionTime.setText("Total Session Shakes: " + totalShakes);
                getCurrentState();
                haShakes.postDelayed(this, 100);
            }
        }, 0);

    }

    public static String splitToComponentTimes(long longVal) {
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours, mins, secs};

        String minStr, hourStr, secStr;
        if (mins < 10) {
            minStr = "0" + mins;
        } else {
            minStr = String.valueOf(mins);
        }
        if (secs < 10) {
            secStr = "0" + secs;
        } else {
            secStr = String.valueOf(secs);
        }
        return minStr + ":" + secStr;
    }


    private void ExitListener() {
        Button mExit = findViewById(R.id.exit);
        mExit.setOnClickListener(v -> {
            finish();
            System.exit(0);
        });
    }


    private void StartServiceListener() {
        mStartService.setOnClickListener(v -> {


            if (!Settings.canDrawOverlays(getApplicationContext())) {
                RequestPermission();
                return;
            }
            SharedPreferences.Editor editor = getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();
            editor.putBoolean("showing", false);
            editor.putInt("totalShakes", 0);
            editor.putLong("lastShake", System.currentTimeMillis() / 1000);
            editor.putLong("started", System.currentTimeMillis() / 1000);
            editor.putBoolean("stop", false);

            editor.apply();

            Intent broadcastIntent = new Intent(this, ServiceRestart.class);
            sendBroadcast(broadcastIntent);

            Intent myIntent = new Intent(getApplicationContext(), AlarmService.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            SharedPreferences prefs = getSharedPreferences("SETTINGS", MODE_PRIVATE);
            int time = prefs.getInt("time", 60);
            alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis() + time * 1000, pendingIntent);
        });
    }

    private void StopServiceListener() {
        mStopService.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();

            editor.putBoolean("stop", true);
            editor.remove("started");
            editor.apply();
            stopService(new Intent(this, AlarmService.class));
            stopService(new Intent(this, BlackScreenService.class));
            stopService(new Intent(this, MyService.class));
            stopService(new Intent(this, ServiceRestart.class));
        });
    }

    private void SeekBarListener() {
        SharedPreferences prefs = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        int force = prefs.getInt("force", 10);
        int time = prefs.getInt("time", 10);

        SeekBar mSeekBar = findViewById(R.id.seekBar);
        TextView mTextForce = findViewById(R.id.textForce);
        SeekBar mSeekBarTime = findViewById(R.id.seekBar_time);
        TextView mTextForceTime = findViewById(R.id.textTime);

        mTextForce.setText("Shake Sentivity: " + force);
        mSeekBar.setProgress(force);
        mTextForceTime.setText("Idle Time Max (seconds): " + time);
        mSeekBarTime.setProgress(time);


        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextForce.setText("Shake Sensitivity: " + progress);
                SharedPreferences.Editor editor = getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();
                editor.putInt("force", progress);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mSeekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextForceTime.setText("Idle Time Max (seconds): " + progress);
                SharedPreferences.Editor editor = getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();
                editor.putInt("time", progress);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    BottomSheetDialog dialog;

    private void RequestPermission() {
        // Check if Android M or higher
        // Show alert dialog to the user saying a separate permission is needed
        // Launch the settings activity if the user prefers
        if (Settings.canDrawOverlays(getApplicationContext())) {
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.fragment_bottom_sheet_dialog, null);
        dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.show();

        LinearLayout overlayClick = view.findViewById(R.id.overlay_click);
        overlayClick.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        });


    }



    @Override
    protected void onResume() {
        super.onResume();

        SeekBarListener();
        StartServiceListener();
        StopServiceListener();
        ExitListener();
        sessionShakes();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds options to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        dialog.dismiss();
//    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dialog.dismiss();
    }
}


