package com.example.eceshop;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class Order implements Parcelable
{

    private String orderId;
    private String address;
    private String paymentId;
    private String paymentMethodId;
    private List<OrderProductContent> products;
    private String status;
    private long timestamp;

    public Order()
    {

    }

    public Order(String orderId, String address, String paymentId, String paymentMethodId, List<OrderProductContent> products, String status,
                 long timestamp)
    {
        this.orderId = orderId;
        this.address = address;
        this.paymentId = paymentId;
        this.paymentMethodId = paymentMethodId;
        this.products = products;
        this.status = status;
        this.timestamp = timestamp;
    }

    public static Order fromJson(String s)
    {
        return new Gson().fromJson(s, Order.class);
    }

    protected Order(Parcel in)
    {
        orderId = in.readString();
        address = in.readString();
        paymentId = in.readString();
        paymentMethodId = in.readString();
        status = in.readString();
        timestamp = in.readLong();
        products = new ArrayList<OrderProductContent>();
        in.readTypedList(products, OrderProductContent.CREATOR);
    }

    public static final Creator<Order> CREATOR = new Creator<Order>()
    {
        @Override
        public Order createFromParcel(Parcel in)
        {
            return new Order(in);
        }

        @Override
        public Order[] newArray(int size)
        {
            return new Order[size];
        }
    };

    public String getOrderId()
    {
        return orderId;
    }

    public void setOrderId(String orderId)
    {
        this.orderId = orderId;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getPaymentId()
    {
        return paymentId;
    }

    public void setPaymentId(String paymentId)
    {
        this.paymentId = paymentId;
    }

    public String getPaymentMethodId()
    {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId)
    {
        this.paymentMethodId = paymentMethodId;
    }

    public List<OrderProductContent> getProducts()
    {
        return products;
    }

    public void setProducts(List<OrderProductContent> products)
    {
        this.products = products;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public double calculateTotalPrice()
    {
        if(products.size() == 0)
        {
            return 0.0d;
        }
        double total = 0.0d;
        for(OrderProductContent product : products)
        {
            total = total + product.getPrice()*product.getQuantity();
        }
        return  total;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(orderId);
        dest.writeString(address);
        dest.writeString(paymentId);
        dest.writeString(paymentMethodId);
        dest.writeString(status);
        dest.writeLong(timestamp);
        dest.writeTypedList(products);
    }

}
