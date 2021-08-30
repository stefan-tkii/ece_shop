package com.example.eceshop;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

public class SpaceItem extends DrawerItem<SpaceItem.ViewHolder> {

    private int spaceDp;

    public SpaceItem(int spaceDp)
    {
        this.spaceDp = spaceDp;
    }

    @Override
    public ViewHolder createViewHolder(ViewGroup parent) {
        Context c = parent.getContext();
        View v = new View(c);
        int height = (int) (c.getResources().getDisplayMetrics().density*spaceDp);
        v.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height
        ));
        return new ViewHolder(v);
    }

    @Override
    public void bindViewHolder(ViewHolder holder) {

    }

    @Override
    public boolean isSelectable(int position) {
        return false;
    }

    public class ViewHolder extends DrawerAdapter.ViewHolder
    {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

        }
    }

}
