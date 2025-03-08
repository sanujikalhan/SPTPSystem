package com.example.sptm_systerm;

import android.content.Context;
import android.content.Intent;
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
        // Apply gradient background based on status
        if ("Done".equalsIgnoreCase(complaint.getTechnicianStatus())) {
            holder.itemView.setBackgroundResource(R.drawable.gradient_background); // Green Gradient
        } else if ("Pending".equalsIgnoreCase(complaint.getTechnicianStatus())) {
            holder.itemView.setBackgroundResource(R.drawable.gradient_orange); // Orange Gradient
        }
        else {
            holder.itemView.setBackgroundResource(R.drawable.default_background); // Default
        }
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

            // Set Click Listener on each item
            itemView.setOnClickListener(v -> {
                if(!GlobalVariable.userRole.equals("Technician"))
                    return;
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Complaint selectedComplaint = complaintList.get(position);

                    // Create Intent to pass data to StatusUpdateActivity
                    Intent intent = new Intent(context, StatusUpdateActivity.class);

                    // If using Serializable
                    intent.putExtra("complaint", selectedComplaint); // 🔹 Pass object

                    // If using Parcelable
                    // intent.putExtra("complaint", selectedComplaint);

                    context.startActivity(intent);
                }
            });
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
