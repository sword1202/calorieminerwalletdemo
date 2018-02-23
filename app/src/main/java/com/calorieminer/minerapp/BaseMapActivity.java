package com.calorieminer.minerapp;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public abstract class BaseMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    protected int getLayoutId() {
        return R.layout.map;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        setUpMap();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMap();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (mMap != null) {
            return;
        }
        mMap = map;
        startMap();
    }



    private void setUpMap() {
//        SupportMapFragment mFra = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

    }
    /**
     * Run the demo-specific code.
     */
    protected abstract void startMap();

    protected GoogleMap getMap() {
        return mMap;
    }
}
