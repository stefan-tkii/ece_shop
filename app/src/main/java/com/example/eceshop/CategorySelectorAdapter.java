package com.example.eceshop;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CategorySelectorAdapter  extends ArrayAdapter<String>
{

    private Context ctx;
    private String[] contentArray;
    private Drawable[] imageArray;

    public CategorySelectorAdapter(Context context, String[] objects, Drawable[] imageArray)
    {
        super(context,  R.layout.category_selector_layout, R.id.category_selector_item, objects);
        this.ctx = context;
        this.contentArray = objects;
        this.imageArray = imageArray;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.category_selector_layout, parent, false);

        TextView textView = (TextView) row.findViewById(R.id.category_selector_item);
        textView.setText(contentArray[position]);

        ImageView imageView = (ImageView)row.findViewById(R.id.category_selector_icon);
        imageView.setImageDrawable(imageArray[position]);

        return row;
    }

}
