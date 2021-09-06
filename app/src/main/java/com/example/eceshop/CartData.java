package com.example.eceshop;

public class CartData
{
    private long timestamp;
    private double price;
    private int orders;

    public CartData()
    {

    }

    public CartData(long timestamp, double price, int orders)
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

    public int getOrders()
    {
        return orders;
    }

    public void setOrders(int orders)
    {
        this.orders = orders;
    }

}
