package com.calorieminer.minerapp.CustomClass;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.calorieminer.minerapp.BuildConfig;
import com.calorieminer.minerapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

/**
 * Created by p1 on 12/23/17.
 */

public class RemoteConfigParam{

    // remote config data

    private boolean community_chat_enabled;
    private boolean show_accredited_investor_button;
    private double default_fps;
    private boolean phoneauth_enabled;
    private boolean pin_enabled;
    private long max_recording_time;
    private double min_fps;
    private double max_fps;
    private long min_timer;
    private long max_timer;
    private boolean map_location_enabled;
    private long max_winbit_message;


    private boolean status;

    private FirebaseRemoteConfig firebaseRemoteConfig;

    private static Context context = null;
    private static RemoteConfigParam instance = null;

    private RemoteConfigParam() { ; }

    public static RemoteConfigParam getInstance(Context mActivity) {

        context = mActivity;

        if(instance == null) {
            instance = new RemoteConfigParam();
        }

        return instance;
    }

    public void setCommunity_chat_enabled(boolean community_chat_enabled)
    {
        this.community_chat_enabled = community_chat_enabled;
    }

    public void setStatus(boolean status)
    {
        this.status = status;
    }

    public void setShow_accredited_investor_button(boolean show_accredited_investor_button)
    {
        this.show_accredited_investor_button = show_accredited_investor_button;
    }

    public void setDefault_fps(double default_fps)
    {
        this.default_fps = default_fps;
    }

    public void setPhoneauth_enabled(boolean phoneauth_enabled)
    {
        this.phoneauth_enabled = phoneauth_enabled;
    }

    public void setPin_enabled(boolean pin_enabled)
    {
        this.pin_enabled = pin_enabled;
    }

    public void setMin_fps(double min_fps)
    {
        this.min_fps = min_fps;
    }

    public void setMax_fps(double max_fps)
    {
        this.max_fps = max_fps;
    }

    public void setMin_timer(long min_timer)
    {
        this.min_timer = min_timer;
    }

    public void setMax_timer(long max_timer)
    {
        this.max_timer = max_timer;
    }

    public void setMap_location_enabled(boolean map_location_enabled)
    {
        this.map_location_enabled = map_location_enabled;
    }

    public void setMax_recording_time(long max_recording_time)
    {
        this.max_recording_time = max_recording_time;
    }

    public void setMax_winbit_message(long max_winbit_message)
    {
        this.max_winbit_message = max_winbit_message;
    }

    public boolean getCommunity_chat_enabled()
    {
        return community_chat_enabled;
    }

    public boolean getStatus()
    {
        return status;
    }

    public boolean getShow_accredited_investor_button()
    {
        return show_accredited_investor_button;
    }

    public double getDefault_fps()
    {
        return default_fps;
    }

    public boolean getPhoneauth_enabled()
    {
        return phoneauth_enabled;
    }

    public boolean getPinEnabled()
    {
        return pin_enabled;
    }

    public long getMax_recording_time()
    {
        return max_recording_time;
    }

    public double getMin_fps()
    {
        return min_fps;
    }

    public double getMax_fps()
    {
        return max_fps;
    }

    public long getMin_timer()
    {
        return min_timer;
    }

    public long getMax_timer()
    {
        return max_timer;
    }

    public boolean getMap_location_enabled()
    {
        return map_location_enabled;
    }

    public long getMax_winbit_message()
    {
        return max_winbit_message;
    }

    public void initRemoteConfig() {

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        firebaseRemoteConfig.setConfigSettings(configSettings);

        long cacheExpiration = 3600;

        if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        // load local data for exception
        final double minFPS = Double.parseDouble(context.getResources().getString(R.string.min_fps));
        final double maxFPS = Double.parseDouble(context.getResources().getString(R.string.max_fps));
        final long minTimer = Long.parseLong(context.getResources().getString(R.string.min_timer));
        final long maxTimer = Long.parseLong(context.getResources().getString(R.string.max_timer));
        final boolean isEnabledMap = Boolean.parseBoolean(context.getResources().getString(R.string.map_location_enabled));

        // set default values
        instance.setPhoneauth_enabled(false);
        instance.setShow_accredited_investor_button(false);
        instance.setDefault_fps(0.5);
        instance.setMin_fps(minFPS);
        instance.setMax_fps(maxFPS);
        instance.setPin_enabled(true);
        instance.setMax_recording_time(20);
        instance.setMin_timer(minTimer);
        instance.setMax_timer(maxTimer);
        instance.setCommunity_chat_enabled(true);
        instance.setMap_location_enabled(isEnabledMap);
        instance.setMax_winbit_message(120);
        instance.setStatus(false);

        firebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            firebaseRemoteConfig.activateFetched();
                            instance.setPhoneauth_enabled(firebaseRemoteConfig.getBoolean(AppConstants.PHONEAUTH_ENABLED));
                            instance.setShow_accredited_investor_button(firebaseRemoteConfig.getBoolean(AppConstants.SHOW_ACCREDITED_INVESTOR_BUTTON));
                            instance.setDefault_fps(firebaseRemoteConfig.getDouble(AppConstants.DEFAULT_FPS));
                            instance.setPin_enabled(firebaseRemoteConfig.getBoolean(AppConstants.PIN_ENABLED));
                            instance.setMax_recording_time(firebaseRemoteConfig.getLong(AppConstants.MAX_RECORDING_TIME));
                            instance.setCommunity_chat_enabled(firebaseRemoteConfig.getBoolean(AppConstants.COMMUNITY_CHAT_ENABLED));
                            instance.setMin_fps(firebaseRemoteConfig.getDouble(AppConstants.MINFPS));
                            instance.setMax_fps(firebaseRemoteConfig.getDouble(AppConstants.MAXFPS));
                            instance.setMin_timer(firebaseRemoteConfig.getLong(AppConstants.MINTIMER));
                            instance.setMax_timer(firebaseRemoteConfig.getLong(AppConstants.MAX_TIMER));
                            instance.setMap_location_enabled(firebaseRemoteConfig.getBoolean(AppConstants.MAP_LOCATION_ENABLED));
                            instance.setMax_winbit_message(firebaseRemoteConfig.getLong(AppConstants.MAX_WINBIT_MESSAGE));
                            instance.setStatus(true);
                            Log.d("RemoteConfig Params: ", "Accredited Investor Button Show : " + String.valueOf(show_accredited_investor_button)+
                                    "\n Phone  Auth Enabled: " + String.valueOf(phoneauth_enabled) +
                                    "\n Pin  Enabled       : " + String.valueOf(pin_enabled) +
                                    "\n Default FPS        : " + String.valueOf(default_fps) +
                                    "\n Max Recording Time : " + String.valueOf(max_recording_time) +
                                    "\n Chat Enabled       : " + String.valueOf(community_chat_enabled));
                            Log.d("RemoteConfig Params: ", "print values");
                        } else {


                            instance.setStatus(true);
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                instance.setStatus(true);
            }
        });

    }
}
