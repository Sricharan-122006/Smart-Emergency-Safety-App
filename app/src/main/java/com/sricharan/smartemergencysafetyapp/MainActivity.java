package com.sricharan.smartemergencysafetyapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sricharan.smartemergencysafetyapp.models.EmergencyEvent;
import com.sricharan.smartemergencysafetyapp.repository.FirebaseRepository;
import com.sricharan.smartemergencysafetyapp.utils.LocationHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_CODE_SOS = 101;

    private TextView tvWelcomeName, tvWelcomeEmail;
    private LinearLayout layoutBatteryWarning;
    private View btnSos;
    private ImageButton btnLogout;
    private FirebaseRepository repository;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            if (level >= 0 && scale > 0) {
                float batteryPct = (level / (float) scale) * 100;
                if (batteryPct <= 15 && repository.isBatteryMonitoringEnabled()) {
                    layoutBatteryWarning.setVisibility(View.VISIBLE);
                } else {
                    layoutBatteryWarning.setVisibility(View.GONE);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = FirebaseRepository.getInstance(this);

        tvWelcomeName = findViewById(R.id.tv_welcome_name);
        tvWelcomeEmail = findViewById(R.id.tv_welcome_email);
        layoutBatteryWarning = findViewById(R.id.layout_battery_warning);
        btnSos = findViewById(R.id.btn_sos);
        btnLogout = findViewById(R.id.btn_header_logout);

        setupUserInfo();
        setupNavigation();

        btnSos.setOnClickListener(v -> confirmAndSendSos());
        btnLogout.setOnClickListener(v -> confirmLogout());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupUserInfo();
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(batteryReceiver);
        } catch (Exception ignored) {}
    }

    private void setupUserInfo() {
        String name = repository.getCurrentUserName();
        String email = repository.getCurrentUserEmail();
        tvWelcomeName.setText("Welcome, " + name);
        tvWelcomeEmail.setText(email);
    }

    private void setupNavigation() {
        findViewById(R.id.card_contacts).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, EmergencyContactsActivity.class)));

        findViewById(R.id.card_location).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, LocationSharingActivity.class)));

        findViewById(R.id.card_timer).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SafetyTimerActivity.class)));

        findViewById(R.id.card_fake_call).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FakeCallActivity.class)));

        findViewById(R.id.card_history).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, EmergencyHistoryActivity.class)));

        findViewById(R.id.card_info).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, EmergencyInformationActivity.class)));

        findViewById(R.id.card_settings).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }

    private void confirmAndSendSos() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.sos_confirm_title)
                .setMessage(R.string.sos_confirm_msg)
                .setIcon(R.drawable.ic_sos)
                .setPositiveButton(R.string.btn_send_sos, (dialog, which) -> checkPermissionsAndTriggerSos())
                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void checkPermissionsAndTriggerSos() {
        List<String> permissionsNeeded = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.SEND_SMS);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSION_REQ_CODE_SOS);
        } else {
            executeSosWorkflow();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE_SOS) {
            executeSosWorkflow();
        }
    }

    private void executeSosWorkflow() {
        repository.getEmergencyContacts((success, contacts, errorMessage) -> {
            if (contacts == null || contacts.isEmpty()) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("No Emergency Contacts Found")
                        .setMessage("Please add at least one emergency contact before triggering SOS.")
                        .setPositiveButton("Add Contact Now", (dialog, which) ->
                                startActivity(new Intent(MainActivity.this, EmergencyContactsActivity.class)))
                        .setNegativeButton("Cancel", null)
                        .show();
                return;
            }

            StringBuilder phoneNumbersBuilder = new StringBuilder();
            for (int i = 0; i < contacts.size(); i++) {
                phoneNumbersBuilder.append(contacts.get(i).getPhoneNumber());
                if (i < contacts.size() - 1) {
                    phoneNumbersBuilder.append(";");
                }
            }
            String phoneNumbers = phoneNumbersBuilder.toString();

            if (LocationHelper.hasLocationPermission(MainActivity.this)) {
                Toast.makeText(MainActivity.this, "Acquiring current location for SOS...", Toast.LENGTH_SHORT).show();
                LocationHelper.getCurrentLocation(MainActivity.this, new LocationHelper.LocationCallback() {
                    @Override
                    public void onSuccess(double latitude, double longitude, String mapsUrl) {
                        sendSosSms(phoneNumbers, latitude, longitude, mapsUrl);
                    }

                    @Override
                    public void onError(String error) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Location Unavailable")
                                .setMessage(error + "\nDo you want to send the Emergency SOS message without location link?")
                                .setPositiveButton("Send Without Location", (dialog, which) ->
                                        sendSosSms(phoneNumbers, 0.0, 0.0, null))
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                });
            } else {
                sendSosSms(phoneNumbers, 0.0, 0.0, null);
            }
        });
    }

    private void sendSosSms(String phoneNumbers, double latitude, double longitude, String mapsUrl) {
        String baseMessage = repository.getCustomSosMessage();
        String fullMessage;

        if (!TextUtils.isEmpty(mapsUrl)) {
            fullMessage = baseMessage + " " + mapsUrl;
        } else {
            fullMessage = baseMessage + " (Location unavailable)";
        }

        try {
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + Uri.encode(phoneNumbers)));
            smsIntent.putExtra("sms_body", fullMessage);
            startActivity(smsIntent);

            EmergencyEvent event = new EmergencyEvent(
                    null,
                    "Emergency SOS Alert",
                    System.currentTimeMillis(),
                    latitude,
                    longitude,
                    mapsUrl != null ? mapsUrl : "N/A",
                    "SOS Alert Initiated",
                    "SMS app opened with message: \"" + fullMessage + "\""
            );
            repository.addEmergencyEvent(event, null);

            Toast.makeText(this, "Emergency SMS screen opened. Tap Send in your messaging app!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not open SMS application: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    repository.logout();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}