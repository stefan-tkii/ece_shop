package com.example.eceshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PreordersRecyclerViewAdapter extends RecyclerView.Adapter<PreordersRecyclerViewAdapter.PreorderViewHolder>
{
    private List<CartItem> items;
    private Context context;
    private int lastPosition = -1;

    public PreordersRecyclerViewAdapter(List<CartItem> items, Context context)
    {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public PreorderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.preorder_item_layout, parent, false);
        PreorderViewHolder viewHolder = new PreorderViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PreorderViewHolder holder, int position)
    {
        CartItem item = items.get(position);
        holder.preorderNameTextView.setText(item.getName());
        String quantity = "Quantity: " + item.getQuantity();
        holder.preorderQuantityTextView.setText(quantity);
        double total = item.getPrice()*item.getQuantity();
        String price = "Price: " + total + "$";
        holder.preorderPriceTextView.setText(price);
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    public static class PreorderViewHolder extends RecyclerView.ViewHolder
    {
        private final LinearLayout container;
        private final TextView preorderNameTextView;
        private final TextView preorderPriceTextView;
        private final TextView preorderQuantityTextView;

        public PreorderViewHolder(@NonNull View itemView)
        {
            super(itemView);

            container = itemView.findViewById(R.id.preorderItemContainer);
            preorderNameTextView = itemView.findViewById(R.id.preorderItemName);
            preorderPriceTextView = itemView.findViewById(R.id.preorderItemPrice);
            preorderQuantityTextView = itemView.findViewById(R.id.preorderItemQuantity);
        }

        public void clearAnimation()
        {
            container.clearAnimation();
        }
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull PreorderViewHolder holder)
    {
        holder.clearAnimation();
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public int getItemViewType(int position)
    {
        return  position;
    }

}
