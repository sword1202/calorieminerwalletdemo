package com.calorieminer.minerapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.calorieminer.minerapp.CustomClass.PrefsUtil;
import com.calorieminer.minerapp.CustomClass.RemoteConfigParam;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.System.exit;

public class SplashActivity extends AppCompatActivity{

    Timer mTimer;
    long elapsedSecond = 0;
    public static final int STORAGE_PERMISSIONS = 10;
    String[] permissions= new String[]{
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState);
        }

        initialRequestPermission();
    }

    private void initialRequestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) +
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(permissions, STORAGE_PERMISSIONS);

            } else
            {
                RemoteConfigParam.getInstance(this).initRemoteConfig();
                checkPinUpdateUI();
            }
        } else
        {
            RemoteConfigParam.getInstance(this).initRemoteConfig();
            checkPinUpdateUI();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case STORAGE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0
                        && grantResults[0] == 0 && grantResults[1] == 0) {

                    if (!isCreatedDir()) {
                        exitApp("Directory not created. App must terminate.");
                        return;
                    }

                    RemoteConfigParam.getInstance(this).initRemoteConfig();
                    checkPinUpdateUI();

                } else {

                    exitApp("Permission not granted. App must terminate.");

                }
            }
        }
    }

    private void exitApp(String msg) {

        new AlertDialog.Builder(this,R.style.DateTImePicker)
                .setTitle("Warning")
                .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exit(0);
                    }
                })

                .show();
    }

    private void checkPinUpdateUI() {

        RemoteConfigParam.getInstance(this).setStatus(false);
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, 0, 1000);

    }

    private void TimerMethod() {
        elapsedSecond++;
        if (RemoteConfigParam.getInstance(SplashActivity.this).getStatus())
        {
            mTimer.cancel();
            updateUI();
        } else
        {
            if (elapsedSecond >= 20)
            {
                mTimer.cancel();
                updateUI();
            }
        }
    }

    private boolean isCreatedDir()
    {
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), getResources().getString(R.string.app_name));
        if (! storageDir.exists()){
            if (! storageDir.mkdirs()){
                Log.d(getResources().getString(R.string.app_name), "failed to create directory");
                return false;

            }
        }

        return true;
    }

    private void updateUI()
    {
        boolean isStoredPIN = PrefsUtil.getInstance(SplashActivity.this).getValue(PrefsUtil.SCRAMBLE_PIN, false);
        boolean isEnalbedPIN = RemoteConfigParam.getInstance(SplashActivity.this).getPinEnabled();
        boolean isEnalbedPhoneAuth = RemoteConfigParam.getInstance(SplashActivity.this).getPhoneauth_enabled();
        boolean isMapEnabled = RemoteConfigParam.getInstance(SplashActivity.this).getMap_location_enabled();

        if (isStoredPIN && isEnalbedPIN)
        {
            startActivity(new Intent(SplashActivity.this, PinEntryActivity.class));

        } else
        {
            if (isEnalbedPhoneAuth)
                startActivity(new Intent(SplashActivity.this, PhoneActivity.class));

            else {

                if (isMapEnabled)
                    startActivity(new Intent(SplashActivity.this, Hallfinder.class));

                else{
                    startActivity(new Intent(SplashActivity.this, SignInActivity.class));
//                    startActivity(new Intent(SplashActivity.this, OpenCVActivity.class));
                }

            }

        }

        finish();
    }
}
