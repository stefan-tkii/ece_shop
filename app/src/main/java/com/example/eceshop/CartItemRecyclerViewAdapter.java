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
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CartItemRecyclerViewAdapter extends RecyclerView.Adapter<CartItemRecyclerViewAdapter.CartItemViewHolder>
{

    private Context context;
    private List<CartItem> items;
      private int lastPosition = -1;
    private OnCartButtonClickListener mListener;

    public CartItemRecyclerViewAdapter(Context context, List<CartItem> items)
    {
        this.context = context;
        this.items = items;
    }

    public interface OnCartButtonClickListener
    {
        void OnRemoveButtonClick(int position, CartItem data);
        void OnOrderButtonClick(int position, CartItem data);
    }

    public void setOnCartButtonClickListener(OnCartButtonClickListener listener)
    {
        mListener = listener;
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item_layout, parent, false);
        return new CartItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position)
    {
        CartItem model = items.get(position);
        Picasso.get().load(model.getImgUri()).placeholder(R.drawable.load_placeholder)
                .into(holder.itemImg);
        holder.itemName.setText(model.getName());
        String price;
        int stock = model.getInStock();
        if(stock == 0)
        {
            price = "Price: " + String.valueOf(model.getPrice()) + "$";
            holder.itemPrice.setText(price);
            holder.itemStock.setText(holder.itemView.getContext().getResources().getString(R.string.no));
            holder.itemStock.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.mainTextColor));
        }
        else
        {
            if(model.getQuantity() == 0)
            {
                price = "Price: " +  String.valueOf(model.getPrice()) + "$";
            }
            else
            {
                price = "Price: " +  String.valueOf(model.getQuantity()*model.getPrice()) + "$";
            }
            holder.itemPrice.setText(price);
            holder.itemStock.setText(holder.itemView.getContext().getResources().getString(R.string.yes));
            holder.itemStock.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.yesColor));
        }
        holder.itemDesc.setText(model.getShortDesc());
        String orders = "Orders: " + model.getOrders();
        holder.itemOrders.setText(orders);

        boolean set = model.isExpanded();
        holder.expandableContainer.setVisibility(set ? View.VISIBLE : View.GONE);

        holder.quantitySelector.setRange(0, model.getInStock());
        holder.quantitySelector.setNumber(String.valueOf(model.getQuantity()));

        holder.quantitySelector.setOnClickListener(new ElegantNumberButton.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String disp = holder.quantitySelector.getNumber();
                int num = Integer.parseInt(disp);
                double totalPrice = num*model.getPrice();
                model.setQuantity(num);
                if(num > 0)
                {
                    String newPrice = "Price: " + totalPrice + "$";
                    holder.itemPrice.setText(newPrice);
                }
            }
        });

        setAnimation(holder.itemView, position);

        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                boolean expanded = model.isExpanded();
                model.setExpanded(!expanded);
                notifyItemChanged(position);
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mListener != null)
                {
                    mListener.OnRemoveButtonClick(position, model);
                }
            }
        });

        holder.orderBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mListener != null)
                {
                    mListener.OnOrderButtonClick(position, model);
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    public static class CartItemViewHolder extends RecyclerView.ViewHolder
    {
        private final ConstraintLayout container;
        private final ImageView itemImg;
        private final TextView itemName;
        private final TextView itemPrice;
        private final TextView itemStock;
        private final TextView itemOrders;
        private final TextView itemDesc;
        private final LinearLayout expandableContainer;
        private final ElegantNumberButton quantitySelector;
        private final AppCompatButton deleteBtn;
        private final AppCompatButton orderBtn;

        public CartItemViewHolder(@NonNull View itemView)
        {
            super(itemView);
            container = itemView.findViewById(R.id.cart_item_container);
            itemImg = itemView.findViewById(R.id.cart_item_image);
            itemName = itemView.findViewById(R.id.cart_item_name);
            itemPrice = itemView.findViewById(R.id.cart_item_price);
            itemStock = itemView.findViewById(R.id.cart_item_inStock);
            itemOrders = itemView.findViewById(R.id.cart_item_orders);
            itemDesc = itemView.findViewById(R.id.cart_item_ShortDesc);
            expandableContainer = itemView.findViewById(R.id.expandableContainer);
            quantitySelector = (ElegantNumberButton) itemView.findViewById(R.id.quantitySelector);
            deleteBtn = itemView.findViewById(R.id.deleteCartItemBtn);
            orderBtn = itemView.findViewById(R.id.orderCartItemBtn);
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
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_animation);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull CartItemViewHolder holder)
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
