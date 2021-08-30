package com.example.eceshop;

import android.graphics.drawable.Drawable;

public class CategoryRecyclerViewModel
{

    private Drawable image;
    private String text;

    public CategoryRecyclerViewModel(Drawable image, String text)
    {
        this.image = image;
        this.text = text;
    }

    public Drawable getImage()
    {
        return image;
    }

    public void setImage(Drawable image)
    {
        this.image = image;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

}
