package com.sricharan.smartemergencysafetyapp;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sricharan.smartemergencysafetyapp.models.EmergencyEvent;
import com.sricharan.smartemergencysafetyapp.repository.FirebaseRepository;
import com.sricharan.smartemergencysafetyapp.utils.LocationHelper;

public class LocationSharingActivity extends AppCompatActivity {

    private static final int PERMISSION_REQ_LOCATION = 102;

    private TextView tvStatus, tvLatitude, tvLongitude, tvMapsLink;
    private ProgressBar pbLocation;
    private Button btnGetLocation, btnOpenMaps, btnShareLocation, btnCopyLink;

    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private String currentMapsUrl = "";
    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_location_sharing);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = FirebaseRepository.getInstance(this);

        tvStatus = findViewById(R.id.tv_location_status);
        tvLatitude = findViewById(R.id.tv_latitude);
        tvLongitude = findViewById(R.id.tv_longitude);
        tvMapsLink = findViewById(R.id.tv_maps_link);
        pbLocation = findViewById(R.id.pb_location);

        btnGetLocation = findViewById(R.id.btn_get_location);
        btnOpenMaps = findViewById(R.id.btn_open_maps);
        btnShareLocation = findViewById(R.id.btn_share_location);
        btnCopyLink = findViewById(R.id.btn_copy_link);

        btnGetLocation.setOnClickListener(v -> checkPermissionAndFetchLocation());
        btnOpenMaps.setOnClickListener(v -> openInGoogleMaps());
        btnShareLocation.setOnClickListener(v -> shareLocationViaApp());
        btnCopyLink.setOnClickListener(v -> copyMapsLinkToClipboard());

        checkPermissionAndFetchLocation();
    }

    private void checkPermissionAndFetchLocation() {
        if (!LocationHelper.hasLocationPermission(this)) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQ_LOCATION
            );
        } else {
            fetchLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                tvStatus.setText("Location permission denied. Cannot acquire GPS coordinates.");
                Toast.makeText(this, "Location permission is required to detect position.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void fetchLocation() {
        pbLocation.setVisibility(View.VISIBLE);
        tvStatus.setText("Acquiring high-accuracy location...");

        LocationHelper.getCurrentLocation(this, new LocationHelper.LocationCallback() {
            @Override
            public void onSuccess(double latitude, double longitude, String mapsUrl) {
                pbLocation.setVisibility(View.GONE);
                currentLatitude = latitude;
                currentLongitude = longitude;
                currentMapsUrl = mapsUrl;

                tvStatus.setText("Location Acquired Successfully!");
                tvLatitude.setText("Latitude: " + latitude);
                tvLongitude.setText("Longitude: " + longitude);
                tvMapsLink.setText("Google Maps Link: " + mapsUrl);
            }

            @Override
            public void onError(String errorMessage) {
                pbLocation.setVisibility(View.GONE);
                tvStatus.setText("Error: " + errorMessage);
                Toast.makeText(LocationSharingActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openInGoogleMaps() {
        if (TextUtils.isEmpty(currentMapsUrl)) {
            Toast.makeText(this, "Please detect location first!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentMapsUrl));
        startActivity(intent);
    }

    private void shareLocationViaApp() {
        if (TextUtils.isEmpty(currentMapsUrl)) {
            Toast.makeText(this, "Please detect location first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String shareText = "EMERGENCY ALERT: Here is my current real-time location: " + currentMapsUrl;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Emergency Location");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent, "Share Location Via"));

        EmergencyEvent event = new EmergencyEvent(
                null,
                "Manual Location Share",
                System.currentTimeMillis(),
                currentLatitude,
                currentLongitude,
                currentMapsUrl,
                "Location Shared",
                "Shared location link via system chooser"
        );
        repository.addEmergencyEvent(event, null);
    }

    private void copyMapsLinkToClipboard() {
        if (TextUtils.isEmpty(currentMapsUrl)) {
            Toast.makeText(this, "Please detect location first!", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Emergency Location Link", currentMapsUrl);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Google Maps link copied to clipboard!", Toast.LENGTH_SHORT).show();
        }
    }
}