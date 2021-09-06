package com.example.eceshop;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public class CartItem implements Parcelable
{

    private String productId;
    private String name;
    private String shortDesc;
    private String imgUri;
    private Double price;
    private int orders;
    private int inStock;
    private int quantity;
    private boolean isExpanded;

    public CartItem()
    {

    }

    public CartItem(String productId, String name, String shortDesc, String imgUri, Double price, int orders, int inStock, int quantity, boolean isExpanded)
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

    protected CartItem(Parcel in)
    {
        productId = in.readString();
        name = in.readString();
        shortDesc = in.readString();
        imgUri = in.readString();
        if (in.readByte() == 0)
        {
            price = null;
        }
        else {
            price = in.readDouble();
        }
        orders = in.readInt();
        inStock = in.readInt();
        quantity = in.readInt();
        isExpanded = in.readByte() != 0;
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>()
    {
        @Override
        public CartItem createFromParcel(Parcel in)
        {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size)
        {
            return new CartItem[size];
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

    public int getOrders()
    {
        return orders;
    }

    public void setOrders(int orders)
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

    public void setAll(CartItem item)
    {
        this.productId = item.getProductId();
        this.name = item.getName();
        this.shortDesc = item.getShortDesc();
        this.imgUri = item.getImgUri();
        this.price = item.getPrice();
        this.orders = item.getOrders();
        this.inStock = item.getInStock();
        this.quantity = item.getQuantity();
        this.isExpanded = item.isExpanded();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem item = (CartItem) o;
        return this.inStock == item.getInStock() &&
                this.quantity == item.getQuantity() &&
                this.isExpanded == item.isExpanded() &&
                Objects.equals(this.productId, item.getProductId()) &&
                Objects.equals(this.name, item.getName()) &&
                Objects.equals(this.shortDesc, item.getShortDesc()) &&
                Objects.equals(this.imgUri, item.getImgUri()) &&
                Objects.equals(this.price, item.getPrice()) &&
                Objects.equals(this.orders, item.getOrders());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(productId, name, shortDesc, imgUri, price, orders, inStock, quantity, isExpanded);
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
        dest.writeString(imgUri);
        if (price == null)
        {
            dest.writeByte((byte) 0);
        }
        else {
            dest.writeByte((byte) 1);
            dest.writeDouble(price);
        }
        dest.writeInt(orders);
        dest.writeInt(inStock);
        dest.writeInt(quantity);
        dest.writeByte((byte) (isExpanded ? 1 : 0));
    }

}
