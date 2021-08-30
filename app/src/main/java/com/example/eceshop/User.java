package com.example.eceshop;

public class User {

    private String fullName;
    private String email;
    private String phoneNumber;
    private String country;

    public User()
    {

    }

    public User(String fullName, String email, String phoneNumber, String country)
    {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.country = country;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

}
