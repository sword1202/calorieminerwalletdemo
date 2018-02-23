package com.calorieminer.minerapp;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

class ImageHelper {

    ImageHelper(){
    }

    String encodeToBase64(Bitmap img){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] bytes = outputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
}