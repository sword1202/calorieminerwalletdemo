package com.calorieminer.minerapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.provider.MediaStore.Video.Thumbnails;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.calorieminer.minerapp.CustomClass.AppConstants;
import com.calorieminer.minerapp.CustomClass.RemoteConfigParam;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Locale;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = VideoActivity.class.getSimpleName();

    VideoView mvideoView;
    RelativeLayout videoLayout;
    ImageView img_thumbnail;
    private MediaController mediaControls;
    Button recordButton, playButton, pauseButton, discardVideo, commitVideo, btnDecreaseTimer, btnIncreaseTimer, btnContinue;
    EditText tv_fpsValue, tv_timerValue;

    String videoPath;
    Uri videoUri;
    File videoFile;

    long totalTime;
    boolean validFPS = true;
    boolean validTimer = true;

    boolean isClickedDoneTimer = false;
    boolean isClickedDoneFPS = false;
    boolean isableToGoRecordActivity = false;

    FirebaseStorage storage;
    StorageReference storageReference;
    StorageReference ref;

    TextView t_elapsedTIme, t_runningTime, t_frameRate;
    long duration = 0, totalNormalVideoDuration, totalSuperVideoDuration, startTime = 0;
    double currentFpsValue;
    RelativeLayout statusLayout;
    CountDownTimer timer;
    boolean isPaused = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        init();
        prepareVideo();

    }

    private void init() {
        mvideoView = findViewById(R.id.videoView);
        videoLayout = findViewById(R.id.videolayout);
        img_thumbnail = findViewById(R.id.thumbnail_img);
        if (mediaControls == null) {
            mediaControls = new MediaController(this);
        }

        recordButton = findViewById(R.id.record_session);
        playButton = findViewById(R.id.play_video);
        pauseButton = findViewById(R.id.pause_video_playback);
        discardVideo = findViewById(R.id.discard_video);
        commitVideo = findViewById(R.id.commit_video);
        btnDecreaseTimer = findViewById(R.id.decreaseTimeBtn);
        btnIncreaseTimer = findViewById(R.id.increaseTimeBtn);
        btnContinue = findViewById(R.id.continue_video);


        tv_fpsValue = findViewById(R.id.fps_value);
        tv_timerValue = findViewById(R.id.timer_value);
        eventOfEditText();

        t_elapsedTIme = findViewById(R.id.elapsedTime);
        t_runningTime = findViewById(R.id.runningTime);
        t_frameRate = findViewById(R.id.frameRate);

        playButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.GONE);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentFpsValue = getIntent().getExtras().getDouble(getResources().getString(R.string.bundle_fps_value));
            t_frameRate.setText(String.format(Locale.US, "%.1f fps", currentFpsValue));
            playButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.VISIBLE);
            videoLayout.setVisibility(View.VISIBLE);
            setDisable(pauseButton);
        }

        statusLayout = findViewById(R.id.statuslayout);
        statusLayout.setVisibility(View.GONE);

        tv_timerValue.setText(String.valueOf(RemoteConfigParam.getInstance(this).getMax_recording_time()));
        tv_fpsValue.setText(String.valueOf(RemoteConfigParam.getInstance(this).getDefault_fps()));
    }

    private void eventOfEditText() {
        tv_fpsValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE)
                {
                    isClickedDoneFPS = true;
                    isClickedDoneTimer = false;
                    checkNumber(tv_fpsValue.getText().toString(), 0);
                    return true;

                } else
                    return false;
            }
        });

        tv_timerValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE)
                {
                    isClickedDoneFPS = false;
                    isClickedDoneTimer = true;
                    checkNumber(tv_timerValue.getText().toString(), 1);
                    return true;

                } else
                    return false;
            }
        });

        tv_fpsValue.setOnFocusChangeListener(mFocusChangeListner);
        tv_timerValue.setOnFocusChangeListener(mFocusChangeListner);

    }

    private void hideKeyboard()
    {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputManager != null && getCurrentFocus() != null)
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    EditText.OnFocusChangeListener mFocusChangeListner = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            switch (view.getId())
            {
                case R.id.fps_value:
                    if (!b && !isClickedDoneFPS)
                    {
                        checkNumber(tv_fpsValue.getText().toString(), 0);

                    }

                    break;

                case R.id.timer_value:
                    if (!b && !isClickedDoneTimer) {
                        checkNumber(tv_timerValue.getText().toString(), 1);
                    }

                    break;
            }
        }
    };

    private void checkNumber(String strNumber, int status)
    {
        if (status == 0)
        {
            setDisable(recordButton);
            setDisable(discardVideo);
            validFPS = false;
            if (strNumber.matches("") || strNumber.matches("0")) {
                RemoteConfigParam.getInstance(VideoActivity.this).setDefault_fps(1);
                String message = String.format(Locale.US, "Select a digit between %.2f and %d", RemoteConfigParam.getInstance(this).getMin_fps(), (int)RemoteConfigParam.getInstance(this).getMax_fps());
                Toast.makeText(VideoActivity.this, message, Toast.LENGTH_LONG).show();
                tv_fpsValue.setText("1");
                if (!isClickedDoneFPS && !isClickedDoneTimer)
                    isableToGoRecordActivity = false;
                return;
            }


            double doubleValue = Double.parseDouble(strNumber);
            if (doubleValue >= RemoteConfigParam.getInstance(this).getMin_fps() && doubleValue <= RemoteConfigParam.getInstance(this).getMax_fps())
            {
                RemoteConfigParam.getInstance(VideoActivity.this).setDefault_fps(doubleValue);
                validFPS = true;
                if (validTimer){
                    setEnable(recordButton);
                    setEnable(discardVideo);
                }
                if (isClickedDoneFPS) {
                    hideKeyboard();
                    isClickedDoneFPS = false;
                }
                if (!isClickedDoneFPS && !isClickedDoneTimer)
                    isableToGoRecordActivity = true;
            } else
            {
                RemoteConfigParam.getInstance(VideoActivity.this).setDefault_fps(1);
                String message = String.format(Locale.US,"Select a digit between %.2f and %d", RemoteConfigParam.getInstance(this).getMin_fps(), (int)RemoteConfigParam.getInstance(this).getMax_fps());
                Toast.makeText(VideoActivity.this, message, Toast.LENGTH_LONG).show();
                tv_fpsValue.setText("1");
                if (!isClickedDoneFPS && !isClickedDoneTimer)
                    isableToGoRecordActivity = false;
            }
        } else if (status == 1)
        {
            setDisable(recordButton);
            setDisable(discardVideo);
            validTimer = false;
            if (strNumber.matches("") || strNumber.matches("0")) {
                RemoteConfigParam.getInstance(VideoActivity.this).setMax_recording_time(1);
                String message = String.format(Locale.US,"Select a digit between %d and %d", RemoteConfigParam.getInstance(this).getMin_timer(), (int)RemoteConfigParam.getInstance(this).getMax_timer());
                Toast.makeText(VideoActivity.this, message, Toast.LENGTH_LONG).show();
                tv_timerValue.setText("1");
                if (!isClickedDoneFPS && !isClickedDoneTimer)
                    isableToGoRecordActivity = false;
                return;
            }


            long longValue = Long.parseLong(strNumber);
            if (longValue >= RemoteConfigParam.getInstance(this).getMin_timer() && longValue <= RemoteConfigParam.getInstance(this).getMax_timer())
            {
                RemoteConfigParam.getInstance(VideoActivity.this).setMax_recording_time(longValue);
                validTimer = true;
                if (validFPS) {
                    setEnable(recordButton);
                    setEnable(discardVideo);
                }

                if (isClickedDoneTimer) {
                    hideKeyboard();
                    isClickedDoneTimer = false;
                }

                if (!isClickedDoneFPS && !isClickedDoneTimer)
                    isableToGoRecordActivity = true;

            } else
            {
                RemoteConfigParam.getInstance(VideoActivity.this).setMax_recording_time(1);
                String message = String.format(Locale.US,"Select a digit between %d and %d", RemoteConfigParam.getInstance(this).getMin_timer(), (int)RemoteConfigParam.getInstance(this).getMax_timer());
                Toast.makeText(VideoActivity.this, message, Toast.LENGTH_LONG).show();
                tv_timerValue.setText("1");
                if (!isClickedDoneFPS && !isClickedDoneTimer)
                    isableToGoRecordActivity = false;
            }
        }

    }

    private void setEnable(Button buttone) {
        buttone.setEnabled(true);
        buttone.setAlpha(1.0f);
    }

    private void setDisable(Button buttone) {
        buttone.setEnabled(false);
        buttone.setAlpha(0.3f);
    }

    private void prepareVideo()
    {
        isPaused = false;
        startTime = 0;
        playButton.setText(R.string.btn_play);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {

            discardVideo.setVisibility(View.GONE);
            commitVideo.setVisibility(View.GONE);
            return;
        }

        videoPath = extras.getString("videoPath");
        videoFile = new File(videoPath);
        videoUri = Uri.fromFile(videoFile);

        mvideoView.setVideoPath(videoPath);
        mvideoView.requestFocus();

        mvideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                duration = mvideoView.getDuration();
                totalSuperVideoDuration = duration;
                totalNormalVideoDuration = (int) ((totalSuperVideoDuration/1000*30)/currentFpsValue);
            }
        });

        displayThumbnail();
    }

    private void displayThumbnail() {

        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, Thumbnails.MICRO_KIND);
        img_thumbnail.setImageBitmap(bitmap);
        img_thumbnail.setAdjustViewBounds(true);
        img_thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.play_video:

                img_thumbnail.setVisibility(View.GONE);
                setEnable(pauseButton);
                setDisable(playButton);

                if (!isPaused) {
                    mvideoView.start();
                    statusLayout.setVisibility(View.VISIBLE);
                    String initElapsedtime = "00:00/00:00";
                    String initRunningTime = "00:00/00:00";
                    t_elapsedTIme.setText(initElapsedtime);
                    t_runningTime.setText(initRunningTime);
                } else
                {
                    mvideoView.resume();
                    isPaused = false;
                }

                totalSuperVideoDuration = duration;
                totalNormalVideoDuration = (int) ((totalSuperVideoDuration/1000*30)/currentFpsValue);

                final int minutes_total = (int) (totalSuperVideoDuration/(1000*60));
                final int seconds_total = (int) (totalSuperVideoDuration/1000 %60);

                final int minutesR_total = (int) (totalNormalVideoDuration/(60));
                final int secondsR_total = (int) (totalNormalVideoDuration%60);

                timer = new CountDownTimer(duration - startTime, 1000) { // 20minutes= 1200,000

                    public void onTick(long millisUntilFinished) {

                        if (isPaused && timer != null)
                        {
                            startTime = duration - startTime - millisUntilFinished;
                            timer.cancel();
                            setDisable(pauseButton);
                            setEnable(playButton);
                            playButton.setText(R.string.btn_resume);
                            return;
                        }

                        long elapsedTime = duration - millisUntilFinished;
                        int minutes = (int)(elapsedTime/(1000*60));
                        int seconds = (int) (elapsedTime/1000)%60;

                        int realtimeInSeconds = (int) ((elapsedTime/1000*30)/currentFpsValue);
                        int minutesR = realtimeInSeconds/(60);
                        int secondsR = realtimeInSeconds%60;

                        String m1, s1, m2, s2, result;

                        m1 = "" + minutesR;
                        if (minutesR < 10)
                            m1 = "0" + minutesR;

                        s1 = "" + secondsR;
                        if (secondsR < 10)
                            s1 = "0" + secondsR;

                        m2 = "" + minutesR_total;
                        if (minutesR_total < 10)
                            m2 = "0" + minutesR_total;

                        s2 = "" + secondsR_total;
                        if (secondsR_total < 10)
                            s2 = "0" + secondsR_total;

                        result = m1 + ":" + s1 + "/" + m2 + ":" + s2;

                        t_elapsedTIme.setText(result);

                        m1 = "" + minutes;
                        if (minutes < 10)
                            m1 = "0" + minutes;

                        s1 = "" + seconds;
                        if (seconds < 10)
                            s1 = "0" + seconds;

                        m2 = "" + minutes_total;
                        if (minutes_total < 10)
                            m2 = "0" + minutes_total;

                        s2 = "" + seconds_total;
                        if (seconds_total < 10)
                            s2 = "0" + seconds_total;

                        result = m1 + ":" + s1 + "/" + m2 + ":" + s2;
                        t_runningTime.setText(result);

                    }

                    public void onFinish() {

                        String m, s, result;

                        m = "" + minutesR_total;
                        if (minutesR_total < 10)
                            m = "0" + minutesR_total;

                        s = "" + secondsR_total;
                        if (secondsR_total < 10)
                            s = "0" + secondsR_total;

                        result = m + ":" + s + "/" + m + ":" + s;

                        t_elapsedTIme.setText(result);

                        m = "" + minutes_total;
                        if (minutes_total < 10)
                            m = "0" + minutes_total;

                        s = "" + seconds_total;
                        if (seconds_total < 10)
                            s = "0" + seconds_total;

                        result = m + ":" + s + "/" + m + ":" + s;
                        t_runningTime.setText(result);

                        setDisable(pauseButton);
                        setEnable(playButton);
                        mvideoView.stopPlayback();
                        prepareVideo();
                        if (timer != null) {
                            timer.cancel();
                        }
                    }
                }.start();
                break;
            case R.id.pause_video_playback:

                setDisable(pauseButton);
                setEnable(playButton);
                mvideoView.pause();
                isPaused = true;

                break;
            case R.id.record_session:

                gotoRecordActivity();

                break;
            case R.id.discard_video:

                gotoRecordActivity();

                break;
            case R.id.commit_video:

                storeVideoInStorage();

                break;

            case R.id.decreaseTimeBtn:

                totalTime = RemoteConfigParam.getInstance(this).getMax_recording_time();
                totalTime --;

                long minTimer = RemoteConfigParam.getInstance(this).getMin_timer();

                if (totalTime < minTimer) {
                    Toast.makeText(VideoActivity.this, "Select a digit larger than 1.", Toast.LENGTH_LONG).show();
                    totalTime ++;
                }

                tv_timerValue.setText(String.valueOf(totalTime));
                RemoteConfigParam.getInstance(this).setMax_recording_time(totalTime);

                break;

            case R.id.increaseTimeBtn:

                totalTime = RemoteConfigParam.getInstance(this).getMax_recording_time();
                totalTime ++;
                long maxTimer = RemoteConfigParam.getInstance(this).getMax_timer();
                if (totalTime > maxTimer){
                    Toast.makeText(this, "Limit reached.", Toast.LENGTH_LONG).show();
                    totalTime --;
                }

                tv_timerValue.setText(String.valueOf(totalTime));
                RemoteConfigParam.getInstance(this).setMax_recording_time(totalTime);
                break;
            case R.id.continue_video:
                if (videoLayout.getVisibility() == View.VISIBLE) {
                    gotoCameraActivity(true);
                } else {
                    gotoCameraActivity(false);
                }

                break;
        }
    }

    private void gotoRecordActivity() {

        isClickedDoneFPS = false;
        isClickedDoneTimer = false;
        isableToGoRecordActivity = false;
        checkNumber(tv_fpsValue.getText().toString(), 0);
        checkNumber(tv_timerValue.getText().toString(), 1);

        if (!isableToGoRecordActivity)
            return;

        int cameraId = findFrontFacingCamera();
        VideoRecorder.isFrontCamera = cameraId >= 0;

        Intent cameraIntent = new Intent(this, VideoRecorder.class);
        startActivity(cameraIntent);
        finish();
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

    private void storeVideoInStorage()
    {
        Toast.makeText(VideoActivity.this, "You cannot claim Caloriecoins in the Demo version.", Toast.LENGTH_LONG).show();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        // check size of video
//        long videoSize = videoFile.length()/1024;

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null)
            return;
        String currentEmail = mUser.getEmail();
        ref = storageReference.child("video/"+ currentEmail + "/" + AppConstants.VIDEO_NAME);

        uploadVideo();
//        new CallVision(videoPath).execute();
    }

    private void uploadVideo() {

        if(videoPath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            ref.putFile(videoUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(VideoActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            gotoCameraActivity(true);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(VideoActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    private void gotoCameraActivity(boolean bundleValue) {

        Intent cameraIntent = new Intent(this, CameraActivity.class);
        cameraIntent.putExtra("video_extra", bundleValue);
        startActivity(cameraIntent);
        finish();
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

//    @SuppressLint("StaticFieldLeak")
//    private class CallVision extends AsyncTask<Void, Void, String> {
//
//        private final String mVideoPath;
//
//        CallVision(String videoPath) {
//            this.mVideoPath = videoPath;
//        }
//
//        @Override
//        protected String doInBackground(Void... voids) {
//
//            try {
////                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
//                NetHttpTransport httpTransport = new NetHttpTransport();
////                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
//                AndroidJsonFactory jsonFactory = new AndroidJsonFactory();
//                VisionRequestInitializer requestInitializer =
//                        new VisionRequestInitializer(getResources().getString(R.string.google_key)) {
//                            /**
//                             * We override this so we can inject important identifying fields into the HTTP
//                             * headers. This enables use of a restricted cloud platform API key.
//                             */
//                            @Override
//                            protected void initializeVisionRequest(VisionRequest<?> visionRequest)
//                                    throws IOException {
//                                super.initializeVisionRequest(visionRequest);
//
//                                String packageName = getPackageName();
//                                visionRequest.getRequestHeaders().set(AppConstants.ANDROID_PACKAGE_HEADER, packageName);
//
//                                String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);
//
//                                visionRequest.getRequestHeaders().set(AppConstants.ANDROID_CERT_HEADER, sig);
//                            }
//                        };
//
//                Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
//                builder.setVisionRequestInitializer(requestInitializer);
//
//                Vision vision = builder.build();
//
//                AnnotateVideoRequest annotateVideoRequest = new AnnotateVideoRequest();
//
//                BatchAnnotateImagesRequest batchAnnotateImagesRequest =
//                        new BatchAnnotateImagesRequest();
//
//                batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
//                    AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
//
//                    // Add the image
//                    com.google.api.services.vision.v1.model.Image base64EncodedImage = new com.google.api.services.vision.v1.model.Image();
//                    // Convert the bitmap to a JPEG
//                    // Just in case it's a format that Android understands but Cloud Vision
////                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
////                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
//                    byte[] videoBytes = mVideoPath.getBytes("UTF-8");
//                    String base64 = Base64.encodeToString(videoBytes, Base64.DEFAULT);
//
//                    // Base64 encode the JPEG
//                    base64EncodedImage.encodeContent(imageBytes);
//                    annotateImageRequest.setImage(base64EncodedImage);
//
//                    // add the features we want
//                    annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
//                        Feature faceDetection = new Feature();
//
//                        if (!isFoodButton)
//                        {
//                            faceDetection.setType(AppConstants.FACE_DETECTION);
//                            faceDetection.setMaxResults(15);
//                            add(faceDetection);
//                        }
//
//
//                        Feature labelDetection = new Feature();
//                        labelDetection.setType(AppConstants.LABEL_DETECTION);
//                        labelDetection.setMaxResults(15);
//                        add(labelDetection);
//
//                        if (!isFoodButton)
//                        {
//                            Feature safeSearch = new Feature();
//                            safeSearch.setType(AppConstants.SAFE_SEARCH_DETECTION);
//                            safeSearch.setMaxResults(5);
//                            add(safeSearch);
//                        }
//
//
//                    }});
//
//                    // Add the list of one thing to the request
//                    add(annotateImageRequest);
//                }});
//
//                Vision.Images.Annotate annotateRequest =
//                        vision.images().annotate(batchAnnotateImagesRequest);
//                // Due to a bug: requests to Vision API containing large images fail when GZipped.
//                annotateRequest.setDisableGZipContent(true);
//                Log.d(TAG, "created Cloud Vision request object, sending request");
//
//                BatchAnnotateImagesResponse response = annotateRequest.execute();
//                return convertResponseToString(response);
//
//            } catch (GoogleJsonResponseException e) {
//                Log.d(TAG, "failed to make API request because " + e.getContent());
//            } catch (IOException e) {
//                Log.d(TAG, "failed to make API request because of other IOException " +
//                        e.getMessage());
//            }
//            return "Video Intelligence API request failed.";
//
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            if (result.equals(getResources().getString(R.string.analysispicture)))
////                detectingVisionData();
//        }
//    }
//
//    private String convertResponseToString(BatchAnnotateImagesResponse response) {
//
//        String message = getResources().getString(R.string.analysispicture);
//
//        // upload face data
//
//
//        List<FaceAnnotation> faceAnnotations = response.getResponses().get(0).getFaceAnnotations();
//
////        WebDetection webDetections = response.getResponses().get(0).getWebDetection();
//        List<EntityAnnotation> labelAnnotations = response.getResponses().get(0).getLabelAnnotations();
////        List<EntityAnnotation> landmarkAnnotations = response.getResponses().get(0).getLandmarkAnnotations();
////        mSafeSearchAnnotation = response.getResponses().get(0).getSafeSearchAnnotation();
////
////        if (labelAnnotations != null)
////        {
////            if (isPhotoButton) {
////                dBRef_face.child(AppConstants.LABEL_DETECTION).setValue(labelAnnotations);
////                mLabelAnnotations = (ArrayList<EntityAnnotation>) labelAnnotations;
////            }
////
////            if (isFoodButton)
////            {
////                dBRef_food.child(AppConstants.LABEL_DETECTION).setValue(labelAnnotations);
////                mLabelAnnotations_food = (ArrayList<EntityAnnotation>) labelAnnotations;
////            }
////
////        }
////
////        if (isPhotoButton) {
////            if (faceAnnotations != null) {
////
////                dBRef_face.child(AppConstants.FACE_DETECTION).setValue(faceAnnotations);
////                mFaceAnnotation = faceAnnotations.get(0);
////            }
////
////            if (mSafeSearchAnnotation != null)
////            {
////                dBRef_face.child(AppConstants.SAFE_SEARCH_DETECTION).setValue(mSafeSearchAnnotation);
////            }
////
////        }
//
//        return message;
//
//    }

}
