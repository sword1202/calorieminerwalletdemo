package com.calorieminer.minerapp;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.calorieminer.minerapp.CustomClass.AppConstants;
import com.calorieminer.minerapp.CustomClass.RemoteConfigParam;
import com.calorieminer.minerapp.CustomClass.SquareSurfaceView;

import java.io.File;
import java.io.IOException;

public class VideoRecorder extends AppCompatActivity implements SurfaceHolder.Callback {

    SquareSurfaceView cameraView;
    MediaRecorder recorder;
    SurfaceHolder holder;
    static boolean isFrontCamera;
    public static int width, height;

    boolean recording = false;
    static int MEDIA_TYPE_VIDEO = 1;

    ToggleButton tbStartRecord, tbCameraSwitch;
    TextView timerStart;

    String videoPath;
    Uri fileUri;

    public static String appName;

    Camera mCamera;
    CountDownTimer timer;
    CamcorderProfile profile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_recorder);

        init();

        buttonClickEvent();

    }

    private void init() {

        appName = getResources().getString(R.string.app_name);

        cameraView = findViewById(R.id.CameraView);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        tbStartRecord = findViewById(R.id.tbStartRecord);
        tbCameraSwitch = findViewById(R.id.tbCameraSwitch);
        timerStart = findViewById(R.id.timerStart);
        String startTime = "00:00";
        timerStart.setText(startTime);
    }

    private void buttonClickEvent()
    {
        tbStartRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (recording) {
                    stopAndSend();
                } else {
                    final long max_recording_time = RemoteConfigParam.getInstance(VideoRecorder.this).getMax_recording_time() * 60000;
                    timer = new CountDownTimer(max_recording_time, 1000) { // 20minutes= 1200,000

                        public void onTick(long millisUntilFinished) {
                            long elapsedTime = max_recording_time - millisUntilFinished;
                            int minutes = (int)(elapsedTime/(1000*60));
                            int seconds = (int) (elapsedTime/1000)%60;

                            String displayMin, displaySecond, displayResult;

                            displayMin = "" + minutes;
                            if (minutes < 10)
                                displayMin = "0" + minutes;

                            displaySecond = "" + seconds;
                            if (seconds < 10)
                                displaySecond = "0" + displaySecond;

                            displayResult = displayMin + ":" + displaySecond;

                            timerStart.setText(displayResult);

                        }

                        public void onFinish() {
                            stopAndSend();
                        }
                    }.start();
                    recording = true;
                    try {
                        recorder.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        tbCameraSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (Camera.getNumberOfCameras() >= 2) {

                    if (isChecked) {
                        if (recording) {
                            Toast.makeText(VideoRecorder.this, "Stop recording before switch", Toast.LENGTH_SHORT).show();
                            tbCameraSwitch.setChecked(false);
                            tbCameraSwitch.setChecked(true);
                        } else {
                            releaseMediaRecorder();

                            if (mCamera != null) {
                                mCamera.stopPreview();
                                mCamera.setPreviewCallback(null);
                                mCamera.release();
                                mCamera = null;
                                //  holder.removeCallback(MediaRecorderRecipe.this);
                                mCamera = null;
                            }

                            if (timer != null) {
                                timer.cancel();
                            }

                            try {
                                initRecorder(0);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {

                        if (recording) {
                            Toast.makeText(VideoRecorder.this, "Stop recording before switch", Toast.LENGTH_SHORT).show();
                            tbCameraSwitch.setChecked(false);
                            tbCameraSwitch.setChecked(true);
                        } else {
                            releaseMediaRecorder();

                            if (mCamera != null) {
                                mCamera.stopPreview();
                                mCamera.setPreviewCallback(null);
                                mCamera.release();
                                mCamera = null;
                                mCamera = null;
                            }

                            if (timer != null) {
                                timer.cancel();
                            }

                            try {
                                initRecorder(2);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    tbCameraSwitch.setChecked(false);
                    Toast.makeText(VideoRecorder.this, "Sorry, you don't have front camera", Toast.LENGTH_SHORT).show();
                    tbCameraSwitch.setChecked(true);
                }
            }
        });
    }

    private void stopAndSend()
    {
        stopEveryThingIsPlayed();
        Log.e("videoPath @ media", videoPath);

        Intent intent = new Intent(getApplicationContext(), VideoActivity.class);
        intent.putExtra("videoPath", videoPath);
        intent.putExtra(getResources().getString(R.string.bundle_fps_value), RemoteConfigParam.getInstance(VideoRecorder.this).getDefault_fps());
//        intent.putExtra("videoUri", fileUri);

        startActivity(intent);
        finish();
    }

    private void releaseMediaRecorder(){
        if (recorder != null) {
            recorder.reset();   // clear recorder configuration
            recorder.release(); // release the recorder object
            recorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void initRecorder(int use_profile) throws IOException {  // 0 front, 2 back
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
        videoPath = getPath(this, fileUri);
        try {

            if (mCamera != null) {
                mCamera.reconnect();

            } else {
                if (use_profile == 0) {
                    int cameraId = findFrontFacingCamera();
                    if (cameraId >= 0) {

                        mCamera = Camera.open(cameraId);
                    }
                } else {
                    int cameraId = findBackFacingCamera();
                    if (cameraId >= 0) {

                        mCamera = Camera.open(cameraId);
                    }
                }
                Camera.Parameters parameters = mCamera.getParameters();
                if ( parameters == null ) {
                    Log.e("Error", "Error reading camera parameters");
                    return;
                }

                mCamera.setDisplayOrientation(90);
            }

            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mCamera.unlock();
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Couldn't connect to camera service");
        }

        if(recorder == null) {
            recorder = new MediaRecorder();

            if (use_profile == 0) {
                recorder.setOrientationHint(270);
            } else {
                recorder.setOrientationHint(90);
            }
            recorder.setCamera(mCamera);

            recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);  // DEFAULT
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            recorder.setOutputFile(videoPath);
            recorder.setPreviewDisplay(holder.getSurface());

            recorder.setProfile(profile);

            recorder.setVideoSize(320,240);

            recorder.setCaptureRate(RemoteConfigParam.getInstance(VideoRecorder.this).getDefault_fps());

        }
        try {
            recorder.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), appName);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d(appName, "failed to create directory");
                return null;
            }
        }

        // Create a media file name

        File mediaFile;
        if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + AppConstants.VIDEO_NAME + ".mp4");  //mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + ".mp4"
        } else {
            return null;
        }

        CameraActivity.videoFile = mediaFile;
        return mediaFile;
    }

    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private void stopEveryThingIsPlayed() {

        releaseMediaRecorder();

        // releasing the Camera preview
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            holder.removeCallback(VideoRecorder.this);
            mCamera = null;
        }


        if(timer != null) {
            timer.cancel();
        }

        if(timer != null) {
            timer.cancel();
        }

    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        // Search for the back facing camera
        // get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        // for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            if (isFrontCamera)
            {
                initRecorder(0);
                tbCameraSwitch.setChecked(true);
            }
            else
            {
                initRecorder(2);
                tbCameraSwitch.setChecked(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (holder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
            //    mCamera.setPreviewDisplay(null);

        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (Exception e){
            Log.d("Error ", "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopEveryThingIsPlayed();
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
                        exitApp();

                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })

                .show();

    }

    private void exitApp() {

        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtra("EXIT", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
    }
}
