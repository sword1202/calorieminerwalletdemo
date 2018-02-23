package com.calorieminer.minerapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.calorieminer.minerapp.CustomClass.AppConstants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
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
import com.google.api.services.vision.v1.model.TextAnnotation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IdDocUploadActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

    static final String TAG = "IdDocUploadActivity";
    static final int REQUEST_CAMERA_CAPTURE = 10;
    boolean isId = false;
    boolean isIncomeDoc = false;
    boolean isAssetsDoc = false;
    boolean isUploadedDocFIle = false;
    boolean isUploadedDocData = false;


    private Button btn_doc;
    private RadioButton rBtnIncomeDoc, rBtnAssetDoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_id_doc);

        init();

    }

    private void init() {

        Button btn_id = findViewById(R.id.btn_id);
        btn_id.setOnClickListener(this);

        btn_doc = findViewById(R.id.btn_doc);
        btn_doc.setOnClickListener(this);
        setDisable(btn_doc);

        rBtnIncomeDoc = findViewById(R.id.radio_income_doc);
        rBtnAssetDoc = findViewById(R.id.radio_asset_doc);

        rBtnIncomeDoc.setOnCheckedChangeListener(this);
        rBtnAssetDoc.setOnCheckedChangeListener(this);

        setDisable(rBtnIncomeDoc);
        setDisable(rBtnAssetDoc);
    }


    private void setEnable(Button buttone) {
        buttone.setEnabled(true);
        buttone.setAlpha(1.0f);
    }

    private void setDisable(Button buttone) {
        buttone.setEnabled(false);
        buttone.setAlpha(0.3f);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.btn_id:
                isAssetsDoc = false;
                isIncomeDoc = false;
                isId = true;
                break;
            case R.id.btn_doc:
                break;
        }

        takePhoto();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        setEnable(btn_doc);
        isId = false;
        isIncomeDoc = false;
        isAssetsDoc = false;

        switch (compoundButton.getId())
        {
            case R.id.radio_income_doc:
                if (b) {
                    isIncomeDoc = true;
                    rBtnAssetDoc.setChecked(false);
                }
                break;
            case R.id.radio_asset_doc:
                if (b) {
                    isAssetsDoc = true;
                    rBtnIncomeDoc.setChecked(false);
                }
                break;
        }


    }

    private void takePhoto() {
        try {
            if (!getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                exitApp();
            } else {

                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_CAMERA_CAPTURE);

            }

        } catch (Exception e) {
            Toast.makeText(this, "Error connecting to camera service", Toast.LENGTH_LONG).show();
        }
    }

    private void exitApp() {

        new AlertDialog.Builder(this,R.style.DateTImePicker)
                .setTitle(R.string.warning)
                .setMessage(R.string.terminate_app)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(IdDocUploadActivity.this, SignInActivity.class);
                        intent.putExtra("EXIT", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();

                    }
                })

                .show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            Bitmap bitmap;
            if (requestCode == REQUEST_CAMERA_CAPTURE && data != null) {

                Bundle extras = data.getExtras();
                if (extras == null)
                    return;
                bitmap = (Bitmap) extras.get("data");

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();

                if (bitmap == null)
                    return;

                for (int i=0; i<100; i++)
                {
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

        handleGoogleCloudVision(bitmap);

        uploadImage(imageInByte);
    }

    private void uploadImage(byte[] data) {

        StorageReference refphoto;
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null)
            return;
        String currentEmail = mUser.getEmail();
        String imgName;

        imgName = isId ? AppConstants.ID_NAME:AppConstants.DOCUMENT_NAME;

        refphoto = FirebaseStorage.getInstance().getReference().child("images/"+ currentEmail + "/" + imgName);

        if(data != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            refphoto.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(IdDocUploadActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            if (!isId) {
                                isUploadedDocFIle = true;
                                if (isUploadedDocData)
                                    goBack();

                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(IdDocUploadActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void handleGoogleCloudVision(Bitmap bitmap)
    {

        new CallVision(bitmap).execute();

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
                HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

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

                        Feature labelDetection = new Feature();
                        labelDetection.setType(AppConstants.LABEL_DETECTION);
                        labelDetection.setMaxResults(15);
                        add(labelDetection);

                        if (isId)
                        {
                            Feature faceDetection = new Feature();
                            faceDetection.setType(AppConstants.FACE_DETECTION);
                            faceDetection.setMaxResults(15);
                            add(faceDetection);

                            Feature safeSearch = new Feature();
                            safeSearch.setType(AppConstants.SAFE_SEARCH_DETECTION);
                            safeSearch.setMaxResults(5);
                            add(safeSearch);
                        }

                        Feature textDetection = new Feature();
                        textDetection.setType(AppConstants.DOCUMENT_TEXT_DETECTION);
                        textDetection.setMaxResults(15);
                        add(textDetection);

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
            if (result == null)
                return;

            if (result.matches(getResources().getString(R.string.analysispicture)))
                if (isId)
                {
                    setEnable(rBtnIncomeDoc);
                    setEnable(rBtnAssetDoc);
                } else
                {
                    isUploadedDocData = true;
                    if (isUploadedDocFIle)
                        goBack();
                }
        }
    }

    @Override
    public void onBackPressed() {
        showAlert();
    }

    private void goBack() {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("id_doc_extra", true);
        startActivity(intent);
        finish();
    }

    private void showAlert() {

        new AlertDialog.Builder(this,R.style.DateTImePicker)
                .setTitle(null)
                .setMessage(R.string.back_button_message)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(IdDocUploadActivity.this, SignInActivity.class);
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

    private String convertResponseToString(BatchAnnotateImagesResponse response) {

        String message = getResources().getString(R.string.analysispicture);

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null)
            return null;

        String uid = mUser.getUid();
        DatabaseReference dBRef;

        if (isId)
            dBRef = FirebaseDatabase.getInstance().getReference().child(AppConstants.DATA_FIELD).child(uid).child(AppConstants.ID_NAME);
        else
            dBRef = FirebaseDatabase.getInstance().getReference().child(AppConstants.DATA_FIELD).child(uid).child(AppConstants.DOCUMENT_NAME);

        List<EntityAnnotation> labelAnnotations = response.getResponses().get(0).getLabelAnnotations();

        if (labelAnnotations != null)
        {
            dBRef.child(AppConstants.LABEL_DETECTION).setValue(labelAnnotations);
        }

        if (isId)
        {
            List<FaceAnnotation> faceAnnotations = response.getResponses().get(0).getFaceAnnotations();

            SafeSearchAnnotation mSafeSearchAnnotation = response.getResponses().get(0).getSafeSearchAnnotation();
            if (faceAnnotations != null) {

                dBRef.child(AppConstants.FACE_DETECTION).setValue(faceAnnotations);
            }


            if (mSafeSearchAnnotation != null)
            {
                dBRef.child(AppConstants.FACE_DETECTION).setValue(mSafeSearchAnnotation);
            }
        } else
        {
            TextAnnotation txtAnnotation = response.getResponses().get(0).getFullTextAnnotation();
            List<EntityAnnotation> textAnnotations = response.getResponses().get(0).getTextAnnotations();
            if (textAnnotations != null)
                dBRef.child(AppConstants.DOCUMENT_TEXT_DETECTION).setValue(textAnnotations);
            if (txtAnnotation != null)
                dBRef.child(AppConstants.FULL_TEXT_DETECTION).setValue(txtAnnotation);
        }

        return message;
    }
}
