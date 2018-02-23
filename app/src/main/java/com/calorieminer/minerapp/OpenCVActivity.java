package com.calorieminer.minerapp;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.calorieminer.minerapp.CustomClass.CustomDialog;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import static com.calorieminer.minerapp.CameraPreview.circles;
import static com.calorieminer.minerapp.CameraPreview.currentCameraId;
import static com.calorieminer.minerapp.CameraPreview.scalehei;
import static com.calorieminer.minerapp.CameraPreview.scalewid;
import static com.calorieminer.minerapp.CameraPreview.xscalefactor;
import static com.calorieminer.minerapp.CameraPreview.yscalefactor;

public class OpenCVActivity extends AppCompatActivity {
    private Camera mCamera;
    private CameraPreview mPreview;
    private String mFilePath;
    private File pictureFile;
    public static int statusBarHeight, finalHeight, finalWidth;
    File mediaStorageDir;
    FrameLayout preview;
    public static float ratio;
    boolean isPressedBackbutton = false;
    Bitmap backgroundBmp;

    Timer mTimer;
    long elapsedSecond = 0;
    private CustomDialog loading;

    private final static String TAG = "OpenCVActivity";

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                    Log.d(TAG, "OpenCV loaded succeffully!!");
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Fabric.with(this, new Crashlytics());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_opencv);

        // get ratio of current device
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int w = displayMetrics.widthPixels;
        int h = displayMetrics.heightPixels;

        ratio = w > h ? (float)(h)/(float) (w): (float)(w)/(float) (h);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) +
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    + ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
            } else
                init();
        } else
            init();
    }

    private void init() {
        if (!OpenCVLoader.initDebug())
        {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        }
        else
        {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }




        if (mPreview == null)
        {
            mCamera = getCameraInstance(currentCameraId);
            mPreview = new CameraPreview(OpenCVActivity.this, mCamera);

        }

        preview = findViewById(R.id.camera_preview);
        Camera.Size size = mPreview.getOptimalPreviewSize();
        float ratio = (float)size.width/size.height;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenhei = displayMetrics.heightPixels;
        int screenwid = displayMetrics.widthPixels;

        int new_width=0, new_height=0;
        if(screenwid/screenhei<ratio){
            new_width = Math.round(screenwid*ratio);
            new_height = screenwid;
        }else{
            new_width = screenwid;
            new_height = Math.round(screenwid/ratio);
        }

        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)preview.getLayoutParams();
        param.width = new_height;
        param.height = new_width;
        preview.setLayoutParams(param);
        preview.addView(mPreview);

        makeDIR();

    }

    @Override
    public void onBackPressed() {
        if (isPressedBackbutton)
            return;

        if(mPreview.detectflag){
            try {

                if (!getPackageManager()
                        .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                    exitApp("Camera not detected. App must terminate.");
                } else {

                    loading = new CustomDialog(this);
                    loading.setText(getResources().getString(R.string.loading));
                    loading.show();

                    isPressedBackbutton = true;
                    pictureFile = getOutputMediaFile();
                    if (pictureFile == null || !mediaStorageDir.exists())
                    {
                        if (loading != null)
                            loading.dismiss();
                        return;
                    }

                    mFilePath = pictureFile.getAbsolutePath();

                    calcCoordinatesForBalls();
                    mCamera.takePicture(null, null, mPicture);
                }

            } catch (Exception e) {
                Toast.makeText(this, "Error connecting to camera service", Toast.LENGTH_LONG).show();
            }


        }
        else {
            isPressedBackbutton = false;
            Toast.makeText(OpenCVActivity.this,"Ball has not been detected! Please Try again", Toast.LENGTH_LONG).show();
        }
    }

    private void exitApp(String message) {

        new AlertDialog.Builder(this,R.style.DateTImePicker)
                .setTitle("Warning")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(OpenCVActivity.this, SignInActivity.class);
                        intent.putExtra("EXIT", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();

                    }
                })

                .show();
    }

    private void calcCoordinatesForBalls() {
        if(circles != null){
            for(int i=0; i<circles.cols(); i++) {

                double[] circle = circles.get(0, i);

                float pX = (float) (circle[0] * xscalefactor);
                float pY = (float) (circle[1] * yscalefactor);
                float r = (float) (circle[2] * xscalefactor);
                if( r > 10 && r < 50 && pX-r > 10 && pX+r < scalewid - 10 &&
                        pY-r > 10 && pY + r < scalehei - 10){

                    String tag = "circle" + i + 1;
                    String rCircle = "r" + i+1 + "=" + r;
                    String xCircle = "x" + i+1 + "=" + pX;
                    String yCircle = "y" + i+1 + "=" + pY;
                    Log.d(tag, rCircle+", " + xCircle + ", " + yCircle + "\n");
                }

//                canvas.drawCircle((float) (circle[0] * xscalefactor), (float) (circle[1] * yscalefactor), 8.0f, nPaint);
            }
        }
    }

    private void makeDIR() {

        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Calorie Miner Wallet/BallDetect");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                mediaStorageDir = null;
            }
        }
    }

    private void TimerMethod() {
        elapsedSecond++;
        if (elapsedSecond >= 6)
        {
            mTimer.cancel();
            updateUI();
        }
    }

    private void updateUI() {
        Intent mIntent = new Intent(OpenCVActivity.this, CalibrationActivity.class);
        mIntent.putExtra("image_path", mFilePath );

        isPressedBackbutton = false;
        backgroundBmp = null;
        if (loading != null)
            loading.dismiss();
        startActivity(mIntent);
        finish();
    }

    @SuppressLint("StaticFieldLeak")
    public class SaveImageTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            File imageFile = new File(mFilePath);

            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                // store bitmap in storage

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                backgroundBmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);
//                byte[] bitmapdata = bos.toByteArray();
//                ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);
//
//                byte[] buf = new byte[1024];
//                int len;
//                while ((len = fis.read(buf)) > 0) {
//                    fos.write(buf, 0, len);
//                }
//                fis.close();
                backgroundBmp.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute(){
            // Show your progress bar here.
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateUI();
        }

    }


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
                Toast.makeText(OpenCVActivity.this, "Available for only portraid mode.", Toast.LENGTH_SHORT).show();
                if (loading != null)
                    loading.dismiss();
                return;
            }
//            File tempFile = new File(mediaStorageDir.getPath() + File.separator + "temp.jpg");
//            if (tempFile.exists())
//                tempFile.delete();


//                fos.write(data);
            Rect frame = new Rect();
            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            statusBarHeight = frame.top;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            finalHeight = displayMetrics.heightPixels;
            finalWidth = displayMetrics.widthPixels;

            Bitmap realImage = BitmapFactory.decodeByteArray(data, 0, data.length);
//                ExifInterface exif=new ExifInterface(pictureFile.toString());
//                if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")){
//                    realImage= rotate(realImage, 90);
//                } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
//                    realImage= rotate(realImage, 270);
//                } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
//                    realImage= rotate(realImage, 180);
//                } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")){
//                    realImage= rotate(realImage, 90);
//                }


            Bitmap scaled = Bitmap.createScaledBitmap(realImage, finalHeight, finalWidth, false);
            int w = scaled.getWidth();
            int h = scaled.getHeight();
            // Setting post rotate to 90
            Matrix mtx = new Matrix();

            if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
            {
                mtx.postRotate(90);
            } else
            {
                mtx.postRotate(270);
            }

            // Rotating Bitmap
            backgroundBmp = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);

            elapsedSecond = 0;

                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        TimerMethod();
                    }
                }, 0, 500);

//            new SaveImageTask().execute(null, null, null);

            File imageFile = new File(mFilePath);

            try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                // store bitmap in storage

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                backgroundBmp.compress(Bitmap.CompressFormat.JPEG, 70, bos);
//                byte[] bitmapdata = bos.toByteArray();
//                ByteArrayInputStream fis = new ByteArrayInputStream(bitmapdata);
//
//                byte[] buf = new byte[1024];
//                int len;
//                while ((len = fis.read(buf)) > 0) {
//                    fos.write(buf, 0, len);
//                }
//                fis.close();
                backgroundBmp.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    };

    @Override
    protected void onDestroy() {
        if (mCamera != null)
        {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        super.onDestroy();
    }

    public Camera getCameraInstance(int cameraID){
        if(mCamera != null)
            return mCamera;
        Camera c = null;
        try {
            c = Camera.open(cameraID);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return c;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1001) {
            if (grantResults.length > 0
                    && grantResults[0] == 0 && grantResults[1] == 0 && grantResults[2] == 0) {

                init();

            } else {

                Log.d("Error", "Permission not granted. App must terminate.");

            }
        }

    }

    private File getOutputMediaFile(){
        if (mediaStorageDir == null)
            return null;
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }


    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        //       mtx.postRotate(degree);
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }
}
