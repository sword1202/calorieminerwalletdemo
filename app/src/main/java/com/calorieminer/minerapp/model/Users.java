package com.calorieminer.minerapp.model;

/**
 * Created by p1 on 12/6/17.
 */

public class Users {
    public String userName;
    public String userEmail;
    public String phoneNumber;
    public String uid;
    public double latitude;
    public double longitude;
    public String timeStamp;
    public int width;
    public int height;
    public String isUpdatedImage;
    public String isUpdatedVideo;

    public Users()
    {

    }

    public Users(String uid, String userName, String userEmail, String phoneNumber, double latitude,
                 double longitude, String timeStamp, int width, int height, String isUpdatedImage, String isUpdatedVideo)
    {
        this.uid = uid;
        this.userEmail = userEmail;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeStamp = timeStamp;
        this.width = width;
        this.height = height;
        this.isUpdatedImage = isUpdatedImage;
        this.isUpdatedVideo = isUpdatedVideo;
    }

    public String getUID()
    {
        return uid;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getUserEmail()
    {
        return userEmail;
    }

    public String getphoneNumber()
    {
        return phoneNumber;
    }

    public double getlatitude()
    {
        return latitude;
    }

    public double getlongitude()
    {
        return longitude;
    }

    public String getTimeStamp()
    {
        return timeStamp;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setUID(String uid)
    {
        this.uid = uid;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public void setUserEmail(String userEmail)
    {
        this.userEmail = userEmail;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public void setlat(double lat)
    {
        this.latitude = lat;
    }

    public void setlon(double lon)
    {
        this.longitude = lon;
    }

    public void setTimeStamp(String timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public void setHeight (int height)
    {
        this.height= height;
    }

    public void setUpdatedImage(String isUpdatedImage)
    {
        this.isUpdatedImage = isUpdatedImage;
    }

    public void setUpdatedVideo(String isUpdatedVideo)
    {
        this.isUpdatedVideo = isUpdatedVideo;
    }
}
