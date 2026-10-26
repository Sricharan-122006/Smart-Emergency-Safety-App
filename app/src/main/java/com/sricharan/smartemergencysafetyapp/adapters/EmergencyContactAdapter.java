package com.sricharan.smartemergencysafetyapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sricharan.smartemergencysafetyapp.R;
import com.sricharan.smartemergencysafetyapp.models.EmergencyContact;

import java.util.List;

public class EmergencyContactAdapter extends RecyclerView.Adapter<EmergencyContactAdapter.ViewHolder> {

    public interface OnContactClickListener {
        void onEdit(EmergencyContact contact);
        void onDelete(EmergencyContact contact);
    }

    private List<EmergencyContact> contactList;
    private final OnContactClickListener listener;

    public EmergencyContactAdapter(List<EmergencyContact> contactList, OnContactClickListener listener) {
        this.contactList = contactList;
        this.listener = listener;
    }

    public void setContactList(List<EmergencyContact> contactList) {
        this.contactList = contactList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emergency_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EmergencyContact contact = contactList.get(position);
        holder.tvName.setText(contact.getName());
        holder.tvPhone.setText(contact.getPhoneNumber());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(contact));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(contact));
    }

    @Override
    public int getItemCount() {
        return contactList != null ? contactList.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_contact_name);
            tvPhone = itemView.findViewById(R.id.tv_contact_phone);
            btnEdit = itemView.findViewById(R.id.btn_edit_contact);
            btnDelete = itemView.findViewById(R.id.btn_delete_contact);
        }
    }
}