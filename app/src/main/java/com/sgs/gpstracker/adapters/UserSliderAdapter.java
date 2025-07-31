package com.sgs.gpstracker.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sgs.gpstracker.R;
import com.sgs.gpstracker.models.UserModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserSliderAdapter extends RecyclerView.Adapter<UserSliderAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onClick(UserModel user);
    }

    private List<UserModel> userList;
    private OnUserClickListener listener;
    private int selectedPosition = 0;

    public UserSliderAdapter(List<UserModel> userList, OnUserClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.user_slider_item, parent, false));


    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.name.setText(user.getName());

        long timestamp = user.getLocation().getTimestamp(); // Milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss  MMM dd", Locale.getDefault());
        String formattedTime = sdf.format(new Date(timestamp));
        holder.userTimestamp.setText(formattedTime);

        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.drawable.item_selected_background); // Create this drawable
            holder.userTimestamp.setTextColor(Color.WHITE);

        } else {
            holder.itemView.setBackgroundResource(R.drawable.item_background); // Already exists
            holder.userTimestamp.setTextColor(Color.BLACK);

        }

        if (user.isOnline()) {
            holder.name.setTextColor(Color.GREEN);
            holder.userIcon.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);

        } else {
            holder.name.setTextColor(Color.RED);
            holder.userIcon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);

        }


        holder.itemView.setOnClickListener(v -> {

            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            listener.onClick(user);

            DatabaseReference requestRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getDeviceId())
                    .child("locationRequest");

            requestRef.setValue(true);
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, userTimestamp;
        ImageView userIcon;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userName);
            userIcon = itemView.findViewById(R.id.userIcon);
            userTimestamp = itemView.findViewById(R.id.userTimestamp);
        }
    }
}
