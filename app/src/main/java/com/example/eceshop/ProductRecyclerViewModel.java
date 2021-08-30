package com.example.eceshop;

import android.os.Parcel;
import android.os.Parcelable;

public class ProductRecyclerViewModel implements Parcelable
{

    private String name;
    private String shortDesc;
    private String longDesc;
    private String imgUri;
    private Double price;
    private String orders;
    private String categoryId;
    private int inStock;

    public ProductRecyclerViewModel()
    {

    }

    public ProductRecyclerViewModel(String name, String shortDesc, String longDesc, String imgUri, Double price, String orders, String categoryId, int inStock)
    {
        this.name = name;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
        this.imgUri = imgUri;
        this.price = price;
        this.orders = orders;
        this.categoryId = categoryId;
        this.inStock = inStock;
    }

    protected ProductRecyclerViewModel(Parcel in)
    {
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

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
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

    @Override
    public int describeContents()
    {
        return 0;
    }

    public static final Creator<ProductRecyclerViewModel> CREATOR = new Creator<ProductRecyclerViewModel>()
    {
        @Override
        public ProductRecyclerViewModel createFromParcel(Parcel in)
        {
            return new ProductRecyclerViewModel(in);
        }

        @Override
        public ProductRecyclerViewModel[] newArray(int size)
        {
            return new ProductRecyclerViewModel[size];
        }
    };

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

}
