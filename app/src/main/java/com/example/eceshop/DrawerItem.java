package com.example.eceshop;

import android.view.ViewGroup;

public abstract class DrawerItem<T extends DrawerAdapter.ViewHolder>
{

    protected boolean isChecked;

    public abstract T createViewHolder(ViewGroup parent);

    public abstract void bindViewHolder(T holder);

    public DrawerItem<T> setChecked(boolean isChecked)
    {
        this.isChecked = isChecked;
        return this;
    }

    public boolean isChecked()
    {
        return this.isChecked;
    }

    public boolean isSelectable(int position)

    {
        if(position == 0)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

}
