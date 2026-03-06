package com.concordia.qualiair;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class FAQActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        //link MaterialToolbar in xml
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Action Bar and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("FAQ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Initialize RecyclerV
        RecyclerView rvFaq = findViewById(R.id.rvFaq);
        rvFaq.setLayoutManager(new LinearLayoutManager(this));

        //list of questions and answers
        List<FAQItem> faqList = new ArrayList<>();
        faqList.add(new FAQItem("Support contact?", "Email us at elec390team1@gmail.com."));

        //Adapter
        FAQAdapter adapter = new FAQAdapter(faqList);
        rvFaq.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}