package com.example.eceshop;

import java.util.Map;

public class OrderDb
{

    private String address;
    private String paymentId;
    private String paymentMethodId;
    private Map<String, OrderProductContentDb> products;
    private String status;
    private long timestamp;

    public OrderDb()
    {

    }

    public OrderDb(String address, String paymentId, String paymentMethodId, Map<String, OrderProductContentDb> products, String status, long timestamp)
    {
        this.address = address;
        this.paymentId = paymentId;
        this.paymentMethodId = paymentMethodId;
        this.products = products;
        this.status = status;
        this.timestamp = timestamp;
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

    public Map<String, OrderProductContentDb> getProducts()
    {
        return products;
    }

    public void setProducts(Map<String, OrderProductContentDb> products)
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

}
