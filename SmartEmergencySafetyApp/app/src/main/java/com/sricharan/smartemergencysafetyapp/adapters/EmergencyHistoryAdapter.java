package com.sricharan.smartemergencysafetyapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sricharan.smartemergencysafetyapp.R;
import com.sricharan.smartemergencysafetyapp.models.EmergencyEvent;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmergencyHistoryAdapter extends RecyclerView.Adapter<EmergencyHistoryAdapter.ViewHolder> {

    public interface OnHistoryClickListener {
        void onDelete(EmergencyEvent event);
    }

    private List<EmergencyEvent> historyList;
    private final OnHistoryClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());

    public EmergencyHistoryAdapter(List<EmergencyEvent> historyList, OnHistoryClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    public void setHistoryList(List<EmergencyEvent> historyList) {
        this.historyList = historyList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyEvent event = historyList.get(position);
        holder.tvType.setText(event.getEventType());
        holder.tvTime.setText(dateFormat.format(new Date(event.getTimestamp())));
        holder.tvStatus.setText(event.getStatus());
        holder.tvDetails.setText(event.getDetails());

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(event));
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvTime, tvStatus, tvDetails;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_history_type);
            tvTime = itemView.findViewById(R.id.tv_history_time);
            tvStatus = itemView.findViewById(R.id.tv_history_status);
            tvDetails = itemView.findViewById(R.id.tv_history_details);
            btnDelete = itemView.findViewById(R.id.btn_delete_history);
        }
    }
}