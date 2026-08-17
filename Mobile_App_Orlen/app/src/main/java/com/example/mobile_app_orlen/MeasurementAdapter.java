package com.example.mobile_app_orlen;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile_app_orlen.data.Measurement;
import com.example.mobile_app_orlen.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MeasurementAdapter
        extends ListAdapter<Measurement, MeasurementAdapter.MeasurementViewHolder> {

    protected MeasurementAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Measurement> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Measurement>() {

                @Override
                public boolean areItemsTheSame(
                        @NonNull Measurement oldItem,
                        @NonNull Measurement newItem
                ) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull Measurement oldItem,
                        @NonNull Measurement newItem
                ) {
                    return oldItem.ch4Value == newItem.ch4Value
                            && oldItem.timestamp == newItem.timestamp
                            && oldItem.isAnomaly == newItem.isAnomaly
                            && equals(oldItem.notes, newItem.notes)
                            && equals(oldItem.locationName, newItem.locationName)
                            && oldItem.latitude == newItem.latitude
                            && oldItem.longitude == newItem.longitude;
                }

                private boolean equals(String a, String b) {
                    if (a == null && b == null) return true;
                    if (a == null || b == null) return false;
                    return a.equals(b);
                }
            };

    @NonNull
    @Override
    public MeasurementViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_measurement,
                        parent,
                        false
                );

        return new MeasurementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MeasurementViewHolder holder,
            int position
    ) {
        holder.bind(getItem(position));
    }

    static class MeasurementViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView tvDate;
        private final TextView tvCh4;
        private final TextView tvStatus;
        private final TextView tvLocation;

        private final SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm",
                        Locale.getDefault()
                );

        public MeasurementViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tvDate);
            tvCh4 = itemView.findViewById(R.id.tvCh4);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvLocation = itemView.findViewById(R.id.tvLocation);
        }

        public void bind(Measurement measurement) {

            tvDate.setText(
                    dateFormat.format(
                            new Date(measurement.timestamp)
                    )
            );

            tvCh4.setText(
                    String.format(
                            Locale.getDefault(),
                            "CH4: %.2f %%",
                            measurement.ch4Value
                    )
            );

            String locationName =
                    measurement.locationName != null
                            ? measurement.locationName.trim()
                            : "";

            if (locationName.isEmpty()) {

                tvLocation.setText(
                        String.format(
                                Locale.getDefault(),
                                "Lokalizacja: %.5f, %.5f",
                                measurement.latitude,
                                measurement.longitude
                        )
                );

            } else {

                tvLocation.setText(
                        "Lokalizacja: " + locationName
                );
            }

            model.SafetyLevel level =
                    model.getMethaneSafetyLevel(
                            measurement.ch4Value
                    );

            tvStatus.setText(
                    model.getStatusMessage(level)
            );

            int color;

            switch (level) {
                case ALERT:
                    color = itemView.getContext()
                            .getResources()
                            .getColor(
                                    android.R.color.holo_red_dark
                            );
                    break;

                case WARNING:
                    color = itemView.getContext()
                            .getResources()
                            .getColor(
                                    android.R.color.holo_orange_dark
                            );
                    break;

                case SAFE:
                default:
                    color = itemView.getContext()
                            .getResources()
                            .getColor(
                                    android.R.color.holo_green_dark
                            );
                    break;
            }

            tvStatus.setBackgroundTintList(
                    ColorStateList.valueOf(color)
            );

            tvStatus.setTextColor(
                    itemView.getContext()
                            .getResources()
                            .getColor(
                                    android.R.color.white
                            )
            );
        }
    }
}