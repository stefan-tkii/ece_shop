package com.example.eceshop;

public class CartData
{
    private long timestamp;
    private double price;
    private String orders;

    public CartData()
    {

    }

    public CartData(long timestamp, double price, String orders)
    {
        this.timestamp = timestamp;
        this.price = price;
        this.orders = orders;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public String getOrders()
    {
        return orders;
    }

    public void setOrders(String orders)
    {
        this.orders = orders;
    }

}
