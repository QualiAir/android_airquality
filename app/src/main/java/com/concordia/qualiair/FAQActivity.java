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

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("FAQ");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView rvFaq = findViewById(R.id.rvFaq);
        rvFaq.setLayoutManager(new LinearLayoutManager(this));

        List<FAQItem> faqList = new ArrayList<>();
        faqList.add(new FAQItem("What is NH3 (Ammonia) and where does it come from?", "Ammonia is a colorless gas with a sharp odor. It mainly comes from agricultural activity (fertilizers, livestock), industrial processes, and wastewater treatment."));
        faqList.add(new FAQItem("What are the health effects of NH3 exposure?", "Low levels cause eye, nose, and throat irritation. At higher concentrations it can cause coughing, respiratory distress, and in severe cases, lung damage. The default sensitivity level flags caution at 25 ppm and alarm at 35 ppm.\n"));
        faqList.add(new FAQItem("What is H2S (Hydrogen Sulfide) and where does it come from?\n", "H2S is a colorless gas with a rotten egg smell. It's produced by decaying organic matter, sewage, oil/gas operations, and industrial waste."));
        faqList.add(new FAQItem("What are the health effects of H2S exposure?", "Even at low levels it causes headaches, nausea, and eye irritation. At higher levels it can impair breathing and become life-threatening quickly. The default sensitivity level flags caution at 1 ppm and alarm at 5 ppm, reflecting how toxic it is even in small amounts.\n"));
        faqList.add(new FAQItem("What is PM2.5 and where does it come from?", "PM2.5 are fine particles 2.5 micrometers or smaller, produced by vehicle emissions, wildfires, industrial activity, and combustion.\n"));
        faqList.add(new FAQItem("What are the health effects of PM2.5 exposure?", "These particles penetrate deep into the lungs and bloodstream, causing respiratory and cardiovascular issues. Long-term exposure is linked to heart disease and lung cancer. the default sensitivity level flags caution at 12 µg/m³ and alarm at 35 µg/m³ per EPA/NIOSH standards.\n"));
        faqList.add(new FAQItem("What is the difference between the Normal and Sensitive PreSet sensitivities?\n", "The Normal preset follows EPA/NIOSH standard thresholds. The Sensitive preset uses tighter limits (e.g. NH3 caution at 10 ppm, H2S caution at 0.5 ppm, PM2.5 caution at 9 µg/m³) for children, the elderly, or those with respiratory conditions.\n"));
        faqList.add(new FAQItem("When should I use the Custom threshold setting?", "Use Custom if you have specific medical needs or workplace requirements that differ from standard guidelines. Note that caution levels should always be set lower than alarm levels for each pollutant.\n"));
        faqList.add(new FAQItem("What should I do when the alarm threshold is triggered?\n", "Leave the area immediately if possible, ventilate the space, and seek fresh air. For H2S especially, do not ignore alarms as dangerous concentrations can occur rapidly.\n"));
        faqList.add(new FAQItem("Where can I find more information on Hydrogen Sulfide?", "We have looked at a few sources and decided to base our preset levels on the data provided by the tables in this link: https://www.osha.gov/chemicaldata/652"));
        faqList.add(new FAQItem("Where can I find more information on Ammonia?", "We have looked at a few sources and decided to base our preset levels on the data provided by the tables in this link: https://www.osha.gov/chemicaldata/623"));
        faqList.add(new FAQItem("Where can I find more information on Particulate Matter?", "We have looked at a few sources and decided to base our preset levels on the data provided by the tables in this link: https://www.epa.gov/criteria-air-pollutants/naaqs-table"));
        faqList.add(new FAQItem("Support contact?", "Email us at elec390team1@gmail.com."));

        FAQAdapter adapter = new FAQAdapter(faqList);
        rvFaq.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationHelper.setupBottomNavigation(this, R.id.nav_faq);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
