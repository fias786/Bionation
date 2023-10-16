package com.persistent.bionation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.persistent.bionation.R;
import com.persistent.bionation.data.ObservationImageData;

import java.util.ArrayList;

public class ObservationImageRecyclerView  extends RecyclerView.Adapter<ObservationImageRecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<ObservationImageData> data;

    public ObservationImageRecyclerView(Context context, ArrayList<ObservationImageData> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_image_layout,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(data.get(position).getLargeImage()).placeholder(R.drawable.image_spinner).into(holder.largeImage);
        Glide.with(context).load(data.get(position).getFirstSmallImage()).placeholder(R.drawable.image_spinner).into(holder.firstSmallImage);
        Glide.with(context).load(data.get(position).getSecondSmallImage()).placeholder(R.drawable.image_spinner).into(holder.secondSmallImage);
        holder.largeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context,""+position,Toast.LENGTH_SHORT).show();
            }
        });
        holder.firstSmallImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context,""+position,Toast.LENGTH_SHORT).show();
            }
        });
        holder.secondSmallImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context,""+position,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView largeImage,firstSmallImage,secondSmallImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            largeImage = itemView.findViewById(R.id.LargeImage);
            firstSmallImage = itemView.findViewById(R.id.FirstSmallImage);
            secondSmallImage = itemView.findViewById(R.id.SecondSmallImage);
        }
    }
}
