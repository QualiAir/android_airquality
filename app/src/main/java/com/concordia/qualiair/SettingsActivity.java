package com.concordia.qualiair;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.appcompat.app.AlertDialog;

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
                    if (selectedImageUri == null) return;

                    // Take persistable permission so URI survives app restarts
                    try {
                        getContentResolver().takePersistableUriPermission(
                                selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        // Not all URIs support this — safe to ignore
                    }

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

        profilePicPreview = findViewById(R.id.profilePicPreview);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        dropdownSensitivity = findViewById(R.id.dropdownSensitivity);
        cardCustomThresholds = findViewById(R.id.cardCustomThresholds);
        etNh3Caution = findViewById(R.id.etNh3Caution);
        etNh3Alarm = findViewById(R.id.etNh3Alarm);
        etH2sCaution = findViewById(R.id.etH2sCaution);
        etH2sAlarm = findViewById(R.id.etH2sAlarm);
        etPm25Caution = findViewById(R.id.etPm25Caution);
        etPm25Alarm = findViewById(R.id.etPm25Alarm);

        MaterialButton btnChangePic = findViewById(R.id.btnChangePic);
        MaterialButton btnSave = findViewById(R.id.btnSave);

        //Sensitivity dropdown
        String[] options = {"Normal", "Sensitive", "Custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, options);
        dropdownSensitivity.setAdapter(adapter);

        dropdownSensitivity.setOnClickListener(v -> dropdownSensitivity.showDropDown());

        // Show/hide custom card based on selection
        dropdownSensitivity.setOnItemClickListener((parent, view, position, id) -> {
            String selected = options[position];

            if (selected.equals("Custom")) {
                showCustomDialog();
            }

            toggleCustomCard(selected);
            if (!selected.equals("Custom")) {
                prefillCustomFields(ThresholdLevels.fromPreference(
                        selected, 0, 0, 0, 0, 0, 0));
            }
        });

        //Load saved values
        userPreferences = new UserPreferences(this);
        userPreferences.loadAllPreferences();
        etName.setText(userPreferences.getUsername());
        etEmail.setText(userPreferences.getEmail());

        String savedUri = sharedPrefs.getString("profile_pic_uri", null);
        if (savedUri != null) {
            try {
                profilePicPreview.setImageURI(Uri.parse(savedUri));
            } catch (Exception e) {
                profilePicPreview.setImageResource(R.drawable.temp_profile);
            }
        }

        String savedPreset = sharedPrefs.getString(ThresholdLevels.KEY_SENSITIVITY, "Normal");
        dropdownSensitivity.setText(savedPreset, false);
        toggleCustomCard(savedPreset);


        // Pre-fill custom fields from saved values
        etNh3Caution.setText(String.valueOf(sharedPrefs.getFloat(ThresholdLevels.KEY_NH3_CAUTION, ThresholdLevels.NORMAL.nh3Caution)));
        etNh3Alarm.setText(String.valueOf(sharedPrefs.getFloat(ThresholdLevels.KEY_NH3_ALARM, ThresholdLevels.NORMAL.nh3Alarm)));
        etH2sCaution.setText(String.valueOf(sharedPrefs.getFloat(ThresholdLevels.KEY_H2S_CAUTION, ThresholdLevels.NORMAL.h2sCaution)));
        etH2sAlarm.setText(String.valueOf(sharedPrefs.getFloat(ThresholdLevels.KEY_H2S_ALARM, ThresholdLevels.NORMAL.h2sAlarm)));
        etPm25Caution.setText(String.valueOf(sharedPrefs.getFloat(ThresholdLevels.KEY_PM25_CAUTION, ThresholdLevels.NORMAL.pm25Caution)));
        etPm25Alarm.setText(String.valueOf(sharedPrefs.getFloat(ThresholdLevels.KEY_PM25_ALARM, ThresholdLevels.NORMAL.pm25Alarm)));

        // Open document picker (supports persistable permissions)
        btnChangePic.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText() != null ? etName.getText().toString().trim() : "";
            String newEmail = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

            if (newName.isEmpty()) {
                etName.setError("Name cannot be empty");
                return;
            }
            if (newEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                etEmail.setError("Enter a valid email address");
                return;
            }

            String preset = dropdownSensitivity.getText().toString();

            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString("username", newName);
            editor.putString("email", newEmail);
            editor.putString(ThresholdLevels.KEY_SENSITIVITY, preset);

            if (preset.equals("Custom")) {
                if (!validateAndSaveCustom(editor)) return;
            } else {
                ThresholdLevels.Thresholds t = ThresholdLevels.fromPreference(preset, 0, 0, 0, 0, 0, 0);
                editor.putFloat(ThresholdLevels.KEY_NH3_CAUTION,  t.nh3Caution);
                editor.putFloat(ThresholdLevels.KEY_NH3_ALARM,    t.nh3Alarm);
                editor.putFloat(ThresholdLevels.KEY_H2S_CAUTION,  t.h2sCaution);
                editor.putFloat(ThresholdLevels.KEY_H2S_ALARM,    t.h2sAlarm);
                editor.putFloat(ThresholdLevels.KEY_PM25_CAUTION, t.pm25Caution);
                editor.putFloat(ThresholdLevels.KEY_PM25_ALARM,   t.pm25Alarm);
            }

            editor.apply();

            // Send updated thresholds to backend so background notifications use correct values
            ThresholdLevels.Thresholds backendThresholds = ThresholdLevels.fromPreference(
                    preset,
                    sharedPrefs.getFloat(ThresholdLevels.KEY_NH3_CAUTION, ThresholdLevels.NORMAL.nh3Caution),
                    sharedPrefs.getFloat(ThresholdLevels.KEY_NH3_ALARM, ThresholdLevels.NORMAL.nh3Alarm),
                    sharedPrefs.getFloat(ThresholdLevels.KEY_H2S_CAUTION, ThresholdLevels.NORMAL.h2sCaution),
                    sharedPrefs.getFloat(ThresholdLevels.KEY_H2S_ALARM, ThresholdLevels.NORMAL.h2sAlarm),
                    sharedPrefs.getFloat(ThresholdLevels.KEY_PM25_CAUTION, ThresholdLevels.NORMAL.pm25Caution),
                    sharedPrefs.getFloat(ThresholdLevels.KEY_PM25_ALARM, ThresholdLevels.NORMAL.pm25Alarm)
            );
            FcmTokenManager.sendThresholdsToBackend(this, backendThresholds);
            Snackbar.make(btnSave, "Settings saved!", Snackbar.LENGTH_SHORT).show();
            finish();
        });
    }

    private void toggleCustomCard(String preset) {
        cardCustomThresholds.setVisibility(View.GONE);
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
            float nh3C = parseField(etNh3Caution, "NH₃ caution");
            float nh3A = parseField(etNh3Alarm, "NH₃ alarm");
            float h2sC = parseField(etH2sCaution, "H₂S caution");
            float h2sA = parseField(etH2sAlarm, "H₂S alarm");
            float pm25C = parseField(etPm25Caution, "PM2.5 caution");
            float pm25A = parseField(etPm25Alarm, "PM2.5 alarm");

            if (nh3C >= nh3A) {
                etNh3Alarm.setError("Must be greater than caution");
                return false;
            }
            if (h2sC >= h2sA) {
                etH2sAlarm.setError("Must be greater than caution");
                return false;
            }
            if (pm25C >= pm25A) {
                etPm25Alarm.setError("Must be greater than caution");
                return false;
            }

            editor.putFloat(ThresholdLevels.KEY_NH3_CAUTION,  nh3C);
            editor.putFloat(ThresholdLevels.KEY_NH3_ALARM,    nh3A);
            editor.putFloat(ThresholdLevels.KEY_H2S_CAUTION,  h2sC);
            editor.putFloat(ThresholdLevels.KEY_H2S_ALARM,    h2sA);
            editor.putFloat(ThresholdLevels.KEY_PM25_CAUTION, pm25C);
            editor.putFloat(ThresholdLevels.KEY_PM25_ALARM,   pm25A);
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

    private void showCustomDialog() {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_custom_thresholds, null);

        TextInputEditText etNh3CautionDialog = dialogView.findViewById(R.id.etNh3Caution);
        TextInputEditText etNh3AlarmDialog   = dialogView.findViewById(R.id.etNh3Alarm);
        TextInputEditText etH2sCautionDialog = dialogView.findViewById(R.id.etH2sCaution);
        TextInputEditText etH2sAlarmDialog   = dialogView.findViewById(R.id.etH2sAlarm);
        TextInputEditText etPm25CautionDialog = dialogView.findViewById(R.id.etPm25Caution);
        TextInputEditText etPm25AlarmDialog   = dialogView.findViewById(R.id.etPm25Alarm);

        // preload from existing card values
        etNh3CautionDialog.setText(etNh3Caution.getText());
        etNh3AlarmDialog.setText(etNh3Alarm.getText());
        etH2sCautionDialog.setText(etH2sCaution.getText());
        etH2sAlarmDialog.setText(etH2sAlarm.getText());
        etPm25CautionDialog.setText(etPm25Caution.getText());
        etPm25AlarmDialog.setText(etPm25Alarm.getText());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Custom Thresholds")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                try {

                    // clear previous errors
                    etNh3CautionDialog.setError(null);
                    etNh3AlarmDialog.setError(null);
                    etH2sCautionDialog.setError(null);
                    etH2sAlarmDialog.setError(null);
                    etPm25CautionDialog.setError(null);
                    etPm25AlarmDialog.setError(null);

                    float nh3C = Float.parseFloat(etNh3CautionDialog.getText().toString());
                    float nh3A = Float.parseFloat(etNh3AlarmDialog.getText().toString());
                    float h2sC = Float.parseFloat(etH2sCautionDialog.getText().toString());
                    float h2sA = Float.parseFloat(etH2sAlarmDialog.getText().toString());
                    float pm25C = Float.parseFloat(etPm25CautionDialog.getText().toString());
                    float pm25A = Float.parseFloat(etPm25AlarmDialog.getText().toString());

                    if (nh3C >= nh3A || h2sC >= h2sA || pm25C >= pm25A) {
                        Snackbar.make(findViewById(R.id.btnSave), "Caution must be less than Alarm", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    // enforce max = default (Normal)
                    ThresholdLevels.Thresholds defaultT =
                            ThresholdLevels.fromPreference("Normal", 0,0,0,0,0,0);

                    // NH3
                    if (nh3C > defaultT.nh3Caution) {
                        etNh3CautionDialog.setError("Max: " + defaultT.nh3Caution);
                        return;
                    }
                    if (nh3A > defaultT.nh3Alarm) {
                        etNh3AlarmDialog.setError("Max: " + defaultT.nh3Alarm);
                        return;
                    }

                    // H2S
                    if (h2sC > defaultT.h2sCaution) {
                        etH2sCautionDialog.setError("Max: " + defaultT.h2sCaution);
                        return;
                    }
                    if (h2sA > defaultT.h2sAlarm) {
                        etH2sAlarmDialog.setError("Max: " + defaultT.h2sAlarm);
                        return;
                    }

                    // PM2.5
                    if (pm25C > defaultT.pm25Caution) {
                        etPm25CautionDialog.setError("Max: " + defaultT.pm25Caution);
                        return;
                    }
                    if (pm25A > defaultT.pm25Alarm) {
                        etPm25AlarmDialog.setError("Max: " + defaultT.pm25Alarm);
                        return;
                    }

                    // copy values BACK into your existing card fields
                    etNh3Caution.setText(String.valueOf(nh3C));
                    etNh3Alarm.setText(String.valueOf(nh3A));
                    etH2sCaution.setText(String.valueOf(h2sC));
                    etH2sAlarm.setText(String.valueOf(h2sA));
                    etPm25Caution.setText(String.valueOf(pm25C));
                    etPm25Alarm.setText(String.valueOf(pm25A));

                    sharedPrefs.edit()
                            .putString(ThresholdLevels.KEY_SENSITIVITY, "Custom")
                            .putFloat(ThresholdLevels.KEY_NH3_CAUTION,  nh3C)
                            .putFloat(ThresholdLevels.KEY_NH3_ALARM,    nh3A)
                            .putFloat(ThresholdLevels.KEY_H2S_CAUTION,  h2sC)
                            .putFloat(ThresholdLevels.KEY_H2S_ALARM,    h2sA)
                            .putFloat(ThresholdLevels.KEY_PM25_CAUTION, pm25C)
                            .putFloat(ThresholdLevels.KEY_PM25_ALARM,   pm25A)
                            .apply();

                    dropdownSensitivity.setText("Custom", false);

                    dialog.dismiss();

                } catch (Exception e) {
                    Snackbar.make(findViewById(R.id.btnSave), "Invalid input", Snackbar.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();

        dialog.getWindow().setLayout(
                (int)(getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
    }
}