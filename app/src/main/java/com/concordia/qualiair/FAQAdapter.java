package com.concordia.qualiair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FAQAdapter extends RecyclerView.Adapter<FAQAdapter.FAQViewHolder> {

    private List<FAQItem> faqList;

    // Constructor to pass the list from the Activity
    public FAQAdapter(List<FAQItem> faqList) {
        this.faqList = faqList;
    }

    @NonNull
    @Override
    public FAQViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Connects Java code to item_faq.xml layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
        return new FAQViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FAQViewHolder holder, int position) {
        FAQItem item = faqList.get(position);

        holder.tvQuestion.setText(item.getQuestion());
        holder.tvAnswer.setText(item.getAnswer());

        // Check if the item is expanded and show/hide the answer
        boolean isExpanded = item.isExpanded();
        holder.tvAnswer.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // Rotate the arrow icon: 180 degrees if open, 0 if closed
        holder.ivArrow.setRotation(isExpanded ? 180f : 0f);

        // Handle the click on the whole row
        holder.itemView.setOnClickListener(v -> {
            item.setExpanded(!item.isExpanded());
            notifyItemChanged(position); // Refreshes just this one row
        });
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    // This class "holds" the views for a single row
    public static class FAQViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvAnswer;
        ImageView ivArrow;

        public FAQViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            tvAnswer = itemView.findViewById(R.id.tvAnswer);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}