package com.example.hiking_application_coursework.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hiking_application_coursework.R;
import com.example.hiking_application_coursework.models.Hike;
import com.google.android.material.chip.Chip;

import java.util.List;

public class HikeAdapter extends RecyclerView.Adapter<HikeAdapter.HikeViewHolder> {

    private List<Hike> hikeList;
    private final OnHikeClickListener listener;

    public interface OnHikeClickListener {
        void onHikeClick(Hike hike);
    }

    public HikeAdapter(List<Hike> hikeList, OnHikeClickListener listener) {
        this.hikeList = hikeList;
        this.listener = listener;
    }

    public void setHikes(List<Hike> hikes) {
        this.hikeList = hikes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hike, parent, false);
        return new HikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HikeViewHolder holder, int position) {
        Hike hike = hikeList.get(position);
        holder.txtName.setText(hike.getName());
        holder.txtLocation.setText(hike.getLocation());
        holder.txtDate.setText(hike.getDate());
        holder.txtLength.setText(hike.getLength() + " km");

        Context context = holder.itemView.getContext();
        int resId = context.getResources().getIdentifier(hike.getImageResName(), "drawable", context.getPackageName());
        if (resId != 0) {
            holder.imgHike.setImageResource(resId);
        }

        String diff = hike.getDifficulty();
        holder.chipDifficulty.setText(diff);
        if (diff != null) {
            if (diff.equalsIgnoreCase("Easy")) {
                holder.chipDifficulty.setChipBackgroundColorResource(R.color.diff_easy_bg);
                holder.chipDifficulty.setTextColor(ContextCompat.getColor(context, R.color.diff_easy_text));
            } else if (diff.equalsIgnoreCase("Medium") || diff.equalsIgnoreCase("Moderate")) {
                holder.chipDifficulty.setChipBackgroundColorResource(R.color.diff_medium_bg);
                holder.chipDifficulty.setTextColor(ContextCompat.getColor(context, R.color.diff_medium_text));
            } else {
                holder.chipDifficulty.setChipBackgroundColorResource(R.color.diff_hard_bg);
                holder.chipDifficulty.setTextColor(ContextCompat.getColor(context, R.color.diff_hard_text));
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onHikeClick(hike));
    }

    @Override
    public int getItemCount() {
        return hikeList.size();
    }

    public static class HikeViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtLocation, txtDate, txtLength;
        ImageView imgHike;
        Chip chipDifficulty;

        public HikeViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtHikeName);
            txtLocation = itemView.findViewById(R.id.txtHikeLocation);
            txtDate = itemView.findViewById(R.id.txtHikeDate);
            txtLength = itemView.findViewById(R.id.txtHikeLength);
            imgHike = itemView.findViewById(R.id.hike_image);
            chipDifficulty = itemView.findViewById(R.id.chipHikeDifficulty);
        }
    }
}
