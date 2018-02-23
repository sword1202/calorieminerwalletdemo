package com.calorieminer.minerapp.model;

public class Starter {

    public String userName;
    public String userEmail;
    public String joinerEmail;
    public String uid;

    public Starter()
    {

    }

    public Starter(String uid, String userName, String userEmail, String joinerEmail)
    {
        this.uid = uid;
        this.userEmail = userEmail;
        this.userName = userName;
        this.joinerEmail = joinerEmail;
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

    public String getjoinerEmail()
    {
        return joinerEmail;
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

    public void setJoinerEmail(String joinerEmail)
    {
        this.joinerEmail = joinerEmail;
    }

}
