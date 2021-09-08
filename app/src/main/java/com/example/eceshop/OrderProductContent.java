package com.example.eceshop;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderProductContent implements Parcelable
{

    private String productId;
    private double price;
    private int quantity;

    public OrderProductContent()
    {

    }

    public OrderProductContent(String productId, double price, int quantity)
    {
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
    }

    protected OrderProductContent(Parcel in)
    {
        productId = in.readString();
        price = in.readDouble();
        quantity = in.readInt();
    }

    public static final Creator<OrderProductContent> CREATOR = new Creator<OrderProductContent>()
    {
        @Override
        public OrderProductContent createFromParcel(Parcel in)
        {
            return new OrderProductContent(in);
        }

        @Override
        public OrderProductContent[] newArray(int size)
        {
            return new OrderProductContent[size];
        }
    };

    public String getProductId()
    {
        return productId;
    }

    public void setProductId(String productId)
    {
        this.productId = productId;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(productId);
        dest.writeDouble(price);
        dest.writeInt(quantity);
    }

}
