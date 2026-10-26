package com.sricharan.smartemergencysafetyapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.sricharan.smartemergencysafetyapp.repository.FirebaseRepository;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvFirebaseStatus;
    private EditText etSosMessage;
    private Button btnSaveSosMessage, btnLogout;
    private SwitchMaterial switchBatteryMonitor, switchPowerOffAlert;
    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = FirebaseRepository.getInstance(this);

        tvName = findViewById(R.id.tv_settings_user_name);
        tvEmail = findViewById(R.id.tv_settings_user_email);
        tvFirebaseStatus = findViewById(R.id.tv_firebase_status);
        etSosMessage = findViewById(R.id.et_settings_sos_message);
        btnSaveSosMessage = findViewById(R.id.btn_save_sos_message);
        btnLogout = findViewById(R.id.btn_settings_logout);
        switchBatteryMonitor = findViewById(R.id.switch_battery_monitor);
        switchPowerOffAlert = findViewById(R.id.switch_power_off_alert);

        tvName.setText(repository.getCurrentUserName());
        tvEmail.setText(repository.getCurrentUserEmail());

        etSosMessage.setText(repository.getCustomSosMessage());

        if (repository.isFirebaseConfigured()) {
            tvFirebaseStatus.setText("Firebase Cloud Active");
            tvFirebaseStatus.setTextColor(0xFF2E7D32);
        } else {
            tvFirebaseStatus.setText("Local Storage Mode");
            tvFirebaseStatus.setTextColor(0xFFF57C00);
        }

        switchBatteryMonitor.setChecked(repository.isBatteryMonitoringEnabled());
        switchPowerOffAlert.setChecked(repository.isPowerOffAlertEnabled());

        switchBatteryMonitor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            repository.setBatteryMonitoringEnabled(isChecked);
            Toast.makeText(this, isChecked ? "Battery monitoring enabled" : "Battery monitoring disabled", Toast.LENGTH_SHORT).show();
        });

        switchPowerOffAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            repository.setPowerOffAlertEnabled(isChecked);
            Toast.makeText(this, isChecked ? "Phone power-off alert enabled" : "Phone power-off alert disabled", Toast.LENGTH_SHORT).show();
        });

        btnSaveSosMessage.setOnClickListener(v -> {
            String msg = etSosMessage.getText().toString().trim();
            if (msg.isEmpty()) {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            repository.setCustomSosMessage(msg);
            Toast.makeText(this, "Custom SOS Message template saved!", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        repository.logout();
                        Intent intent = new Intent(SettingsActivity.class.cast(this), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }
}