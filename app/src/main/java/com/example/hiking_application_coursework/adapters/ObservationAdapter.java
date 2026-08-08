package com.example.hiking_application_coursework.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hiking_application_coursework.R;
import com.example.hiking_application_coursework.models.Observation;

import java.util.List;

public class ObservationAdapter extends RecyclerView.Adapter<ObservationAdapter.ObservationViewHolder> {

    private List<Observation> observationList;

    public ObservationAdapter(List<Observation> observationList) {
        this.observationList = observationList;
    }

    @NonNull
    @Override
    public ObservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_observation, parent, false);
        return new ObservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObservationViewHolder holder, int position) {
        Observation obs = observationList.get(position);
        holder.txtName.setText(obs.getObservation());
        holder.txtTime.setText(obs.getTime());
        holder.txtComments.setText(obs.getComments());
    }

    @Override
    public int getItemCount() {
        return observationList.size();
    }

    public void setObservations(List<Observation> observations) {
        this.observationList = observations;
        notifyDataSetChanged();
    }

    public static class ObservationViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtTime, txtComments;

        public ObservationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtObsName);
            txtTime = itemView.findViewById(R.id.txtObsTime);
            txtComments = itemView.findViewById(R.id.txtObsComments);
        }
    }
}
