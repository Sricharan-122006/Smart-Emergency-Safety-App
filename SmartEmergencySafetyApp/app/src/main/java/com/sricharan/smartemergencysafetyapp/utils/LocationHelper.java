package com.sricharan.smartemergencysafetyapp.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

public class LocationHelper {

    public interface LocationCallback {
        void onSuccess(double latitude, double longitude, String mapsUrl);
        void onError(String errorMessage);
    }

    public static boolean hasLocationPermission(@NonNull Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static String generateMapsUrl(double latitude, double longitude) {
        return "https://maps.google.com/?q=" + latitude + "," + longitude;
    }

    @SuppressLint("MissingPermission")
    public static void getCurrentLocation(@NonNull Context context, @NonNull LocationCallback callback) {
        if (!hasLocationPermission(context)) {
            callback.onError("Location permission not granted.");
            return;
        }

        FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(context);

        try {
            CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lng = location.getLongitude();
                            callback.onSuccess(lat, lng, generateMapsUrl(lat, lng));
                        } else {
                            getLastKnownLocationFallback(context, fusedClient, callback);
                        }
                    })
                    .addOnFailureListener(e -> getLastKnownLocationFallback(context, fusedClient, callback));
        } catch (SecurityException e) {
            callback.onError("Security Exception getting location: " + e.getMessage());
        }
    }

    @SuppressLint("MissingPermission")
    private static void getLastKnownLocationFallback(Context context, FusedLocationProviderClient fusedClient, LocationCallback callback) {
        try {
            fusedClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();
                    callback.onSuccess(lat, lng, generateMapsUrl(lat, lng));
                } else {
                    LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    Location gpsLoc = null;
                    Location netLoc = null;
                    if (lm != null) {
                        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            gpsLoc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            netLoc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                    }

                    Location finalLoc = gpsLoc != null ? gpsLoc : netLoc;
                    if (finalLoc != null) {
                        double lat = finalLoc.getLatitude();
                        double lng = finalLoc.getLongitude();
                        callback.onSuccess(lat, lng, generateMapsUrl(lat, lng));
                    } else {
                        callback.onError("Location unavailable. Make sure GPS is enabled.");
                    }
                }
            }).addOnFailureListener(e -> callback.onError("Failed to acquire location: " + e.getMessage()));
        } catch (SecurityException e) {
            callback.onError("Permission denied for location fallback.");
        }
    }
}