package com.example.zhongzhoujianshe.ins.Carema;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity ;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.graphics.Matrix;
import com.example.zhongzhoujianshe.ins.ImageProcess.BitmapStore;
import com.example.zhongzhoujianshe.ins.ImageProcess.ImageProcessing;
import com.example.zhongzhoujianshe.ins.PostActivity;
import com.example.zhongzhoujianshe.ins.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class EditPhotoActivity extends AppCompatActivity  {
    private ImageView imageView;
    private Bitmap rawBitmap;
    private Bitmap newBitmap;
    private Bitmap scaledBitmap;


    private Button btnFilter1 = null;
    private Button btnFilter2 = null;
    private Button btnFilter3 = null;
    private Button btnCrop = null;
    private SeekBar seekBarContrast = null;
    private SeekBar seekBarBrightness = null;
    private TextView textview_contrast = null;
    private TextView textview_brightness = null;

    static int progress_contrast = 0;
    static int progress_brightness = 0;
    static int FILTER_STATIC = 0; // 0 represents no filters on, 1 one filter applied.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_photo);

        imageView = (ImageView)findViewById(R.id.imageview_edit);
        Intent intent = getIntent();
        if (intent != null) {
            Matrix matrix = new Matrix();
            rawBitmap = BitmapStore.getBitmap();
            matrix.setRotate(90);//turn 90
            Bitmap dstbmp=Bitmap.createBitmap(rawBitmap,0,0,rawBitmap.getWidth(), rawBitmap.getHeight(),matrix,true);
            imageView.setImageBitmap(dstbmp);
            newBitmap = dstbmp;
        }

        // Filters
        btnFilter3 = (Button) findViewById(R.id.button_filter3);
        btnFilter1 = (Button) findViewById(R.id.button_filter1);
        btnFilter2 = (Button) findViewById(R.id.button_filter2);
        btnCrop = (Button) findViewById(R.id.button_crop);


        btnFilter3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FILTER_STATIC == 1) {
                    newBitmap = ImageProcessing.doColorFilter(rawBitmap, 0.5, 0.5, 0.5);
                    imageView.setImageBitmap(newBitmap);
                } else {
                    newBitmap = ImageProcessing.doColorFilter(newBitmap, 0.5, 0.5, 0.5);
                    imageView.setImageBitmap(newBitmap);
                    FILTER_STATIC = 1;
                }
            }
        });

        btnFilter1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FILTER_STATIC == 1) {
                    newBitmap = ImageProcessing.applySaturationFilter(rawBitmap, 1);
                    imageView.setImageBitmap(newBitmap);
                } else {
                    newBitmap = ImageProcessing.applySaturationFilter(newBitmap, 1);
                    imageView.setImageBitmap(newBitmap);
                    FILTER_STATIC = 1;
                }
            }
        });

        btnFilter2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FILTER_STATIC == 1) {
                    newBitmap = ImageProcessing.engrave(rawBitmap);
                    imageView.setImageBitmap(newBitmap);
                } else {
                    newBitmap = ImageProcessing.engrave(newBitmap);
                    imageView.setImageBitmap(newBitmap);
                    FILTER_STATIC = 1;
                }
            }
        });

        // Contrast & Brightness
        seekBarContrast = (SeekBar) findViewById(R.id.seekbar_contrast);
        seekBarBrightness = (SeekBar) findViewById(R.id.seekbar_brightness);
        textview_contrast = (TextView) findViewById(R.id.text_contrast);
        textview_brightness = (TextView) findViewById(R.id.textview_brightness);


        // Listener for seekbar object
        seekBarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textview_contrast.setText("Contrast: " + String.valueOf(progress));
                newBitmap = ImageProcessing.changeBitmapContrastBrightness(rawBitmap,
                        (float) progress/10f, (float) 5.12*(progress_brightness-50f));
                imageView.setImageBitmap(newBitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                progress_contrast = seekBarContrast.getProgress();
            }
        });

        seekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textview_brightness.setText("Brightness: " + String.valueOf(progress));

                newBitmap = ImageProcessing.changeBitmapContrastBrightness(rawBitmap,
                        (float) progress_contrast/10f, (float) 5.12*(progress -50f));
                imageView.setImageBitmap(newBitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                progress_brightness = (seekBarBrightness.getProgress());
            }
        });


        //crop button
        btnCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapStore.setBitmap(newBitmap);

                Intent intent = new Intent(EditPhotoActivity.this, CropActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_next) {
            startNext();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startNext() {
        scaledBitmap = Bitmap.createScaledBitmap(newBitmap, 640, 640, false);

        // save the modified picture
        File storagePath = new File(Environment.getExternalStorageDirectory()
                + "/DCIM/100ANDRO/");
        storagePath.mkdirs();

        File myImage = new File(storagePath, Long.toString(System.currentTimeMillis())
                + "_mod.jpg");
        try {
            FileOutputStream out = new FileOutputStream(myImage);
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            out.flush();
            out.close();
        } catch(FileNotFoundException e) {
            Log.d("In Saving File", e + "");
        } catch(IOException e) {
            Log.d("In Saving File", e + "");
        }

        // Pass the new image to the next post view
        Intent intent = new Intent();
        intent.putExtra("post_img", myImage.toString());
        intent.setClass(EditPhotoActivity.this, PostActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
