package com.example.sptm_systerm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sptm_systerm.R;
import com.example.sptm_systerm.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private Context context;
    private List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_row, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.userName.setText(user.getName());

        // Set checkboxes based on user role
        holder.checkBoxT.setChecked(user.getRole().equals("Technical Team"));
        holder.checkBoxR.setChecked(user.getRole().equals("Reader"));
        holder.checkBoxC.setChecked(user.getRole().equals("Customer"));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName;
        CheckBox checkBoxT, checkBoxR, checkBoxC;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userName);
            checkBoxT = itemView.findViewById(R.id.checkBoxT);
            checkBoxR = itemView.findViewById(R.id.checkBoxR);
            checkBoxC = itemView.findViewById(R.id.checkBoxC);
        }
    }
}
