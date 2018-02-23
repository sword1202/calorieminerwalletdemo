package com.calorieminer.minerapp.CustomClass;

import android.content.Context;

import org.bouncycastle.util.encoders.Hex;

import java.security.MessageDigest;
import java.util.UUID;

public class AccessFactory {

    public static final int MIN_PIN_LENGTH = 1;
    public static final int MAX_PIN_LENGTH = 8;

    private static Context context = null;
    private static AccessFactory instance = null;

    private AccessFactory()	{ ; }

    public static AccessFactory getInstance(Context ctx) {

        context = ctx;

        if (instance == null) {
            instance = new AccessFactory();
        }

        return instance;
    }

    public static AccessFactory getInstance() {

        if (instance == null) {
            instance = new AccessFactory();
        }

        return instance;
    }


}
