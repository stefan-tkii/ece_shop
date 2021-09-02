package com.example.eceshop;

public class CartItem
{

    private String productId;
    private String name;
    private String shortDesc;
    private String imgUri;
    private Double price;
    private String orders;
    private int inStock;
    private int quantity;
    private boolean isExpanded;

    public CartItem()
    {

    }

    public CartItem(String productId, String name, String shortDesc, String imgUri, Double price, String orders, int inStock, int quantity, boolean isExpanded)
    {
        this.productId = productId;
        this.name = name;
        this.shortDesc = shortDesc;
        this.imgUri = imgUri;
        this.price = price;
        this.orders = orders;
        this.inStock = inStock;
        this.quantity = quantity;
        this.isExpanded = isExpanded;
    }

    public String getProductId()
    {
        return productId;
    }

    public void setProductId(String productId)
    {
        this.productId = productId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getShortDesc()
    {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc)
    {
        this.shortDesc = shortDesc;
    }

    public String getImgUri()
    {
        return imgUri;
    }

    public void setImgUri(String imgUri)
    {
        this.imgUri = imgUri;
    }

    public Double getPrice()
    {
        return price;
    }

    public void setPrice(Double price)
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

    public int getInStock()
    {
        return inStock;
    }

    public void setInStock(int inStock)
    {
        this.inStock = inStock;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public boolean isExpanded()
    {
        return isExpanded;
    }

    public void setExpanded(boolean expanded)
    {
        isExpanded = expanded;
    }

}
