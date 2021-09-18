package com.example.eceshop;

import android.os.Parcel;
import android.os.Parcelable;

public class UserRvItem implements Parcelable
{

    private String userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String country;
    private int orderCount;
    private boolean hasActiveOrder;

    public UserRvItem()
    {

    }

    public UserRvItem(String userId, String fullName, String email, String phoneNumber, String country, int orderCount, boolean hasActiveOrder)
    {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.country = country;
        this.orderCount = orderCount;
        this.hasActiveOrder = hasActiveOrder;
    }

    protected UserRvItem(Parcel in)
    {
        userId = in.readString();
        fullName = in.readString();
        email = in.readString();
        phoneNumber = in.readString();
        country = in.readString();
        orderCount = in.readInt();
        hasActiveOrder = in.readByte() != 0;
    }

    public static final Creator<UserRvItem> CREATOR = new Creator<UserRvItem>()
    {
        @Override
        public UserRvItem createFromParcel(Parcel in)
        {
            return new UserRvItem(in);
        }

        @Override
        public UserRvItem[] newArray(int size)
        {
            return new UserRvItem[size];
        }
    };

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
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

    public int getOrderCount()
    {
        return orderCount;
    }

    public void setOrderCount(int orderCount)
    {
        this.orderCount = orderCount;
    }

    public boolean isHasActiveOrder()
    {
        return hasActiveOrder;
    }

    public void setHasActiveOrder(boolean hasActiveOrder)
    {
        this.hasActiveOrder = hasActiveOrder;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(userId);
        dest.writeString(fullName);
        dest.writeString(email);
        dest.writeString(phoneNumber);
        dest.writeString(country);
        dest.writeInt(orderCount);
        dest.writeByte((byte) (hasActiveOrder ? 1 : 0));
    }

}
