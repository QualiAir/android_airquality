package com.concordia.qualiair;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {

    private static final String KEY_SENSITIVITY     = "sensitivity_preset";
    private static final String KEY_CUSTOM_NH3_C    = "custom_nh3_caution";
    private static final String KEY_CUSTOM_NH3_A    = "custom_nh3_alarm";
    private static final String KEY_CUSTOM_H2S_C    = "custom_h2s_caution";
    private static final String KEY_CUSTOM_H2S_A    = "custom_h2s_alarm";
    private static final String KEY_CUSTOM_PM25_C   = "custom_pm25_caution";
    private static final String KEY_CUSTOM_PM25_A   = "custom_pm25_alarm";

    private ShapeableImageView profilePicPreview;
    private TextInputEditText etName, etEmail;
    private AutoCompleteTextView dropdownSensitivity;
    private CardView cardCustomThresholds;
    private TextInputEditText etNh3Caution, etNh3Alarm;
    private TextInputEditText etH2sCaution, etH2sAlarm;
    private TextInputEditText etPm25Caution, etPm25Alarm;
    private UserPreferences userPreferences;
    private SharedPreferences sharedPrefs;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    profilePicPreview.setImageURI(selectedImageUri);
                    sharedPrefs.edit()
                            .putString("profile_pic_uri", selectedImageUri.toString())
                            .apply();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPrefs = getSharedPreferences("QualiAirPreferences", MODE_PRIVATE);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        profilePicPreview    = findViewById(R.id.profilePicPreview);
        etName               = findViewById(R.id.etName);
        etEmail              = findViewById(R.id.etEmail);
        dropdownSensitivity  = findViewById(R.id.dropdownSensitivity);
        cardCustomThresholds = findViewById(R.id.cardCustomThresholds);
        etNh3Caution         = findViewById(R.id.etNh3Caution);
        etNh3Alarm           = findViewById(R.id.etNh3Alarm);
        etH2sCaution         = findViewById(R.id.etH2sCaution);
        etH2sAlarm           = findViewById(R.id.etH2sAlarm);
        etPm25Caution        = findViewById(R.id.etPm25Caution);
        etPm25Alarm          = findViewById(R.id.etPm25Alarm);

        MaterialButton btnChangePic = findViewById(R.id.btnChangePic);
        MaterialButton btnSave      = findViewById(R.id.btnSave);

        // --- Sensitivity dropdown ---
        String[] options = {"Normal", "Sensitive", "Custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, options);
        dropdownSensitivity.setAdapter(adapter);

        // Show/hide custom card based on selection
        dropdownSensitivity.setOnItemClickListener((parent, view, position, id) -> {
            String selected = options[position];
            toggleCustomCard(selected);
            if (!selected.equals("Custom")) {
                prefillCustomFields(ThresholdLevels.fromPreference(
                        selected, 0, 0, 0, 0, 0, 0));
            }
        });

        // --- Load saved values ---
        userPreferences = new UserPreferences(this);
        userPreferences.loadAllPreferences();
        etName.setText(userPreferences.getUsername());
        etEmail.setText(userPreferences.getEmail());

        String savedUri = sharedPrefs.getString("profile_pic_uri", null);
        if (savedUri != null) profilePicPreview.setImageURI(Uri.parse(savedUri));

        String savedPreset = sharedPrefs.getString(KEY_SENSITIVITY, "Normal");
        dropdownSensitivity.setText(savedPreset, false);
        toggleCustomCard(savedPreset);

        // Pre-fill custom fields from saved values
        etNh3Caution.setText(String.valueOf(sharedPrefs.getFloat(KEY_CUSTOM_NH3_C,  ThresholdLevels.NORMAL.nh3Caution)));
        etNh3Alarm.setText(String.valueOf(sharedPrefs.getFloat(KEY_CUSTOM_NH3_A,    ThresholdLevels.NORMAL.nh3Alarm)));
        etH2sCaution.setText(String.valueOf(sharedPrefs.getFloat(KEY_CUSTOM_H2S_C,  ThresholdLevels.NORMAL.h2sCaution)));
        etH2sAlarm.setText(String.valueOf(sharedPrefs.getFloat(KEY_CUSTOM_H2S_A,    ThresholdLevels.NORMAL.h2sAlarm)));
        etPm25Caution.setText(String.valueOf(sharedPrefs.getFloat(KEY_CUSTOM_PM25_C, ThresholdLevels.NORMAL.pm25Caution)));
        etPm25Alarm.setText(String.valueOf(sharedPrefs.getFloat(KEY_CUSTOM_PM25_A,  ThresholdLevels.NORMAL.pm25Alarm)));

        // --- Listeners ---
        btnChangePic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            String newName  = etName.getText()  != null ? etName.getText().toString().trim()  : "";
            String newEmail = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

            if (newName.isEmpty()) { etName.setError("Name cannot be empty"); return; }
            if (newEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                etEmail.setError("Enter a valid email address"); return;
            }

            String preset = dropdownSensitivity.getText().toString();

            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("username", newName);
            editor.putString("email", newEmail);
            editor.putString(KEY_SENSITIVITY, preset);

            if (preset.equals("Custom")) {
                if (!validateAndSaveCustom(editor)) return;
            } else {
                // Save the preset values as the active thresholds too
                ThresholdLevels.Thresholds t = ThresholdLevels.fromPreference(preset, 0,0,0,0,0,0);
                editor.putFloat(KEY_CUSTOM_NH3_C,  t.nh3Caution);
                editor.putFloat(KEY_CUSTOM_NH3_A,  t.nh3Alarm);
                editor.putFloat(KEY_CUSTOM_H2S_C,  t.h2sCaution);
                editor.putFloat(KEY_CUSTOM_H2S_A,  t.h2sAlarm);
                editor.putFloat(KEY_CUSTOM_PM25_C, t.pm25Caution);
                editor.putFloat(KEY_CUSTOM_PM25_A, t.pm25Alarm);
            }

            editor.apply();
            Snackbar.make(btnSave, "Settings saved!", Snackbar.LENGTH_SHORT).show();
            finish();
        });
    }

    private void toggleCustomCard(String preset) {
        cardCustomThresholds.setVisibility(
                preset.equals("Custom") ? View.VISIBLE : View.GONE);
    }

    private void prefillCustomFields(ThresholdLevels.Thresholds t) {
        etNh3Caution.setText(String.valueOf(t.nh3Caution));
        etNh3Alarm.setText(String.valueOf(t.nh3Alarm));
        etH2sCaution.setText(String.valueOf(t.h2sCaution));
        etH2sAlarm.setText(String.valueOf(t.h2sAlarm));
        etPm25Caution.setText(String.valueOf(t.pm25Caution));
        etPm25Alarm.setText(String.valueOf(t.pm25Alarm));
    }

    private boolean validateAndSaveCustom(SharedPreferences.Editor editor) {
        try {
            float nh3C  = parseField(etNh3Caution,  "NH₃ caution");
            float nh3A  = parseField(etNh3Alarm,    "NH₃ alarm");
            float h2sC  = parseField(etH2sCaution,  "H₂S caution");
            float h2sA  = parseField(etH2sAlarm,    "H₂S alarm");
            float pm25C = parseField(etPm25Caution, "PM2.5 caution");
            float pm25A = parseField(etPm25Alarm,   "PM2.5 alarm");

            if (nh3C  >= nh3A)  { etNh3Alarm.setError("Must be greater than caution");   return false; }
            if (h2sC  >= h2sA)  { etH2sAlarm.setError("Must be greater than caution");   return false; }
            if (pm25C >= pm25A) { etPm25Alarm.setError("Must be greater than caution");  return false; }

            editor.putFloat(KEY_CUSTOM_NH3_C,  nh3C);
            editor.putFloat(KEY_CUSTOM_NH3_A,  nh3A);
            editor.putFloat(KEY_CUSTOM_H2S_C,  h2sC);
            editor.putFloat(KEY_CUSTOM_H2S_A,  h2sA);
            editor.putFloat(KEY_CUSTOM_PM25_C, pm25C);
            editor.putFloat(KEY_CUSTOM_PM25_A, pm25A);
            return true;

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private float parseField(TextInputEditText field, String label) {
        String val = field.getText() != null ? field.getText().toString().trim() : "";
        if (val.isEmpty()) {
            field.setError(label + " cannot be empty");
            throw new IllegalArgumentException();
        }
        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException e) {
            field.setError("Invalid number");
            throw new IllegalArgumentException();
        }
    }
}