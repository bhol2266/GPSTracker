package com.sgs.gpstracker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sgs.gpstracker.R;
import com.sgs.gpstracker.models.UserLocation;

import java.util.List;

public class UserSliderAdapter extends RecyclerView.Adapter<UserSliderAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onClick(UserLocation user);
    }

    private List<UserLocation> userList;
    private OnUserClickListener listener;

    public UserSliderAdapter(List<UserLocation> userList, OnUserClickListener listener) {
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
        UserLocation user = userList.get(position);
        holder.name.setText(user.getName());
        holder.itemView.setOnClickListener(v -> listener.onClick(user));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.userName);
        }
    }
}
