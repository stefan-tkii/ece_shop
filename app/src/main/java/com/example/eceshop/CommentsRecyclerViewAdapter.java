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

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<CommentsRecyclerViewAdapter.CommentViewHolder>
{

    private Context context;
    private List<CommentRvItem> commentList;
    private int lastPosition = -1;

    public CommentsRecyclerViewAdapter(Context context, List<CommentRvItem> commentList)
    {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        CommentsRecyclerViewAdapter.CommentViewHolder viewHolder = new  CommentsRecyclerViewAdapter.CommentViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position)
    {
        CommentRvItem item = commentList.get(position);
        holder.posterNameTextView.setText(item.getUserName());
        holder.posterEmailTextView.setText(item.getUserEmail());
        holder.contentTextView.setText(item.getContent());
        String postedAt = "Posted at: " + item.getPostedAt();
        holder.postedAtTextView.setText(postedAt);
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount()
    {
        return commentList.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder
    {
        private final LinearLayout container;
        private final TextView posterNameTextView;
        private final TextView posterEmailTextView;
        private final TextView contentTextView;
        private final TextView postedAtTextView;

        public CommentViewHolder(@NonNull View itemView)
        {
            super(itemView);
            container = itemView.findViewById(R.id.comment_container);
            posterNameTextView = itemView.findViewById(R.id.poster_details);
            posterEmailTextView = itemView.findViewById(R.id.poster_email);
            contentTextView = itemView.findViewById(R.id.comment_content);
            postedAtTextView = itemView.findViewById(R.id.sent_at);
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
    public void onViewDetachedFromWindow(@NonNull CommentViewHolder holder)
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
