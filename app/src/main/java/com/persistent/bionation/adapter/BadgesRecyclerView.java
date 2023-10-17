package com.persistent.bionation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.persistent.bionation.R;
import com.persistent.bionation.data.BadgeObservationData;

import java.util.ArrayList;

public class BadgesRecyclerView extends RecyclerView.Adapter<BadgesRecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<BadgeObservationData> data;

    public BadgesRecyclerView(Context context, ArrayList<BadgeObservationData> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.badge_recycler_view_layout,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.badgeTitle.setText(data.get(position).getTitle());
        holder.badgeTime.setText(data.get(position).getTime());
        holder.badgeCommonName.setText(data.get(position).getCommonName());
        holder.badgeIsThreatened.setText(data.get(position).getIsThreatened());
        holder.badgeObserveCount.setText(data.get(position).getObserveCount());
        Glide.with(context).load(data.get(position).getUrlToImage()).placeholder(R.drawable.image_spinner).into(holder.badgeImage);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView badgeImage;
        TextView badgeTitle,badgeTime,badgeCommonName,badgeIsThreatened,badgeObserveCount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            badgeImage = itemView.findViewById(R.id.badgesImage);
            badgeTitle = itemView.findViewById(R.id.badgesTitle);
            badgeTime = itemView.findViewById(R.id.badgesTime);
            badgeCommonName = itemView.findViewById(R.id.badgesCommonName);
            badgeIsThreatened = itemView.findViewById(R.id.badgesIsThreatened);
            badgeObserveCount = itemView.findViewById(R.id.badgesIsObserveCount);

        }
    }
}
