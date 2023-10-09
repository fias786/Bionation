package com.persistent.bionation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.persistent.bionation.R;
import com.persistent.bionation.data.NewsArticlesData;

import java.util.ArrayList;

public class NewsArticlesRecyclerViewAdapter extends RecyclerView.Adapter<NewsArticlesRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<NewsArticlesData> data;

    public NewsArticlesRecyclerViewAdapter(Context context, ArrayList<NewsArticlesData> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_articles_layout,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.newsTitle.setText(data.get(position).getTitle());
        holder.newsDescription.setText(data.get(position).getDescription());
        holder.newsTime.setText(data.get(position).getPublishedAt().split("T")[0]);
        Glide.with(context).load(data.get(position).getUrlToImage()).placeholder(R.drawable.image_spinner).into(holder.newsImage);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView newsImage;
        TextView newsTitle,newsDescription,newsTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsDescription = itemView.findViewById(R.id.newsDescription);
            newsTime = itemView.findViewById(R.id.newsTime);
            newsImage = itemView.findViewById(R.id.newsImage);

        }
    }
}
