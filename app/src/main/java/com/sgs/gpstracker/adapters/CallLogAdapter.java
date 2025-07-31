package com.sgs.gpstracker.adapters;
import static android.view.View.GONE;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sgs.gpstracker.R;
import com.sgs.gpstracker.models.CallLogModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.CallLogViewHolder> {

    private List<CallLogModel> callLogs;

    public CallLogAdapter(List<CallLogModel> callLogs) {
        this.callLogs = callLogs;
    }

    @NonNull
    @Override
    public CallLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call_log, parent, false);
        return new CallLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallLogViewHolder holder, int position) {
        CallLogModel log = callLogs.get(position);
        if ("Unknown".equals(log.getName())) {
            holder.numberText.setText(log.getNumber());
        } else {
            holder.numberText.setText(log.getName());
        }

        setCallDuration(holder.callDuration,log);
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = null;
        try {
            date = inputFormat.parse(log.getFormattedDate());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm EEE", Locale.getDefault());
        String formatted = outputFormat.format(date);

        holder.dateText.setText(formatted);
        if(log.getType().equals("Outgoing")){holder.callTypeImage.setBackgroundResource(R.drawable.ic_outgoing);}
        if(log.getType().equals("Missed")){holder.callTypeImage.setBackgroundResource(R.drawable.ic_missed);}
        if(log.getType().equals("Incoming")){holder.callTypeImage.setBackgroundResource(R.drawable.ic_incoming);}
    }

    private void setCallDuration(TextView callDuration, CallLogModel log) {

        long duration = log.getDuration(); // in seconds
        String durationText;

        if (duration < 60) {
            durationText = duration + " sec";
        } else {
            long minutes = duration / 60;
            long seconds = duration % 60;
            if (seconds == 0) {
                durationText = minutes + " min";
            } else {
                durationText = minutes + " min " + seconds + " sec";
            }
        }

        callDuration.setText(durationText);
      if(log.getType().equals("Missed")){callDuration.setVisibility(GONE);}else{
          callDuration.setVisibility(View.VISIBLE);
      }
    }

    @Override
    public int getItemCount() {
        return callLogs.size();
    }

    public static class CallLogViewHolder extends RecyclerView.ViewHolder {
        TextView numberText, callDuration, dateText;
        ImageView callTypeImage;

        public CallLogViewHolder(@NonNull View itemView) {
            super(itemView);
            numberText = itemView.findViewById(R.id.textNumber);
            dateText = itemView.findViewById(R.id.textDate);
            callTypeImage = itemView.findViewById(R.id.callTypeImage);
            callDuration = itemView.findViewById(R.id.callDuration);
        }
    }
}
