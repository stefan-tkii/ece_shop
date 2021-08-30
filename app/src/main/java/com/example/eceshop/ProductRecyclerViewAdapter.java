package com.example.eceshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ProductRecyclerViewAdapter extends RecyclerView.Adapter<ProductRecyclerViewAdapter.ItemViewHolder>
{

    private Context context;
    private List<ProductRecyclerViewModel> items;
    private int lastPosition = -1;
    private int selectedPos = RecyclerView.NO_POSITION;
    private OnProductClickListener mListener;

    public ProductRecyclerViewAdapter(Context context, List<ProductRecyclerViewModel> items)
    {
        this.context = context;
        this.items = items;
    }

    public interface OnProductClickListener
    {
        void OnProductClick(int position, ProductRecyclerViewModel data);
    }

    public void setOnProductClickListener(OnProductClickListener listener)
    {
        mListener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_rv_item, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position)
    {
        ProductRecyclerViewModel item = items.get(position);
        holder.name.setText(item.getName());
        String imgUrl = item.getImgUri();
        Picasso.get().load(imgUrl).into(holder.image);
        holder.description.setText(item.getShortDesc());
        String price = "Price: " + item.getPrice().toString() + "$";
        holder.price.setText(price);
        String orders = "Orders: " + item.getOrders();
        holder.orders.setText(orders);
        setAnimation(holder.itemView, position);

        selectedPos = position;

        holder.container.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mListener != null)
                {
                    mListener.OnProductClick(selectedPos, item);
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder
    {

        private final TextView name;
        private final ImageView image;
        private final TextView description;
        private final TextView price;
        private final TextView orders;
        private final LinearLayout container;

        public ItemViewHolder(@NonNull View itemView)
        {
            super(itemView);
            container = itemView.findViewById(R.id.product_container);
            name = itemView.findViewById(R.id.product_name);
            image = itemView.findViewById(R.id.product_image);
            description = itemView.findViewById(R.id.product_description);
            price = itemView.findViewById(R.id.product_price);
            orders = itemView.findViewById(R.id.product_orders);
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
            //Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_animation);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ItemViewHolder holder)
    {
        holder.clearAnimation();
    }

}
