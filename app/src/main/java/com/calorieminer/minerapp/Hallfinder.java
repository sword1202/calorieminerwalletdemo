package com.calorieminer.minerapp;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Hallfinder extends BaseMapActivity implements View.OnClickListener {

    private Button mContinueButton;
    private FirebaseAuth mAuth;


    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private TextView latituteField;
    private TextView longitudeField;
    private TextView statusField;

    LatLng currentPosition;
    Marker currentMarker;

    public GPSTracker gps;
    private GoogleMap mMap;

    protected int getLayoutId() {
        return R.layout.activity_hallfinder;
    }

    @Override
    protected void startMap() {

        isConfirmedPIN();

        init();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermission();
        } else {
            startApp();
        }

    }

    private void isConfirmedPIN() {

        Bundle extras = getIntent().getExtras();
        SignInActivity.isConfirmedPIN = false;
        if (extras != null && extras.containsKey("confirmPIN") && extras.getBoolean("confirmPIN")) {
            SignInActivity.isConfirmedPIN = true;
        }
    }

    private void init() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Calorie Miner Wallet Demo - Rig Location");
        }

        mContinueButton = findViewById(R.id.btncontinue);
        mContinueButton.setOnClickListener(this);

//        mContinueButton.setClickable(false);
//        mContinueButton.setAlpha(0.3f);

        mAuth = FirebaseAuth.getInstance();

        latituteField = findViewById(R.id.TextView02);
        longitudeField = findViewById(R.id.TextView04);
        TextView timestampField = findViewById(R.id.TextView06);
        statusField = findViewById(R.id.tv_status);

        statusField.setText("");

        latituteField.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
        longitudeField.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));
        timestampField.setTypeface(Typeface.createFromAsset(this.getAssets(), "Roboto-Regular.ttf"));

        long tsLong = System.currentTimeMillis();
        timestampField.setText(getDate(tsLong));
        CameraActivity.timeStamp = getDate(tsLong);
    }

    @SuppressLint("MissingPermission")
    private void startApp() {

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null)
            return;

        gps = new GPSTracker(Hallfinder.this);
        if (gps.canGetLocation()) {

            try {

                mMap = getMap();

            } catch (Exception e) {
                Log.e("Exception caught", e.toString());
            }

            LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    handlewithLocation(location);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

//            Criteria criteria = new Criteria();
//            if (mLocationManager == null)
//                return;
//            String bestProvider = mLocationManager.getBestProvider(criteria, false);
//
//            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//                return;
//            }
//            Location location = mLocationManager.getLastKnownLocation(bestProvider);
            handlewithLocation(lastLocation);

        } else {
            gps.showSettingsAlert();
            statusField.setText(R.string.not_available);
        }

    }

    @SuppressLint("MissingPermission")
    private void handlewithLocation(Location location) {
        Double lat, lon;
        lat = 0.0;
        lon = 0.0;


        if (location == null)
        {
            lat = gps.getLatitude();
            lon = gps.getLongitude();
        } else
        {
            try {
                lat = location.getLatitude();
                lon = location.getLongitude();

            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        CameraActivity.latitude = lat;
        CameraActivity.longitude= lon;

        currentPosition = new LatLng(lat, lon);
        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.mipmap.location);
        Bitmap b=bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
        currentMarker = mMap.addMarker(new MarkerOptions().position(currentPosition).title("This is my current location").icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 1));

        if (lat == 0.0 && lon == 0.0)
        {
            statusField.setText(R.string.not_available);
        } else
        {
            String strlat = "" + lat;
            String strlon = "" + lon;

            latituteField.setText(strlat);
            longitudeField.setText(strlon);

            statusField.setText(R.string.available);
//            mContinueButton.setAlpha(1.0f);
//            mContinueButton.setClickable(true);
        }

        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setMyLocationEnabled(false);
    }

    private String getDate (long timeStamp)
    {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.US);
            Date netDate = (new Date(timeStamp));
            return sdf.format(netDate);
        }
        catch (Exception e){
            return "exception";
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0
                        && grantResults[0] == 0) {


                    startApp();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    new AlertDialog.Builder(this,R.style.DateTImePicker)
                            .setTitle("Warning")
                            .setMessage("Permission not granted. App must terminate.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    System.exit(0);

                                }
                            })

                            .show();
                }
            }
        }
    }

    private void getPermission()
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                + ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.i("Permission is ", "require first time...OK...getPermission() method!..if");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                            android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_ID_MULTIPLE_PERMISSIONS);

        }
        else
        {
            Log.i("Permission is ", "already granted...Ok...getPermission() method!..else");
            startApp();
        }
    }

    private void signOut() {
        mAuth.signOut();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btncontinue: {
                FirebaseUser mUser = mAuth==null ? null:mAuth.getCurrentUser();
                if (mUser != null)
                    signOut();
                Intent intent = new Intent(Hallfinder.this, SignInActivity.class);
                startActivity(intent);
                finish();


            }
            break;
        }
    }

    @Override
    public void onBackPressed() {
        showAlert();
    }

    private void showAlert() {

        FirebaseUser mUser = mAuth==null ? null:mAuth.getCurrentUser();
        if (mUser != null)
            signOut();
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

        System.exit(0);
    }

}
