package com.persistent.bionation.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.persistent.bionation.R;
import com.persistent.bionation.data.NewsArticlesData;
import com.persistent.bionation.ui.settings.SettingsFragment;
import com.persistent.bionation.ui.updates.NewsArticleActivity;

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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.newsTitle.setText(data.get(position).getTitle());
        holder.newsTime.setText(data.get(position).getPublishedAt());
        Glide.with(context).load(data.get(position).getUrlToImage()).placeholder(R.drawable.image_spinner).into(holder.newsImage);
        holder.newsCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, NewsArticleActivity.class);
                intent.putExtra("newsTitle",data.get(position).getTitle());
                intent.putExtra("newsDescription",data.get(position).getDescription());
                intent.putExtra("newsTime",data.get(position).getPublishedAt());
                intent.putExtra("newsUrlImage",data.get(position).getUrlToImage());
                intent.putExtra("newsContent", Html.fromHtml(data.get(position).getContent()));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout newsCardView;
        ImageView newsImage;
        TextView newsTitle,newsTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            newsCardView = itemView.findViewById(R.id.NewsCardView);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsTime = itemView.findViewById(R.id.newsTime);
            newsImage = itemView.findViewById(R.id.newsImage);

        }
    }
}
