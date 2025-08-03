package com.sgs.gpstracker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sgs.gpstracker.R;
import com.sgs.gpstracker.models.SmsModel;

import java.util.List;

public class SmsLogAdapter extends RecyclerView.Adapter<SmsLogAdapter.SmsViewHolder> {

    private final Context context;
    private final List<SmsModel> smsList;

    public SmsLogAdapter(Context context, List<SmsModel> smsList) {
        this.context = context;
        this.smsList = smsList;
    }

    @NonNull
    @Override
    public SmsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sms_log, parent, false);
        return new SmsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SmsViewHolder holder, int position) {
        SmsModel sms = smsList.get(position);
        holder.nameText.setText(sms.getName().equals("Unknown") ? sms.getAddress() : sms.getName());
        holder.bodyText.setText(sms.getBody());
        holder.typeText.setText(sms.getType());
        holder.dateText.setText(sms.getFormattedDate());
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }

    static class SmsViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, bodyText, typeText, dateText;

        public SmsViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.smsName);
            bodyText = itemView.findViewById(R.id.smsBody);
            typeText = itemView.findViewById(R.id.smsType);
            dateText = itemView.findViewById(R.id.smsDate);
        }
    }
}
