package com.calorieminer.minerapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.calorieminer.minerapp.CustomClass.AppConstants;
import com.calorieminer.minerapp.CustomClass.CustomDialog;
import com.calorieminer.minerapp.CustomClass.PrefsUtil;
import com.calorieminer.minerapp.CustomClass.RemoteConfigParam;
import com.calorieminer.minerapp.ListAdapter.LabelListAdapter;
import com.calorieminer.minerapp.VisionPackage.GraphicOverlayView;
import com.calorieminer.minerapp.model.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.FaceAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.SafeSearchAnnotation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener{

    String[] permissions= new String[]{
            android.Manifest.permission.CAMERA
    };

    LinearLayout visionLayout, foodLayout;
    TextView tv_pictureAnalysisTitle;

    public static String PHOTO_NAME;
    public static String FOOD_NAME;

    private static final String TAG = CameraActivity.class.getSimpleName();
    private Button btnFood;
    private Button btnAccreditedInvestorPortal;
    private Button btnStartMining;
    private Button btnSendFiles;
    private Button btnCreatePin;
    private Button btnRemovePin;
    private Button btnChangePin;
    private Button btnCalibrate;
    GraphicOverlayView imvSelfie, imvFood;
    TextView tv_name, tv_email, tv_phoneNumber, tv_lat_lon, tv_gpsTimestamp;
    static double latitude, longitude;
    static String phoneNumber = "";
    static String timeStamp = "";
    static String current_email = "";
    static final int REQUEST_CAMERA_CAPTURE = 10;
    static final int REQUEST_CAMERA_PERMISSION = 1;
    String uid = "";
    static Users selectedUser;

    // send files
    static File videoFile, photoFile, htmlFile, idFile, documentFile, foodFile;

    String firebaseVideoUrl, firebasePhotUrl, firebaseFoodUrl;

    DatabaseReference dBRef_face, dBRef_id, dBRef_doc, dBRef_food;

    private CustomDialog pdface_detect, pdID_detect, pdDoc_detect, pdFood_detect, pdimage_file, pdvideo_file, pdID_file, pdDoc_file, pdFood_file;
    FirebaseStorage storage;
    StorageReference storageReference;
    StorageReference refphoto, refVideo, refID, refDoc, refFood;

    ImageHelper imageHelper;
    boolean isPhotoButton;
    boolean isVideoButton;
    boolean isFoodButton;

    boolean isDetectedVision, isUploadedImage;

    private int cameraId = 0;

    // cloudvision variables
    FaceAnnotation mFaceAnnotation, mFaceAnnotation_id, mFaceAnnotation_doc;
    EntityAnnotation mLabelAnnotation, mLabelAnnotation_food, mLabelAnnotation_id, mLabelAnnotation_doc;
    ArrayList<EntityAnnotation> mLabelAnnotations, mLabelAnnotations_food, mLabelAnnotations_id, mLabelAnnotations_doc;
    SafeSearchAnnotation mSafeSearchAnnotation, mSafeSearchAnnotation_id, mSafeSearchAnnotation_doc;
    public static int previewWidth;
    public static int previewHeight;
    ListView mLabelListView, mFaceListView, mLabelFoodListView;
    CheckBox overlayCheckBox;
    Bundle extras;

    static boolean isTakenPicture = false;
    static boolean isTakenFood = false;

    boolean isStoredPIN;
    boolean isEnabledPIN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        generateFileNames();
        initialRequestPermission();
        initFirebase();

    }

    private void generateFileNames() {

        extras = getIntent().getExtras();
//        PHOTO_NAME = "PHOTO_" + randomSTR();
//        FOOD_NAME = "FOOD_" + randomSTR();
        PHOTO_NAME = "my_picture";
        FOOD_NAME = "my_food";

        isDetectedVision = false;
        isUploadedImage = false;

        isPhotoButton = false;
        isVideoButton = false;
        isFoodButton = false;
    }

    public static String randomSTR() {

        String e_chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder returnStr = new StringBuilder();
        Random rnd = new Random();
        while (returnStr.length() < 8) { // length of the random string.
            int index = (int) (rnd.nextFloat() * e_chars.length());
            returnStr.append(e_chars.charAt(index));
        }
        return returnStr.toString();
    }

    private void initialRequestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) +
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION);

            }
        }
    }

    private void initFirebase() {

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        init();
        if (mFirebaseUser == null)
        {
            return;
        }

        uid = mFirebaseUser.getUid();
        current_email = mFirebaseUser.getEmail();
        checkDatabase();

    }

    private void init() {

        isStoredPIN = PrefsUtil.getInstance(this).getValue(PrefsUtil.SCRAMBLE_PIN, false);
        isEnabledPIN = RemoteConfigParam.getInstance(CameraActivity.this).getPinEnabled();

        Button btnCamera = findViewById(R.id.camerabutton);
        btnCamera.setOnClickListener(this);

        btnFood = findViewById(R.id.foodbutton);
        btnFood.setOnClickListener(this);
//        setDisable(btnFood);

        btnAccreditedInvestorPortal = findViewById(R.id.accredited_investor_portal);
        btnAccreditedInvestorPortal.setOnClickListener(this);

        if (RemoteConfigParam.getInstance(CameraActivity.this).getShow_accredited_investor_button())
        {
            btnAccreditedInvestorPortal.setVisibility(View.VISIBLE);
//            setDisable(btnAccreditedInvestorPortal);
        }

        else
            btnAccreditedInvestorPortal.setVisibility(View.GONE);

        btnStartMining = findViewById(R.id.btnstartmining);
        btnStartMining.setOnClickListener(this);
//        setDisable(btnStartMining);

        btnSendFiles = findViewById(R.id.email_files);
        btnSendFiles.setOnClickListener(this);
//        setDisable(btnSendFiles);

        Button btnStartChat = findViewById(R.id.start_chat);
        btnStartChat.setOnClickListener(this);

        if (RemoteConfigParam.getInstance(CameraActivity.this).getCommunity_chat_enabled())
            btnStartChat.setVisibility(View.VISIBLE);
        else
            btnStartChat.setVisibility(View.GONE);

        btnCreatePin = findViewById(R.id.createpincode);
        btnCreatePin.setOnClickListener(this);

        btnRemovePin = findViewById(R.id.removepincode);
        btnRemovePin.setOnClickListener(this);

        btnChangePin = findViewById(R.id.changepincode);
        btnChangePin.setOnClickListener(this);

        btnCalibrate = findViewById(R.id.btntrack);
        btnCalibrate.setOnClickListener(this);

        btnCreatePin.setVisibility(View.GONE);
        btnChangePin.setVisibility(View.GONE);
        btnRemovePin.setVisibility(View.GONE);

        if (isEnabledPIN)
        {
            if (isStoredPIN) {
                btnChangePin.setVisibility(View.VISIBLE);
                btnRemovePin.setVisibility(View.VISIBLE);
            }
        }


        Button btnContinue = findViewById(R.id.btncontinue);
        btnContinue.setOnClickListener(this);
//        setDisable(btnContinue);

        imvSelfie = findViewById(R.id.imageView_selfie);
        imvSelfie.setVisibility(View.GONE);
        imvFood = findViewById(R.id.imgView_food);
        imvFood.setVisibility(View.GONE);

        tv_name = findViewById(R.id.name_textView);
        tv_email = findViewById(R.id.emailTextView);
        tv_phoneNumber = findViewById(R.id.phoneTextView);
        tv_lat_lon = findViewById(R.id.latlonTextView);
        tv_gpsTimestamp = findViewById(R.id.gpstimestamp);

        visionLayout = findViewById(R.id.linearLayoutpictureanalysis);
        visionLayout.setVisibility(View.GONE);

        foodLayout = findViewById(R.id.linearLayoutfoodanalysis);
        foodLayout.setVisibility(View.GONE);

        tv_pictureAnalysisTitle = findViewById(R.id.pictureAnalysisTitle);

        // overlay checkbox

        overlayCheckBox = findViewById(R.id.overlaycheckbox);
        overlayCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                {
                    imvSelfie.startOverlay(mFaceAnnotation, previewWidth, previewHeight);
                    overlayCheckBox.setText(R.string.camera_on);
                } else
                {
                    imvSelfie.startOverlay(null, 1, 1);
                    overlayCheckBox.setText(R.string.camera_off);
                }
            }
        });

        overlayCheckBox.setVisibility(View.GONE);
    }

//    private void setEnable(Button buttone) {
//        buttone.setEnabled(true);
//        buttone.setAlpha(1.0f);
//    }
//
//    private void setDisable(Button buttone) {
//        buttone.setEnabled(false);
//        buttone.setAlpha(0.3f);
//    }

    private void checkDatabase() {

        pdface_detect = new CustomDialog(this);
        pdface_detect.setText(getResources().getString(R.string.face_loading));
        pdface_detect.show();

        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (pdface_detect != null)
                    pdface_detect.dismiss();

                if (dataSnapshot.exists())
                {
                    selectedUser = dataSnapshot.getValue(Users.class);

                    // calculate preview size for landmark overlay
                    if (selectedUser == null) return;
                    previewWidth = selectedUser.getWidth();
                    previewHeight = selectedUser.getHeight();



                    // display landmark overlay

                    dBRef_face = FirebaseDatabase.getInstance().getReference().child(AppConstants.DATA_FIELD).child(uid).child(PHOTO_NAME);
                    dBRef_id   = FirebaseDatabase.getInstance().getReference().child(AppConstants.DATA_FIELD).child(uid).child(AppConstants.ID_NAME);
                    dBRef_doc  = FirebaseDatabase.getInstance().getReference().child(AppConstants.DATA_FIELD).child(uid).child(AppConstants.DOCUMENT_NAME);
                    dBRef_food  = FirebaseDatabase.getInstance().getReference().child(AppConstants.DATA_FIELD).child(uid).child(FOOD_NAME);

                    if (rootFile() == null)
                        return;

                    videoFile = new File(rootFile().getPath() + File.separator + AppConstants.VIDEO_NAME + ".mp4");
                    photoFile = new File(rootFile().getPath() + File.separator + PHOTO_NAME + ".jpg");
                    foodFile = new File(rootFile().getPath() + File.separator + FOOD_NAME + ".jpg");

                    displayData();

                    // check photo and video, and then write it in local storage
                    storage          = FirebaseStorage.getInstance();
                    storageReference = storage.getReference();

                    if (RemoteConfigParam.getInstance(CameraActivity.this).getShow_accredited_investor_button() &&
                            extras != null && extras.containsKey("id_doc_extra") && extras.getBoolean("id_doc_extra"))
                    {

                        handleID();
                        handleDocument();
                    }

                    refphoto     = storageReference.child("images/"+ selectedUser.getUserEmail() + "/" + PHOTO_NAME);

                    refFood     = storageReference.child("foods/"+ selectedUser.getUserEmail() + "/" + FOOD_NAME);

                    if (isTakenPicture) {
                        detect_face();
                        handleImage();
                    }

                    if (isTakenFood) {
                        detect_food();
                        handleFood();
                    }

                    if(extras != null && extras.containsKey("video_extra") && extras.getBoolean("video_extra"))
                        handleVideo();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (pdface_detect != null)
                    pdface_detect.dismiss();
            }
        });

    }

    private void handleID()
    {

        refID = storageReference.child("images/"+ selectedUser.getUserEmail() + "/" + AppConstants.ID_NAME);

        writeIDInStorage();

    }

    private void writeIDInStorage() {

        if (rootFile()==null)
            return;
        idFile       = new File(rootFile().getPath() + File.separator + AppConstants.ID_NAME + ".jpg");
        documentFile = new File(rootFile().getPath() + File.separator + AppConstants.DOCUMENT_NAME + ".jpg");
        pdID_file = new CustomDialog(this);
        pdID_file.setText(getResources().getString(R.string.goverment_id_loading));
        pdID_file.show();



        refID.getFile(idFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                if (pdID_file!=null)
                    pdID_file.dismiss();
//                if (documentFile.exists())
//                    setEnable(btnStartMining);
            }
        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pdID_file!=null)
                            pdID_file.dismiss();
                    }
                });
    }

    private File rootFile() {

        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getResources().getString(R.string.app_name));
        if (! storageDir.exists()){
            if (! storageDir.mkdirs()){
                Log.d(getResources().getString(R.string.app_name), "failed to create directory");
                return null;
            }
        }
        return storageDir;
    }

    private void handleDocument()
    {
        refDoc    = storageReference.child("images/"+ selectedUser.getUserEmail() + "/" + AppConstants.DOCUMENT_NAME);

        writeDocumentInStorage();

    }

    private void writeDocumentInStorage() {

        if (rootFile()==null)
            return;

        idFile       = new File(rootFile().getPath() + File.separator + AppConstants.ID_NAME + ".jpg");
        documentFile = new File(rootFile().getPath() + File.separator + AppConstants.DOCUMENT_NAME + ".jpg");

        pdDoc_file = new CustomDialog(this);
        pdDoc_file.setText(getResources().getString(R.string.documents_loading));
        pdDoc_file.show();

        refDoc.getFile(documentFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                if (pdDoc_file!=null)
                    pdDoc_file.dismiss();
//                if (idFile.exists())
//                    setEnable(btnStartMining);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (pdDoc_file!=null)
                    pdDoc_file.dismiss();
            }
        });
    }

    private void handleImage() {

        imvSelfie.setVisibility(View.GONE);
        pdimage_file = new CustomDialog(this);
        pdimage_file.setText(getResources().getString(R.string.image_loading));
        pdimage_file.show();

        if (refphoto == null)
        {
            return;
        }

//        downloadImageURL();
        writeImageInStorage();

    }

    private void downloadImageURL() {

        refphoto.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                firebasePhotUrl = uri.toString();
                Glide.with(getApplicationContext())
                        .load(firebasePhotUrl)
                        .asBitmap()
                        .dontAnimate()
                        .into(new BitmapImageViewTarget(imvSelfie) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                //Play with bitmap
                                super.setResource(resource);
                                showImage();
                            }
                        });

                writeImageInStorage();


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (pdimage_file!=null)
                    pdimage_file.dismiss();

//                Toast.makeText(CameraActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void writeImageInStorage() {

        if (rootFile()==null)
            return;

        refphoto.getFile(photoFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                if (pdimage_file!=null)
                    pdimage_file.dismiss();

                showImage();
//                if (videoFile.exists() && foodFile.exists())
//                    generateHTMLString();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (pdimage_file!=null)
                    pdimage_file.dismiss();
            }
        });
    }

    private void handleFood() {

        imvFood.setVisibility(View.GONE);
        pdFood_file = new CustomDialog(this);
        pdFood_file.setText(getResources().getString(R.string.food_loading));
        pdFood_file.show();

        if (refFood == null)
        {
            return;
        }
        writeFoodInStorage();
//        downloadFoodURL();

    }

    private void downloadFoodURL() {

        refFood.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                firebaseFoodUrl = uri.toString();
                Glide.with(getApplicationContext())
                        .load(firebaseFoodUrl)
                        .asBitmap()
                        .into(new BitmapImageViewTarget(imvFood) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                //Play with bitmap
                                super.setResource(resource);
                                if (pdFood_file!=null)
                                    pdFood_file.dismiss();
                                createRoundedImageView(imvFood);
                                imvFood.setVisibility(View.VISIBLE);
//                                if (RemoteConfigParam.getInstance(CameraActivity.this).getShow_accredited_investor_button())
//                                    setEnable(btnAccreditedInvestorPortal);
//                                else
//                                    setEnable(btnStartMining);
                            }
                        });

                writeFoodInStorage();


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (pdFood_file!=null)
                    pdFood_file.dismiss();

//                Toast.makeText(CameraActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayImage(GraphicOverlayView imgView, File imageFile)
    {
        String filePath = imageFile.getPath();
        imgView.setImageBitmap(BitmapFactory.decodeFile(filePath));
    }

    private void writeFoodInStorage() {

        if (rootFile()==null)
            return;

        refFood.getFile(foodFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                if (pdFood_file!=null)
                    pdFood_file.dismiss();

                createRoundedImageView(imvFood);
                imvFood.setVisibility(View.VISIBLE);
                displayImage(imvFood, foodFile);
//                if (photoFile.exists() && videoFile.exists())
//                    generateHTMLString();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pdFood_file!=null)
                            pdFood_file.dismiss();
                    }
                });
    }

    private void handleVideo() {

        pdvideo_file = new CustomDialog(this);
        pdvideo_file.setText(getResources().getString(R.string.video_loading));
        pdvideo_file.show();

        refVideo = storageReference.child("video/"+ selectedUser.getUserEmail() + "/" + AppConstants.VIDEO_NAME);

        downloadVideoURL();

    }

    private void downloadVideoURL() {

        refVideo.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                firebaseVideoUrl = uri.toString();

                writeVideoInStorage();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (pdvideo_file!=null)
                    pdvideo_file.dismiss();

//                Toast.makeText(CameraActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void writeVideoInStorage() {

        if (rootFile()==null)
            return;

        refVideo.getFile(videoFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                if (pdvideo_file!=null)
                    pdvideo_file.dismiss();
//                if (photoFile.exists() && foodFile.exists())
//                    generateHTMLString();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (pdvideo_file!=null)
                    pdvideo_file.dismiss();
            }
        });
    }

    public void displayData() {
        tv_name.setText(selectedUser.getUserName());
        tv_email.setText(selectedUser.getUserEmail());

        if (RemoteConfigParam.getInstance(CameraActivity.this).getPhoneauth_enabled())
            tv_phoneNumber.setText(selectedUser.getphoneNumber());
        else {
            TextView tv_phoneTitle = findViewById(R.id.phone_title);
            tv_phoneTitle.setVisibility(View.GONE);
            tv_phoneNumber.setVisibility(View.GONE);
        }
        String lat_long = "" + selectedUser.getlatitude() + ", " + selectedUser.getlongitude();
        tv_lat_lon.setText(lat_long);
        tv_gpsTimestamp.setText(selectedUser.getTimeStamp());

        if (!RemoteConfigParam.getInstance(this).getMap_location_enabled())
        {
            TextView tv_gpsTitle = findViewById(R.id.gps_title);
            tv_gpsTitle.setVisibility(View.GONE);
            tv_lat_lon.setVisibility(View.GONE);
            tv_gpsTimestamp.setVisibility(View.GONE);
        }

    }

    private void detect_face() {

        overlayCheckBox.setVisibility(View.GONE);

        if (dBRef_face == null)
        {
            return;
        }

        isDetectedVision = true;
        if (isUploadedImage) {
            isPhotoButton = false;
            isDetectedVision = false;
            isUploadedImage = false;
        }

        pdface_detect = new CustomDialog(this);
        pdface_detect.setText(getResources().getString(R.string.face_loading));
        pdface_detect.show();

        dBRef_face.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (pdface_detect != null)
                    pdface_detect.dismiss();

                if (dataSnapshot.exists())
                {
                    for (DataSnapshot everyfeature : dataSnapshot.getChildren())
                    {
                        // face_annotations
                        if (everyfeature.getKey().equals(AppConstants.FACE_DETECTION))
                        {

                            List<FaceAnnotation> results = (List<FaceAnnotation>) everyfeature.getValue();
                            if (results == null) return;
                            for (int i=0; i<results.size(); i++) {
                                String mChild = "" + i;
                                mFaceAnnotation = everyfeature.child(mChild).getValue(FaceAnnotation.class);

                            }


                        } else if (everyfeature.getKey().equals(AppConstants.LABEL_DETECTION))
                        {
                            List<EntityAnnotation> results = (List<EntityAnnotation>) everyfeature.getValue();
                            mLabelAnnotations = new ArrayList<EntityAnnotation>();
                            if (results == null) return;
                            for (int i=0; i<results.size(); i++)
                            {
                                String mChild = ""+i;
                                mLabelAnnotation = everyfeature.child(mChild).getValue(EntityAnnotation.class);

                                if (mLabelAnnotation != null)
                                    mLabelAnnotations.add(mLabelAnnotation);

                            }

                        } else if (everyfeature.getKey().equals(AppConstants.SAFE_SEARCH_DETECTION))
                        {
                            mSafeSearchAnnotation = everyfeature.getValue(SafeSearchAnnotation.class);
                        }
                    }
                    handleCloudVision(PHOTO_NAME); // face handle
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (pdface_detect != null)
                    pdface_detect.dismiss();
            }
        });
    }

    private void detect_food() {

        if (dBRef_food == null)
        {
            return;
        }

        isDetectedVision = true;
        if (isUploadedImage) {
            isFoodButton = false;
            isDetectedVision = false;
            isUploadedImage = false;
        }

        pdFood_detect = new CustomDialog(this);
        pdFood_detect.setText(getResources().getString(R.string.food_loading));
        pdFood_detect.show();

        dBRef_food.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (pdFood_detect != null)
                    pdFood_detect.dismiss();

                if (dataSnapshot.exists())
                {
                    for (DataSnapshot everyfeature : dataSnapshot.getChildren())
                    {

                        if (everyfeature.getKey().equals(AppConstants.LABEL_DETECTION))
                        {
                            List<EntityAnnotation> results = (List<EntityAnnotation>) everyfeature.getValue();
                            mLabelAnnotations_food = new ArrayList<EntityAnnotation>();
                            if (results == null) return;
                            for (int i=0; i<results.size(); i++)
                            {
                                String mChild = ""+i;
                                mLabelAnnotation_food = everyfeature.child(mChild).getValue(EntityAnnotation.class);

                                if (mLabelAnnotation_food != null)
                                    mLabelAnnotations_food.add(mLabelAnnotation_food);

                            }

                        }

                    }

                    handleCloudVision(FOOD_NAME); // face handle
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (pdFood_detect != null)
                    pdFood_detect.dismiss();
            }
        });
    }

    private void detect_goverment_id() {
        pdID_detect = new CustomDialog(this);
        pdID_detect.setText(getResources().getString(R.string.goverment_id_loading));
        pdID_detect.show();
        dBRef_id.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (pdID_detect != null)
                    pdID_detect.dismiss();

                if (dataSnapshot.exists())
                {
                    for (DataSnapshot everyfeature : dataSnapshot.getChildren())
                    {
                        // face_annotations
                        if (everyfeature.getKey().equals(AppConstants.FACE_DETECTION))
                        {
                            List<FaceAnnotation> results = (List<FaceAnnotation>) everyfeature.getValue();
                            if (results == null) return;
                            for (int i=0; i<results.size(); i++) {
                                String mChild = "" + i;
                                mFaceAnnotation = everyfeature.child(mChild).getValue(FaceAnnotation.class);

                            }


                        } else if (everyfeature.getKey().equals(AppConstants.LABEL_DETECTION))
                        {
                            List<EntityAnnotation> results = (List<EntityAnnotation>) everyfeature.getValue();
                            mLabelAnnotations = new ArrayList<>();
                            if (results == null) return;
                            for (int i=0; i<results.size(); i++)
                            {
                                String mChild = ""+i;
                                mLabelAnnotation = everyfeature.child(mChild).getValue(EntityAnnotation.class);

                                if (mLabelAnnotation != null)
                                    mLabelAnnotations.add(mLabelAnnotation);

                            }

                        } else if (everyfeature.getKey().equals(AppConstants.SAFE_SEARCH_DETECTION))
                        {
                            mSafeSearchAnnotation = everyfeature.getValue(SafeSearchAnnotation.class);
                        }
                    }
                    handleCloudVision(AppConstants.ID_NAME);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (pdID_detect != null)
                    pdID_detect.dismiss();
            }
        });
    }

    private void detect_documents() {
        pdDoc_detect = new CustomDialog(this);
        pdDoc_detect.setText(getResources().getString(R.string.documents_loading));
        pdDoc_detect.show();
        dBRef_face.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (pdDoc_detect != null)
                    pdDoc_detect.dismiss();

                if (dataSnapshot.exists())
                {
                    for (DataSnapshot everyfeature : dataSnapshot.getChildren())
                    {
                        // face_annotations
                        if (everyfeature.getKey().equals(AppConstants.FACE_DETECTION))
                        {
                            List<FaceAnnotation> results = (List<FaceAnnotation>) everyfeature.getValue();
                            if (results == null) return;
                            for (int i=0; i<results.size(); i++) {
                                String mChild = "" + i;
                                mFaceAnnotation = everyfeature.child(mChild).getValue(FaceAnnotation.class);

                            }


                        } else if (everyfeature.getKey().equals(AppConstants.LABEL_DETECTION))
                        {
                            List<EntityAnnotation> results = (List<EntityAnnotation>) everyfeature.getValue();
                            mLabelAnnotations = new ArrayList<EntityAnnotation>();
                            if (results == null) return;
                            for (int i=0; i<results.size(); i++)
                            {
                                String mChild = ""+i;
                                mLabelAnnotation = everyfeature.child(mChild).getValue(EntityAnnotation.class);

                                if (mLabelAnnotation != null)
                                    mLabelAnnotations.add(mLabelAnnotation);

                            }

                        } else if (everyfeature.getKey().equals(AppConstants.SAFE_SEARCH_DETECTION))
                        {
                            mSafeSearchAnnotation = everyfeature.getValue(SafeSearchAnnotation.class);
                        }
                    }
                    handleCloudVision(AppConstants.DOCUMENT_NAME);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (pdDoc_detect != null)
                    pdDoc_detect.dismiss();
            }
        });
    }

    private void handleCloudVision(String state) {

        if (state.equals(PHOTO_NAME))
        {
            if (mFaceAnnotation != null)
            {
                setVisableCheckbox();
                checkFaceData();
            }


            if (mLabelAnnotations.size() > 0)
                checkLabelData(state);

        } else if (state.equals(FOOD_NAME))
        {
            checkLabelData(state);
        }

    }

    private void checkFaceData()
    {

        imvSelfie.startOverlay(mFaceAnnotation, previewWidth, previewHeight);



        ArrayList<EntityAnnotation> mFaceDetectionList = new ArrayList<>();

        mLabelAnnotation = new EntityAnnotation();
        if (mFaceAnnotation.getJoyLikelihood()!= null)
        {
            mLabelAnnotation.setDescription("Joy");
            mLabelAnnotation.setMid(mFaceAnnotation.getJoyLikelihood());
            mFaceDetectionList.add(mLabelAnnotation);
        }

        mLabelAnnotation = new EntityAnnotation();
        if (mFaceAnnotation.getAngerLikelihood()!= null){
            mLabelAnnotation.setDescription("Anger");
            mLabelAnnotation.setMid(mFaceAnnotation.getAngerLikelihood());
            mFaceDetectionList.add(mLabelAnnotation);
        }

        mLabelAnnotation = new EntityAnnotation();
        if (mFaceAnnotation.getSorrowLikelihood() != null)
        {
            mLabelAnnotation.setDescription("Sorrow");
            mLabelAnnotation.setMid(mFaceAnnotation.getSorrowLikelihood());
            mFaceDetectionList.add(mLabelAnnotation);
        }

        mLabelAnnotation = new EntityAnnotation();
        if (mFaceAnnotation.getSurpriseLikelihood() != null)
        {
            mLabelAnnotation.setDescription("Surprise");
            mLabelAnnotation.setMid(mFaceAnnotation.getSurpriseLikelihood());
            mFaceDetectionList.add(mLabelAnnotation);
        }

        mLabelAnnotation = new EntityAnnotation();
        if (mFaceAnnotation.getBlurredLikelihood() != null){
            mLabelAnnotation.setDescription("Blurred");
            mLabelAnnotation.setMid(mFaceAnnotation.getBlurredLikelihood());
            mFaceDetectionList.add(mLabelAnnotation);
        }

        mLabelAnnotation = new EntityAnnotation();
        if (mFaceAnnotation.getHeadwearLikelihood() != null)
        {
            mLabelAnnotation.setDescription("Headwear");
            mLabelAnnotation.setMid(mFaceAnnotation.getHeadwearLikelihood());
            mFaceDetectionList.add(mLabelAnnotation);
        }

        mLabelAnnotation = new EntityAnnotation();
        if (mFaceAnnotation.getUnderExposedLikelihood() != null)
        {
            mLabelAnnotation.setDescription("UnderExposed");
            mLabelAnnotation.setMid(mFaceAnnotation.getUnderExposedLikelihood());
            mFaceDetectionList.add(mLabelAnnotation);
        }

        // safe_annotations


        mLabelAnnotation = new EntityAnnotation();
        if (mSafeSearchAnnotation!=null && mSafeSearchAnnotation.getAdult() != null)
        {
            mLabelAnnotation.setDescription("Adult Explicit");
            mLabelAnnotation.setMid(mSafeSearchAnnotation.getAdult());
            mFaceDetectionList.add(mLabelAnnotation);
        }

        mLabelAnnotation = new EntityAnnotation();
        if (mSafeSearchAnnotation!=null && mSafeSearchAnnotation.getMedical() != null)
        {
            mLabelAnnotation.setDescription("Medical Explicit");
            mLabelAnnotation.setMid(mSafeSearchAnnotation.getMedical());
            mFaceDetectionList.add(mLabelAnnotation);
        }

        mLabelAnnotation = new EntityAnnotation();
        if (mSafeSearchAnnotation!=null && mSafeSearchAnnotation.getViolence() != null)
        {
            mLabelAnnotation.setDescription("Violence");
            mLabelAnnotation.setMid(mSafeSearchAnnotation.getViolence());
            mFaceDetectionList.add(mLabelAnnotation);
        }


        // set array adapter
        mFaceListView = findViewById(R.id.face_detect_listView);
        mFaceListView.setAdapter(new LabelListAdapter(CameraActivity.this, mFaceDetectionList, AppConstants.FACE_ADAPTER));
        setListViewHeightBasedOnChildren(mFaceListView);

    }

    private void checkLabelData(String state)
    {
        if (state.equals(PHOTO_NAME))
        {
            mLabelListView = findViewById(R.id.label_listView);

            mLabelAnnotation = new EntityAnnotation();

            // add confidence Score at the bottom

            mLabelAnnotation.setDescription("Confidence");

            if (mFaceAnnotation != null)
                mLabelAnnotation.setScore(mFaceAnnotation.getDetectionConfidence());
            else
                mLabelAnnotation.setScore(0.0f);

            mLabelAnnotations.add(mLabelAnnotation);

            mLabelListView.setAdapter(new LabelListAdapter(CameraActivity.this, mLabelAnnotations, AppConstants.LABEL_ADAPTER));
            setListViewHeightBasedOnChildren(mLabelListView);

            visionLayout.setVisibility(View.VISIBLE);
        } else if (state.equals(FOOD_NAME))
        {
            mLabelFoodListView = findViewById(R.id.food_label_listView);

            mLabelAnnotation_food = new EntityAnnotation();

            // add confidence Score at the bottom

//            mLabelAnnotation.setDescription("Confidence");

//            if (mFaceAnnotation != null)
//                mLabelAnnotation.setScore(mFaceAnnotation.getDetectionConfidence());
//            else
//                mLabelAnnotation.setScore(0.0f);

//            mLabelAnnotations_food.add(mLabelAnnotation_food);

            mLabelFoodListView.setAdapter(new LabelListAdapter(CameraActivity.this, mLabelAnnotations_food, AppConstants.FOOD_ADAPTER));
            setListViewHeightBasedOnChildren(mLabelFoodListView);

            foodLayout.setVisibility(View.VISIBLE);
        }

    }

    //**** Method for Setting the Height of the ListView dynamically

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.camerabutton:
                if (isFoodButton || isPhotoButton)
                    return;

                isVideoButton = false;
                isPhotoButton = true;
                isDetectedVision = false;
                isUploadedImage = false;
                askPermission();
                break;
            case R.id.foodbutton:
                if (isFoodButton || isPhotoButton)
                    return;
                isVideoButton = false;
                isFoodButton = true;
                isPhotoButton = false;
                isDetectedVision = false;
                isUploadedImage = false;
                askPermission();
                break;
            case R.id.accredited_investor_portal:
                gotoIdDocActivity();
                break;
            case R.id.btnstartmining:
                isVideoButton = true;
                isPhotoButton = false;
                isFoodButton = false;
                askPermission();
                break;
            case R.id.email_files:
                sendFiles();
                break;
            case R.id.btncontinue:
                gotoMainActivity();
                break;
            case R.id.start_chat:
                gotoChatActivity();
                break;
            case R.id.createpincode:

                Intent intent = new Intent(this, PinEntryActivity.class);
                intent.putExtra("create", true);
                startActivity(intent);
                finish();
                break;
            case R.id.removepincode:
                PrefsUtil.getInstance(this).setValue(PrefsUtil.SCRAMBLE_PIN, false);
                Toast.makeText(this, "PIN code has been removed.", Toast.LENGTH_SHORT).show();
                btnCreatePin.setVisibility(View.VISIBLE);
                btnRemovePin.setVisibility(View.GONE);
                btnChangePin.setVisibility(View.GONE);
                break;
            case R.id.changepincode:
                changePinCode();
                break;
            case R.id.btntrack:
                handleOpenCV();
                break;

        }
    }

    private void handleOpenCV() {

        CameraPreview.currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        Intent intent = new Intent(this, OpenCVActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoIdDocActivity() {
        Intent intent = new Intent(this, IdDocUploadActivity.class);
        startActivity(intent);
        finish();
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void changePinCode()
    {
        new AlertDialog.Builder(this,R.style.DateTImePicker)
                .setTitle(R.string.app_name)
                .setMessage("Are you sure you want to change your PIN?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(CameraActivity.this, PinEntryActivity.class);
                        intent.putExtra("changepin", true);
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

    private void sendFiles() {

        if (!isGenerateHTMLString())
            return;

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        ArrayList<Uri> uris = new ArrayList<>();
        String appID = getApplicationContext().getPackageName();
        String auth = appID+".fileprovider";

        if (photoFile.exists()){
            Uri photoUri = FileProvider.getUriForFile(this, auth, photoFile);
            uris.add(photoUri);
        }

        if (foodFile.exists()){
            Uri foodUri = FileProvider.getUriForFile(this, auth, foodFile);
            uris.add(foodUri);
        }

        if (videoFile.exists())
        {
            Uri videoUri = FileProvider.getUriForFile(this, auth, videoFile);
            uris.add(videoUri);
        }

        if (htmlFile.exists())
        {
            Uri htmlUri = FileProvider.getUriForFile(this, auth, htmlFile);
            uris.add(htmlUri);
        }

        try {
            // the attachments
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
// set the type to 'email'
            emailIntent .setType(getResources().getString(R.string.email_text_type));
            String to[] = {current_email};
            emailIntent .putExtra(Intent.EXTRA_EMAIL, to);

            String cc[] = {current_email};
            emailIntent.putExtra(Intent.EXTRA_CC, cc);

// the mail subject
            String subjectString = getResources().getString(R.string.email_send_reciept) + getDateTime();
            emailIntent .putExtra(Intent.EXTRA_SUBJECT, subjectString);
            emailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.extra_text));

            startActivity(Intent.createChooser(emailIntent , getResources().getString(R.string.chooser_title)));

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.email_send_error, Toast.LENGTH_SHORT).show();
        }



    }

    private String getDateTime() {

        String fullDay = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd MMM yyyy hh:mm a", Locale.getDefault());
        Date date = new Date();

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day)
        {
            case Calendar.SUNDAY:
                fullDay = "Sunday " + dateFormat.format(date);
                break;
            case Calendar.MONDAY:
                fullDay = "Monday " + dateFormat.format(date);
                break;
            case Calendar.TUESDAY:
                fullDay = "Tuesday " + dateFormat.format(date);
                break;
            case Calendar.WEDNESDAY:
                fullDay = "Wednesday " + dateFormat.format(date);
                break;
            case Calendar.THURSDAY:
                fullDay = "Thursday " + dateFormat.format(date);
                break;
            case Calendar.FRIDAY:
                fullDay = "Friday " + dateFormat.format(date);
                break;
            case Calendar.SATURDAY:
                fullDay = "Saturday " + dateFormat.format(date);
                break;

        }
        return fullDay;
    }

    private void gotoChatActivity() {

        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
        finish();
    }

    private void askPermission() {

        imageHelper = new ImageHelper();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) +
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, REQUEST_CAMERA_PERMISSION);

            }
            else {
                if (isPhotoButton || isFoodButton)
                {
                    takePhoto();

                } else if (isVideoButton)
                {
                    isVideoButton = false;
                    gotoRecordActivity();
                }

            }
        } else {

            if (isPhotoButton || isFoodButton)
            {
                takePhoto();

            } else if (isVideoButton)
            {
                isVideoButton = false;
                gotoRecordActivity();
            }
        }
    }

    private void takePhoto() {
        try {

            if (!getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                exitApp("Camera not detected. App must terminate.");
            } else {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_CAMERA_CAPTURE);

            }

        } catch (Exception e) {
            Toast.makeText(this, "Error connecting to camera service", Toast.LENGTH_LONG).show();
        }
    }

    private void gotoRecordActivity() {

        cameraId = findFrontFacingCamera();

        VideoRecorder.isFrontCamera = cameraId >= 0;
        Intent cameraIntent = new Intent(this, VideoActivity.class);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0
                        && grantResults[0] == 0 && grantResults[1] == 0) {

                    if (isPhotoButton)
                    {
                        takePhoto();

                    } else if (isVideoButton)
                    {
                        isVideoButton = false;
                        gotoRecordActivity();
                    }

                } else {

                    exitApp("Permission not granted. App must terminate.");

                }
            }
        }
    }

    private void exitApp(String message) {

        new AlertDialog.Builder(this,R.style.DateTImePicker)
                .setTitle("Warning")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(CameraActivity.this, SignInActivity.class);
                        intent.putExtra("EXIT", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();

                    }
                })

                .show();
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
                        Intent intent = new Intent(CameraActivity.this, SignInActivity.class);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            Bitmap bitmap;
            if (requestCode == REQUEST_CAMERA_CAPTURE && data != null) {

                Bundle dataExtras = data.getExtras();
                if (dataExtras == null) return;
                bitmap = (Bitmap) dataExtras.get("data");

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                for (int i=0; i<100; i++)
                {
                    if (bitmap == null) return;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100 - i, bytes);
                    byte[] imageinByte = bytes.toByteArray();
                    long lengthbmp = imageinByte.length; // in KB size
                    if (lengthbmp <= 400)
                    {
                        Log.v("image size", ""+lengthbmp+" KB");

                        processImage(bitmap, imageinByte);

                        break;
                    } else if (i==99) {

                        processImage(bitmap, imageinByte);

                    }

                }

            }
        }

    }

    private void processImage(Bitmap bitmap, byte[] imageInByte) {

        if (isPhotoButton)
        {
            selectedUser.setWidth(bitmap.getWidth());
            selectedUser.setHeight(bitmap.getHeight());

            storeBitmapSize();

            isTakenPicture = true;
        } else {
            isTakenFood = true;
        }


        handleGoogleCloudVision(bitmap);
        uploadImage(imageInByte);
    }

    private void handleGoogleCloudVision(Bitmap bitmap)
    {

        new CallVision(bitmap).execute();

    }


    private void showImage() {

        if (pdimage_file!=null)
            pdimage_file.dismiss();
        createRoundedImageView(imvSelfie);
        imvSelfie.setVisibility(View.VISIBLE);
        displayImage(imvSelfie, photoFile);
//        setEnable(btnFood);

    }

    private void setVisableCheckbox()
    {
        if (previewWidth > 0 && previewHeight > 0)
        {
            overlayCheckBox.setVisibility(View.VISIBLE);
            overlayCheckBox.setChecked(true);
        } else
        {
            overlayCheckBox.setVisibility(View.GONE);
        }
    }

    private void createRoundedImageView(GraphicOverlayView imv) {

        imv.setBackgroundResource(R.drawable.tags_rounded_corners);

        GradientDrawable drawable = (GradientDrawable) imv.getBackground();
        drawable.setColor(Color.parseColor("#296d33"));
    }

    private void uploadImage(byte[] data) {


        if(data != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            final StorageReference ref;
            if (isFoodButton)
                ref = refFood;
            else
                ref = refphoto;

            ref.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(CameraActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                            isUploadedImage = true;
                            if (isDetectedVision) {
                                isPhotoButton = false;
                                isFoodButton = false;
                                isDetectedVision = false;
                                isUploadedImage = false;
                            }

                            if (ref == refFood) {
                                handleFood();
                            }
                            else
                            {
                                handleImage();
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(CameraActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void storeBitmapSize() {

        // update w and h for new image

        previewWidth = selectedUser.getWidth();
        previewHeight = selectedUser.getHeight();

        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(uid).setValue(selectedUser);
    }

    private boolean isGenerateHTMLString()
    {
        // all parameters to use in html ******************

        boolean value = false;
        StringBuilder returnStr = new StringBuilder();

        String appName = "Calorie Miner Session";
        String sessionID = uid;
        String name = selectedUser.getUserName();
        String phone_number = selectedUser.getphoneNumber();
        String email = selectedUser.getUserEmail();
        String gpslatlong = String.format(Locale.US,"%.6f, %.6f", selectedUser.getlatitude(), selectedUser.getlongitude());
        String gpsTimestamp = selectedUser.getTimeStamp();
        String imgURL = firebasePhotUrl == null ? "No Image":firebaseFoodUrl;
        String videoURL = firebaseVideoUrl == null ? "No Video":firebaseVideoUrl;
//        String appName = R.string.app_name;

        // ----------------------------- ******************

        returnStr.append("<!DOCTYPE html>\n" + "<html>\n" + "<body>\n" + "\n" + "<h4>")
                .append(appName)
                .append("</h4>\n")
                .append("<p>Session ID: ")
                .append(sessionID)
                .append("</p>\n")
                .append("<p>Name: ")
                .append(name)
                .append("</p>\n");

        if (RemoteConfigParam.getInstance(this).getPhoneauth_enabled() && phone_number!= null && !phone_number.matches("")) {
            returnStr.append("<p>Phone: ")
                    .append(phone_number)
                    .append("</p>\n");
        }

        returnStr.append("<p >Email: <a href=\"").append(email).append("\">").append(email).append("</a> </p>\n");

        if (RemoteConfigParam.getInstance(this).getMap_location_enabled())
        {
            returnStr.append("<p>GPS: ").append(gpslatlong).append("</p>\n").append("<p>GPS Timestamp: ").append(gpsTimestamp).append("</p>\n");
        }

        if (imgURL != null && !imgURL.matches(""))
        {
            returnStr.append("<p>Image:</p>\n" + "<a href=\"").append(imgURL).append("\">").append(imgURL).append("</a>\n");
        }

        if (videoURL != null && !videoURL.matches(""))
        {
            returnStr.append("<p>Video:</p>\n" + "<a href=\"").append(videoURL).append("\">\"").append(videoURL).append("\"</a>\n").append("\n").append("</body>\n").append("</html>");
        }

        if (rootFile()==null)
            return value;


        htmlFile = new File(rootFile().getPath() + File.separator + AppConstants.HTML_NAME + ".html");

        try {
            FileWriter writer = new FileWriter(htmlFile);
            writer.append(returnStr.toString());
            writer.flush();
            writer.close();

            value = true;
            // enable this button only when there exits 3 files (video, html and photo)
//            setEnable(btnSendFiles);

        } catch (IOException e) {
            e.printStackTrace();
            value = false;
        }

        return value;
    }

    @SuppressLint("StaticFieldLeak")
    private class CallVision extends AsyncTask<Void, Void, String> {

        private final Bitmap bitmap;

        CallVision(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        protected String doInBackground(Void... voids) {

            try {
//                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                NetHttpTransport httpTransport = new NetHttpTransport();
//                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
                AndroidJsonFactory jsonFactory = new AndroidJsonFactory();
                VisionRequestInitializer requestInitializer =
                        new VisionRequestInitializer(getResources().getString(R.string.google_key)) {
                            /**
                             * We override this so we can inject important identifying fields into the HTTP
                             * headers. This enables use of a restricted cloud platform API key.
                             */
                            @Override
                            protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                    throws IOException {
                                super.initializeVisionRequest(visionRequest);

                                String packageName = getPackageName();
                                visionRequest.getRequestHeaders().set(AppConstants.ANDROID_PACKAGE_HEADER, packageName);

                                String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                visionRequest.getRequestHeaders().set(AppConstants.ANDROID_CERT_HEADER, sig);
                            }
                        };

                Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                builder.setVisionRequestInitializer(requestInitializer);

                Vision vision = builder.build();
                
                BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                        new BatchAnnotateImagesRequest();

                batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                    AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                    // Add the image
                    com.google.api.services.vision.v1.model.Image base64EncodedImage = new com.google.api.services.vision.v1.model.Image();
                    // Convert the bitmap to a JPEG
                    // Just in case it's a format that Android understands but Cloud Vision
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                    byte[] imageBytes = byteArrayOutputStream.toByteArray();

                    // Base64 encode the JPEG
                    base64EncodedImage.encodeContent(imageBytes);
                    annotateImageRequest.setImage(base64EncodedImage);

                    // add the features we want
                    annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                        Feature faceDetection = new Feature();

                        if (isPhotoButton)
                        {
                            faceDetection.setType(AppConstants.FACE_DETECTION);
                            faceDetection.setMaxResults(15);
                            add(faceDetection);
                        }


                        Feature labelDetection = new Feature();
                        labelDetection.setType(AppConstants.LABEL_DETECTION);
                        labelDetection.setMaxResults(15);
                        add(labelDetection);

                        if (isPhotoButton)
                        {
                            Feature safeSearch = new Feature();
                            safeSearch.setType(AppConstants.SAFE_SEARCH_DETECTION);
                            safeSearch.setMaxResults(5);
                            add(safeSearch);
                        }


                    }});

                    // Add the list of one thing to the request
                    add(annotateImageRequest);
                }});

                Vision.Images.Annotate annotateRequest =
                        vision.images().annotate(batchAnnotateImagesRequest);
                // Due to a bug: requests to Vision API containing large images fail when GZipped.
                annotateRequest.setDisableGZipContent(true);
                Log.d(TAG, "created Cloud Vision request object, sending request");

                BatchAnnotateImagesResponse response = annotateRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }

            return "Cloud Vision API request failed.";

        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals(getResources().getString(R.string.analysispicture)))
            {
                if (isPhotoButton)
                    detect_face();
                else if (isFoodButton)
                    detect_food();
            }
        }
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {

        String message = getResources().getString(R.string.analysispicture);

        // upload face data


        List<FaceAnnotation> faceAnnotations = response.getResponses().get(0).getFaceAnnotations();

//        WebDetection webDetections = response.getResponses().get(0).getWebDetection();
        List<EntityAnnotation> labelAnnotations = response.getResponses().get(0).getLabelAnnotations();
//        List<EntityAnnotation> landmarkAnnotations = response.getResponses().get(0).getLandmarkAnnotations();
        mSafeSearchAnnotation = response.getResponses().get(0).getSafeSearchAnnotation();

        if (labelAnnotations != null)
        {
            if (isPhotoButton) {
                dBRef_face.child(AppConstants.LABEL_DETECTION).setValue(labelAnnotations);
                mLabelAnnotations = (ArrayList<EntityAnnotation>) labelAnnotations;
            }

            if (isFoodButton)
            {
                dBRef_food.child(AppConstants.LABEL_DETECTION).setValue(labelAnnotations);
                mLabelAnnotations_food = (ArrayList<EntityAnnotation>) labelAnnotations;
            }

        }

        if (isPhotoButton) {
            if (faceAnnotations != null) {

                dBRef_face.child(AppConstants.FACE_DETECTION).setValue(faceAnnotations);
                mFaceAnnotation = faceAnnotations.get(0);
            }

            if (mSafeSearchAnnotation != null)
            {
                dBRef_face.child(AppConstants.SAFE_SEARCH_DETECTION).setValue(mSafeSearchAnnotation);
            }

        }

        return message;

    }

}