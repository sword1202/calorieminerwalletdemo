package com.calorieminer.minerapp.model;

public class Joiner {

    public String userName;
    public String userEmail;
    public String starterEmail;
    public String uid;

    public Joiner()
    {

    }

    public Joiner(String uid, String userName, String userEmail, String starterEmail)
    {
        this.uid = uid;
        this.userEmail = userEmail;
        this.userName = userName;
        this.starterEmail = starterEmail;
    }

    public String getUid()
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

    public String getstarterEmail()
    {
        return starterEmail;
    }

    public void setUid(String uid)
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

    public void setStarterEmail(String starterEmail)
    {
        this.starterEmail = starterEmail;
    }

}
