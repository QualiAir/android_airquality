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
        faqList.add(new FAQItem("Where can I find more information on Hydrogen Sulfide?", "We have looked at a few sources and decided to base our preset levels on the data provided by the tables in this link: https://www.osha.gov/chemicaldata/652"));
        faqList.add(new FAQItem("Where can I find more information on Ammonia?", "We have looked at a few sources and decided to base our preset levels on the data provided by the tables in this link: https://www.osha.gov/chemicaldata/623"));
        faqList.add(new FAQItem("Where can I find more information on Particulate Matter?", "We have looked at a few sources and decided to base our preset levels on the data provided by the tables in this link: https://www.epa.gov/criteria-air-pollutants/naaqs-table"));


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