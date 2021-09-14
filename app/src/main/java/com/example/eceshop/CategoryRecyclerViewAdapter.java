package com.example.eceshop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryRecyclerViewAdapter extends RecyclerView.Adapter<CategoryRecyclerViewAdapter.CategoryRecyclerViewHolder>
{

    private List<CategoryRecyclerViewModel> items;
    private int selectedPos = RecyclerView.NO_POSITION;
    private OnItemClickListener mlistener;

    public CategoryRecyclerViewAdapter(List<CategoryRecyclerViewModel> items)
    {
        this.items = items;
    }

    public interface OnItemClickListener
    {
        void OnItemClick(int position, String data);
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        mlistener = listener;
    }

    @NonNull
    @Override
    public CategoryRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_rv_item, parent, false);
        CategoryRecyclerViewHolder viewHolder = new CategoryRecyclerViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryRecyclerViewHolder holder, int position)
    {
        holder.itemView.setSelected(selectedPos == position);
        CategoryRecyclerViewModel model = items.get(position);
        holder.categoryImage.setImageDrawable(model.getImage());
        holder.categoryText.setText(model.getText());

        if (selectedPos == position)
        {
            holder.containerLayout.setBackgroundResource(R.drawable.category_rv_selected_bg);
        }
        else
            {
                holder.containerLayout.setBackgroundResource(R.drawable.category_rv_bg);
        }

        holder.containerLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(selectedPos == position)
                {
                    selectedPos = RecyclerView.NO_POSITION;
                    if(mlistener != null)
                    {
                        mlistener.OnItemClick(selectedPos, model.getText());
                    }
                    notifyDataSetChanged();
                }
                else
                {
                    selectedPos = position;
                    if(mlistener != null)
                    {
                        mlistener.OnItemClick(selectedPos, model.getText());
                    }
                    notifyDataSetChanged();
                }
            }
        });

    }

    @Override
    public int getItemCount()
    {
        return items.size();
    }

    public static class CategoryRecyclerViewHolder extends RecyclerView.ViewHolder
    {

        private final TextView categoryText;
        private final ImageView categoryImage;
        private final LinearLayout containerLayout;

        public CategoryRecyclerViewHolder(@NonNull View itemView)
        {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.category_image);
            categoryText = itemView.findViewById(R.id.category_text);
            containerLayout = itemView.findViewById(R.id.category_item_recycler_container);
        }

    }

}
