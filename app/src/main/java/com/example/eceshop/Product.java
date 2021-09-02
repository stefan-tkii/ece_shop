package com.example.eceshop;

import android.os.Parcel;
import android.os.Parcelable;

public class Product implements Parcelable
{
    private String productId;
    private String name;
    private String shortDesc;
    private String longDesc;
    private String imgUri;
    private Double price;
    private String orders;
    private String categoryId;
    private int inStock;

    public Product()
    {

    }

    public Product(String productId, String name, String shortDesc, String longDesc, String imgUri, Double price, String orders, String categoryId, int inStock)
    {
        this.productId = productId;
        this.name = name;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
        this.imgUri = imgUri;
        this.price = price;
        this.orders = orders;
        this.categoryId = categoryId;
        this.inStock = inStock;
    }

    protected Product(Parcel in)
    {
        productId = in.readString();
        name = in.readString();
        shortDesc = in.readString();
        longDesc = in.readString();
        imgUri = in.readString();
        if (in.readByte() == 0)
        {
            price = null;
        }
        else {
            price = in.readDouble();
        }
        orders = in.readString();
        categoryId = in.readString();
        inStock = in.readInt();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>()
    {
        @Override
        public Product createFromParcel(Parcel in)
        {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size)
        {
            return new Product[size];
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

    public String getLongDesc()
    {
        return longDesc;
    }

    public void setLongDesc(String longDesc)
    {
        this.longDesc = longDesc;
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

    public String getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(String categoryId)
    {
        this.categoryId = categoryId;
    }

    public int getInStock()
    {
        return inStock;
    }

    public void setInStock(int inStock)
    {
        this.inStock = inStock;
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
        dest.writeString(name);
        dest.writeString(shortDesc);
        dest.writeString(longDesc);
        dest.writeString(imgUri);
        if (price == null)
        {
            dest.writeByte((byte) 0);
        }
        else {
            dest.writeByte((byte) 1);
            dest.writeDouble(price);
        }
        dest.writeString(orders);
        dest.writeString(categoryId);
        dest.writeInt(inStock);
    }

}
