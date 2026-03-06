package com.concordia.qualiair;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReadingAdapter extends RecyclerView.Adapter<ReadingAdapter.ReadingViewHolder> {

    private List<Reading> readingList;

    public ReadingAdapter(List<Reading> readingList) {

        this.readingList = readingList;

    }

    @NonNull
    @Override
    public ReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Inflate the custom row layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reading, parent, false);
        return new ReadingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingViewHolder holder, int position) {
        // Get the specific data item for this row and pass it to the Viewholder
        Reading reading = readingList.get(position);
        holder.bind(reading);
    }

    @Override
    public int getItemCount() {
        return readingList.size();
    }

    //The ViewHolder Class holds the views for a single row in the list
    public static class ReadingViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        TextView valueTextView;
        TextView levelTextView;

        public ReadingViewHolder(@NonNull View itemView) {
            super(itemView);
            // Connects the variable to the TextViews in the item)reading.xml
            timeTextView = itemView.findViewById(R.id.timeTextView);
            valueTextView = itemView.findViewById(R.id.valueTextView);
            levelTextView = itemView.findViewById(R.id.levelTextView);
        }

       // This method fills a single row with data and styles it
        public void bind(Reading reading) {
            // Put the data from the Reading object into TextView
            timeTextView.setText(reading.getTime());
            valueTextView.setText(String.format("%.1f", reading.getValue()));
            levelTextView.setText(reading.getLevel());

            // Chnage the color and background based omn the status
            Context context = itemView.getContext();
            switch (reading.getLevel()) {
                case "High":
                    levelTextView.setBackgroundResource(R.drawable.bg_status_high);
                    levelTextView.setTextColor(context.getColor(R.color.danger));
                    valueTextView.setTextColor(context.getColor(R.color.danger));
                    break;

                case "Moderate":
                    levelTextView.setBackgroundResource(R.drawable.bg_status_moderate);
                    levelTextView.setTextColor(context.getColor(R.color.warning));
                    valueTextView.setTextColor(context.getColor(R.color.warning));
                    break;

                case "Low":
                    levelTextView.setBackgroundResource(R.drawable.bg_status_low);
                    levelTextView.setTextColor(context.getColor(R.color.safe));
                    valueTextView.setTextColor(context.getColor(R.color.safe));
                    break;

                default:
                    // A fallback for any unexpected status string
                    levelTextView.setBackgroundColor(context.getColor(android.R.color.transparent));
                    levelTextView.setTextColor(context.getColor(R.color.textMuted));
                    valueTextView.setTextColor(context.getColor(R.color.text));
                    break;
            }
        }
    }
}
