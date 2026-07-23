package com.example.mobile_app_orlen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_app_orlen.data.Measurement;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MeasurementAdapter extends ListAdapter<Measurement, MeasurementAdapter.MeasurementViewHolder> {

    protected MeasurementAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Measurement> DIFF_CALLBACK = new DiffUtil.ItemCallback<Measurement>() {
        @Override
        public boolean areItemsTheSame(@NonNull Measurement oldItem, @NonNull Measurement newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Measurement oldItem, @NonNull Measurement newItem) {
            return oldItem.ch4Value == newItem.ch4Value &&
                    oldItem.timestamp == newItem.timestamp;
        }
    };

    @NonNull
    @Override
    public MeasurementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_measurement, parent, false);
        return new MeasurementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeasurementViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class MeasurementViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate, tvCh4, tvStatus;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        public MeasurementViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvCh4 = itemView.findViewById(R.id.tvCh4);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(Measurement measurement) {
            tvDate.setText(dateFormat.format(new Date(measurement.timestamp)));
            tvCh4.setText(String.format(Locale.getDefault(), "CH4: %.2f %%", measurement.ch4Value));
            
            if (measurement.isAnomaly) {
                tvStatus.setText("ANOMALIA");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            } else {
                tvStatus.setText("OK");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            }
        }
    }
}
