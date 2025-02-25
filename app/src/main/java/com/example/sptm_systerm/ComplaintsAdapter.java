package com.example.sptm_systerm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ComplaintsAdapter extends RecyclerView.Adapter<ComplaintsAdapter.ViewHolder> {

    private List<Complaint> complaintList;
    private Context context;

    public ComplaintsAdapter(List<Complaint> complaintList, Context context) {
        this.complaintList = complaintList;
        this.context = context;
    }

    public void setComplaintList(List<Complaint> complaintList) {
        this.complaintList = complaintList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (complaintList == null) ? 0 : complaintList.size();
    }

    @NonNull
    @Override
    public ComplaintsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintsAdapter.ViewHolder holder, int position) {
        Complaint complaint = complaintList.get(position);
        holder.bind(complaint);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView complaintTextView, dateTextView, locationTextView, technicianStatusTextView, userStatusTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            complaintTextView = itemView.findViewById(R.id.complaintTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            technicianStatusTextView = itemView.findViewById(R.id.technicianStatusTextView);
            userStatusTextView = itemView.findViewById(R.id.userStatusTextView);
        }

        void bind(Complaint complaint) {
            complaintTextView.setText("Complaint: " + complaint.getComplaint());
            dateTextView.setText("Date: " + complaint.getDate());
            locationTextView.setText("Location: " + complaint.getLocation());
            technicianStatusTextView.setText("Technician Status: " + complaint.getTechnicianStatus());
            userStatusTextView.setText("User Status: " + complaint.getUserStatus());
        }
    }
}
