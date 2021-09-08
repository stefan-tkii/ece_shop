package com.example.eceshop;

import android.annotation.SuppressLint;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OrderRecyclerViewAdapter extends RecyclerView.Adapter<OrderRecyclerViewAdapter.OrderViewHolder>
{

    private Context context;
    private List<Order> ordersList;
    private int lastPosition = -1;
    private OnOrderClickListener mListener;

    public OrderRecyclerViewAdapter(Context context, List<Order> ordersList)
    {
        this.context = context;
        this.ordersList = ordersList;
    }

    public interface OnOrderClickListener
    {
        void OnOrderClick(int position, Order data);
    }

    public void setOnOrderClickListener(OnOrderClickListener listener)
    {
        mListener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_rv_item, parent, false);
        return new OrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position)
    {
        Order item = ordersList.get(position);
        String id = "ID: " + item.getOrderId();
        holder.idTextView.setText(id);
        String address = "Address: " + item.getAddress();
        holder.addressTextView.setText(address);
        double total = item.calculateTotalPrice();
        String price = "Total amount: " + total + "$";
        holder.priceTextView.setText(price);
        String status = item.getStatus();
        holder.statusTextView.setText(status);
        if(status.equals("Ongoing"))
        {
            holder.statusTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.pendingColor));
        }
        else if(status.equals("Completed"))
        {
            holder.statusTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.yesColor));
        }
        else if(status.equals("Cancelled"))
        {
            holder.statusTextView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.mainTextColor));
        }
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        long timestamp = item.getTimestamp();
        String date = sdf.format(new Date(timestamp));
        String dateText = "Ordered at: " + date;
        holder.dateTextView.setText(dateText);

        setAnimation(holder.itemView, position);
        holder.container.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mListener != null)
                {
                    mListener.OnOrderClick(position, item);
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return ordersList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder
    {

        private final LinearLayout container;
        private final TextView idTextView;
        private final TextView addressTextView;
        private final TextView priceTextView;
        private final TextView statusTextView;
        private final TextView dateTextView;

        public OrderViewHolder(@NonNull View itemView)
        {
            super(itemView);

            container = itemView.findViewById(R.id.order_container);
            idTextView = itemView.findViewById(R.id.orderId);
            addressTextView = itemView.findViewById(R.id.order_address);
            priceTextView = itemView.findViewById(R.id.order_price);
            statusTextView = itemView.findViewById(R.id.order_status);
            dateTextView = itemView.findViewById(R.id.order_date);
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
    public void onViewDetachedFromWindow(@NonNull OrderViewHolder holder)
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
        return position;
    }

}
