package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.watermark.androidwm.WatermarkBuilder;
import com.watermark.androidwm.bean.WatermarkImage;
import com.watermark.androidwm.bean.WatermarkText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.graphics.Typeface.BOLD;
import static com.example.myapplication.BlackScreenService.splitToComponentTimes;

public class ShareActivity extends AppCompatActivity {


    Button sharetest;
    ImageView viewImage;
    // EditText description;

    Uri imageUri;
    Boolean button;
    int shakes;
    long passed;

    private File output = null;

    String paths;
    String temp2;

    String picname;

    private String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.ACCESS_FINE_LOCATION", "android.permission.READ_PHONE_STATE", "android.permission.SYSTEM_ALERT_WINDOW", "android.permission.CAMERA"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);


        button = true;


        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        sharetest = findViewById(R.id.sharetest);
        viewImage = findViewById(R.id.viewImage);


        SharedPreferences prefs = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        shakes = prefs.getInt("totalShakes", 0);

        long startTime = prefs.getLong("started", System.currentTimeMillis() / 1000);
        passed = System.currentTimeMillis() / 1000 - startTime;
        int requestCode = 200;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }


        sharetest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button == true)
                    selectImage();
                else if (button == false) {

                    String type = "image/*";
                    // String filename = "/myPhoto.jpg";
                    String mediaPath = Environment.getExternalStorageDirectory() + picname;

                    imageUri = Uri.parse(paths);

                    File media = new File(mediaPath);
                    Uri uri = Uri.fromFile(media);
                   // Toast.makeText(getApplicationContext(), mediaPath, Toast.LENGTH_LONG).show();
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.putExtra(Intent.EXTRA_STREAM, imageUri);
                    share.putExtra(Intent.EXTRA_TEXT,"Number of shakes: " + shakes + "\nTime spent: " + passed + "\nGet the app at www.google.com");
                    share.setType(type);
                    startActivity(Intent.createChooser(share, "Share to"));


//                    imageUri = Uri.parse(paths);
//                    Intent intent = new Intent(Intent.ACTION_SEND);
//                    intent.putExtra(Intent.EXTRA_TEXT, "Number of shakes: " + shakes + "\nTime spent: " + passed + "\nGet the app at www.google.com");
//                    intent.putExtra(Intent.EXTRA_STREAM, imageUri);
//                    intent.setPackage("com.instagram.android");
//                    intent.setType("*/*");
//                    startActivity(intent);
                    //startActivity(Intent.createChooser(intent, "ShareVia"));
                }
            }
        });

    }

    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(ShareActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {

                //TODO: Work on Camera picture

                button = false;
                sharetest.setText("Share");
                Intent i = new Intent(Intent.ACTION_VIEW);
                Bitmap captureImage = (Bitmap) data.getExtras().get("data");

                temp2 = MediaStore.Images.Media.insertImage
                        (getApplicationContext().getContentResolver(),
                                captureImage, "IMG_" + System.currentTimeMillis(), null);

                Uri temp = Uri.parse(temp2);
                try {
                    Bitmap srcBmp = BitmapFactory.decodeStream(getApplicationContext().getContentResolver().openInputStream(temp), null, null);


                    //Watermark text here
                    WatermarkText watermarkText = new WatermarkText("Number of Shakes : " + String.valueOf(shakes) + " Total Session Time : " + String.valueOf(passed))
                            .setPositionY(0.9)              //Position of text from top to bottom (Value range 0 to 1)
                            .setPositionX(0)                //Position of text from left to right (Value range 0 to 1)
                            .setTextAlpha(250)              //Opacity of text (Value range 0 to 250)
                            .setBackgroundColor(Color.BLACK)//Setting background of text
                            .setTextColor(Color.WHITE)      //Changing text color
                            .setTextSize(12);               //Changing text size

                    //Watermark Logo here
                    Bitmap watermarkBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);

                    //Image water mark
                    WatermarkImage watermarkImage = new WatermarkImage(watermarkBitmap)
                            .setPositionY(0.7)              //Position of text from top to bottom (Value range 0 to 1)
                            .setPositionX(0)                //Position of text from left to right (Value range 0 to 1)
                            .setSize(0.5)                   //Fixing size of image
                            .setImageAlpha(250);            //changing Opacity/trancparency

                    WatermarkBuilder.create(this, srcBmp)       //Adding watermarks on selected images
                            .loadWatermarkText(watermarkText)
                            .loadWatermarkImage(watermarkImage)
                            .getWatermark()
                            .setToImageView(viewImage);

                    Bitmap bitmap = WatermarkBuilder
                            .create(this, captureImage)
                            .loadWatermarkText(watermarkText)
                            .loadWatermarkImage(watermarkImage)
                            .getWatermark()
                            .getOutputImage();

                    paths = MediaStore.Images.Media.insertImage
                            (getApplicationContext().getContentResolver(),
                                    bitmap, "IMG_" + System.currentTimeMillis(), null);
                    Uri uri = Uri.parse(paths);
                    picname = "IMG_" + System.currentTimeMillis();
                   // Toast.makeText(getApplicationContext(), paths, Toast.LENGTH_LONG).show();

                    //title was temp before

                    //viewImage.setImageBitmap(srcBmp);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }


            } else if (requestCode == 2) {

                //TODO: Work on Gallery Image

                sharetest.setText("Share");
                button = false;
                imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    Bitmap watermarkBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);

                    Bitmap scaledimg = Bitmap.createScaledBitmap(bitmap, 200, 200, true);

                    WatermarkText watermarkText = new WatermarkText("Number of Shakes : " + String.valueOf(shakes) + "Total Session Time : " + String.valueOf(passed))
                            .setPositionY(0.9)                  //Position of text from top to bottom (Value range 0 to 1)
                            .setPositionX(0)                    //Position of text from left to Right (Value range 0 to 1)
                            .setTextAlpha(250)                  //Changing Opacity
                            .setBackgroundColor(Color.BLACK)    //Setting background of text
                            .setTextColor(Color.WHITE)          //Changing text color
                            .setTextSize(12);                   //changing Text size

                    WatermarkImage watermarkImage = new WatermarkImage(watermarkBitmap)
                            .setPositionY(0.7)                  //Position of image from top to bottom (Value range 0 to 1)
                            .setPositionX(0)                    //Position of image from left to Right (Value range 0 to 1)
                            .setSize(0.5)                       //Changing Image size
                            .setImageAlpha(250);                //Change opacity

                    WatermarkBuilder.create(this, bitmap)
                            .loadWatermarkText(watermarkText)
                            .loadWatermarkImage(watermarkImage)
                            .getWatermark()
                            .setToImageView(viewImage);

                    Bitmap bitmap2 = WatermarkBuilder
                            .create(this, bitmap)
                            .loadWatermarkText(watermarkText)
                            .loadWatermarkImage(watermarkImage)
                            .getWatermark()
                            .getOutputImage();
                    paths = MediaStore.Images.Media.insertImage
                            (getApplicationContext().getContentResolver(),
                                    bitmap2, "IMG_" + System.currentTimeMillis(), null);

//                          Toast.makeText(getApplicationContext(), String.valueOf(imageUri), Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

}