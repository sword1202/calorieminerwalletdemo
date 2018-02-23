package com.calorieminer.minerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;
import java.util.List;

import static com.calorieminer.minerapp.CameraPreview.currentCameraId;
import static com.calorieminer.minerapp.CameraPreview.scalehei;
import static com.calorieminer.minerapp.CameraPreview.scalewid;
import static com.calorieminer.minerapp.CameraPreview.xscalefactor;
import static com.calorieminer.minerapp.CameraPreview.yscalefactor;

public class CalibrationActivity extends AppCompatActivity implements View.OnClickListener {

    Button recalibrate_led_Button, recalibrateButton, continueButton;

    private Bitmap mImgBitmap;
    private List<MatOfPoint> contours;

    Bitmap resultimg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        recalibrateButton = findViewById(R.id.btn_recalibrate);
        recalibrateButton.setOnClickListener(this);

        recalibrate_led_Button = findViewById(R.id.btn_recalibrateled);
        recalibrate_led_Button.setOnClickListener(this);

        continueButton = findViewById(R.id.btnContinue);
        continueButton.setOnClickListener(this);

        String mFilePath = getIntent().getStringExtra("image_path");

        try{
            mImgBitmap = BitmapFactory.decodeFile(mFilePath);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        ImageView resultimage = findViewById(R.id.imageView_opencv);
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int screenwid = displayMetrics.widthPixels;
        RelativeLayout.LayoutParams params0 = (RelativeLayout.LayoutParams)resultimage.getLayoutParams();
        params0.width = (int)((double)OpenCVActivity.ratio*params0.height);
        resultimage.setLayoutParams(params0);

        createRoundedImageView(resultimage);

        try {
            resultimg = findCircle(mImgBitmap);
            resultimage.setImageBitmap(resultimg);
//            ScreenshotUtils.savePic(ScreenshotUtils.takeScreenShot(ResultActivity.this), mFilePath);

            FileOutputStream fos = new FileOutputStream(mFilePath);
            resultimg.compress(Bitmap.CompressFormat.PNG, 30, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d("*******", "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private void createRoundedImageView(ImageView resultimage) {
        resultimage.setBackgroundResource(R.drawable.tags_rounded_corners);

        GradientDrawable drawable = (GradientDrawable) resultimage.getBackground();
        drawable.setColor(Color.parseColor("#296d33"));
    }

    @Override
    public void onBackPressed() {
        showAlert();
    }

    private void showAlert() {

        new AlertDialog.Builder(this,R.style.DateTImePicker)
                .setTitle(null)
                .setMessage(R.string.back_button_message)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(CalibrationActivity.this, SignInActivity.class);
                        intent.putExtra("EXIT", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })

                .show();

    }

    public Bitmap findCircle(Bitmap image) throws Exception {

        Mat imgOriginal = new Mat();

        Bitmap bmp32 = image.copy(Bitmap.Config.ARGB_8888, true);

        Utils.bitmapToMat(bmp32, imgOriginal);
        Imgproc.cvtColor(imgOriginal, imgOriginal, Imgproc.COLOR_RGB2RGBA);
        imgOriginal.convertTo(imgOriginal, 0);

        StringBuilder s = new StringBuilder();

        for(int i=0; i<CameraPreview.circles.cols(); i++) {

            double[] circle = CameraPreview.circles.get(0, i);

            float pX = (float) (circle[0] * xscalefactor);
            float pY = (float) (circle[1] * yscalefactor);

            if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                pY = (float) (scalehei - pY);
                pX = (float) (scalewid - pX);
            }


            float r = (float) (circle[2] * xscalefactor);
            if( r > 10 && r < 50 && pX-r > 10 && pX+r < scalewid - 10 &&
                    pY-r > 10 && pY + r < scalehei - 10){

                String tag = "circle" + i + 1;
                String rCircle = "r" + i+1 + "=" + r;
                String xCircle = "x" + i+1 + "=" + pX;
                String yCircle = "y" + i+1 + "=" + pY;
                String m = " Tracker" + " : " + Math.floor(pX)  + ", " + Math.floor(pY) + "\n\n";
                s.append(m);
                Log.d(tag, rCircle+", " + xCircle + ", " + yCircle + "\n");
                Imgproc.circle(imgOriginal, new Point(pX, pY+OpenCVActivity.statusBarHeight/2), (int)(r + 2), new Scalar(255, 0, 0,1), 10);
            }

//                canvas.drawCircle((float) (circle[0] * xscalefactor), (float) (circle[1] * yscalefactor), 8.0f, nPaint);
        }


        TextView result = findViewById(R.id.coordinates);
        result.setText(s.toString());


        Imgproc.cvtColor(imgOriginal, imgOriginal, Imgproc.COLOR_RGBA2RGB);
        Bitmap bmp;
        bmp = Bitmap.createBitmap(imgOriginal.cols(), imgOriginal.rows(),
                Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgOriginal, bmp);

        return bmp;
    }

    String convertNumberToChar (int i)
    {
        String mStr = "One";
        switch (i){
            case 2:
                mStr = "Two";
                break;
            case 3:
                mStr = "Three";
                break;
        }

        return mStr;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_recalibrateled:
                recalibrate();
                break;
            case R.id.btn_recalibrate:
                recalibrate();
                break;
            case R.id.btnContinue:
                fcontinue();
                break;
        }
    }

    private void fcontinue() {
        Intent intent = new Intent(CalibrationActivity.this, CameraActivity.class);
        startActivity(intent);
        finish();
    }

    private void recalibrate() {
        //select rear or front camera

        int cameraid = findFrontFacingCamera();
        if (cameraid >= 0)
        {
            new AlertDialog.Builder(this,R.style.DateTImePicker)
                    .setTitle(null)
                    .setMessage(R.string.camera_use)
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                            gotoOpenCVActivity();

                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                            gotoOpenCVActivity();
                        }
                    })

                    .show();
        } else
        {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            gotoOpenCVActivity();
        }

    }

    private void gotoOpenCVActivity() {
        Intent intent = new Intent(CalibrationActivity.this, OpenCVActivity.class);
        startActivity(intent);
        finish();
    }
}
