package com.example.eceshop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserItemRecyclerViewAdapter extends RecyclerView.Adapter<UserItemRecyclerViewAdapter.UserViewHolder>
{

    private Context context;
    private List<UserRvItem> usersList;
    private int lastPosition = -1;
    private OnUserItemClickListener mListener;

    public UserItemRecyclerViewAdapter(Context context, List<UserRvItem> usersList)
    {
        this.context = context;
        this.usersList = usersList;
    }

    public interface OnUserItemClickListener
    {
        void OnUserItemClick(int position, UserRvItem clickedUser);
    }

    public void setOnUserItemClickListener(OnUserItemClickListener listener)
    {
        mListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_recycler_item, parent, false);
        UserViewHolder viewHolder = new UserViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position)
    {
        UserRvItem item = usersList.get(position);
        String id = "ID: " + item.getUserId();
        String userName = item.getFullName();
        String email = item.getEmail();
        String orders = "Orders: " + String.valueOf(item.getOrderCount());
        String phoneNum = "Phone number: " + item.getPhoneNumber();

        holder.idTextView.setText(id);
        holder.nameTextView.setText(userName);
        holder.emailTextView.setText(email);
        holder.orderNumberTextView.setText(orders);
        holder.phoneTextView.setText(phoneNum);

        if(item.isHasActiveOrder())
        {
            holder.container.setBackgroundResource(R.drawable.active_order_item_bg);
        }
        else
        {
            holder.container.setBackgroundResource(R.drawable.non_active_order_bg);
        }

        holder.container.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mListener != null)
                {
                    mListener.OnUserItemClick(position, item);
                }
            }
        });

        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount()
    {
        return usersList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder
    {

        private final ConstraintLayout container;
        private final TextView idTextView;
        private final TextView nameTextView;
        private final TextView emailTextView;
        private final TextView orderNumberTextView;
        private final TextView phoneTextView;

        public UserViewHolder(@NonNull View itemView)
        {
            super(itemView);

            container = itemView.findViewById(R.id.user_item_container);
            idTextView = itemView.findViewById(R.id.user_item_id);
            nameTextView = itemView.findViewById(R.id.user_item_userName);
            emailTextView = itemView.findViewById(R.id.user_item_email);
            orderNumberTextView = itemView.findViewById(R.id.user_item_orders);
            phoneTextView = itemView.findViewById(R.id.user_item_phone);
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
    public void onViewDetachedFromWindow(@NonNull UserViewHolder holder)
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
