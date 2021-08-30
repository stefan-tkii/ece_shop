package com.example.eceshop;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class SimpleItem extends DrawerItem<SimpleItem.ViewHolder> {

    private int selectedItemIconTint;
    private int normalItemIconTint;

    private int selectedItemTitleTint;
    private int normalItemTitleTint;

    private Drawable icon;
    private String title;

    public SimpleItem(Drawable icon, String title)
    {
        this.icon = icon;
        this.title = title;
    }

    @Override
    public ViewHolder createViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_option, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void bindViewHolder(ViewHolder holder) {
        holder.icon.setImageDrawable(icon);
        holder.title.setText(title);
        if(title.equals("Logout"))
        {
            holder.title.setTypeface(null, Typeface.BOLD);
        }
        if(isChecked)
        {
            holder.title.setTextColor(selectedItemTitleTint);
            holder.icon.setColorFilter(selectedItemIconTint);
        }
        else
        {
            holder.title.setTextColor(normalItemTitleTint);
            holder.icon.setColorFilter(normalItemIconTint);
        }
    }

    public SimpleItem withSelectedIconTint(int selectedItemIconTint)
    {
        this.selectedItemIconTint = selectedItemIconTint;
        return this;
    }

    public SimpleItem withSelectedTitleTint(int selectedItemTitleTint)
    {
        this.selectedItemTitleTint = selectedItemTitleTint;
        return this;
    }

    public SimpleItem withIconTint(int normalItemIconTint)
    {
        this.normalItemIconTint = normalItemIconTint;
        return this;
    }

    public SimpleItem withTitleTint(int normalItemTitleTint)
    {
        this.normalItemTitleTint = normalItemTitleTint;
        return this;
    }

    static class ViewHolder extends DrawerAdapter.ViewHolder
    {

        private ImageView icon;
        private TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.item_icon);
            title = itemView.findViewById(R.id.item_title);
        }

    }

}
