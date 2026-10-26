package com.sricharan.smartemergencysafetyapp.receivers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.sricharan.smartemergencysafetyapp.models.EmergencyContact;
import com.sricharan.smartemergencysafetyapp.models.EmergencyEvent;
import com.sricharan.smartemergencysafetyapp.repository.FirebaseRepository;
import com.sricharan.smartemergencysafetyapp.utils.LocationHelper;

import java.util.List;

public class PowerOffReceiver extends BroadcastReceiver {

    private static final String TAG = "PowerOffReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        if (Intent.ACTION_SHUTDOWN.equals(action)
                || "android.intent.action.QUICKBOOT_POWEROFF".equals(action)
                || "com.htc.intent.action.QUICKBOOT_POWEROFF".equals(action)) {

            Log.d(TAG, "Device shutdown broadcast received: " + action);

            FirebaseRepository repository = FirebaseRepository.getInstance(context);

            if (!repository.isPowerOffAlertEnabled()) {
                Log.d(TAG, "Power-off alert feature disabled by user");
                return;
            }

            // Fetch saved emergency contacts
            List<EmergencyContact> contacts = repository.getLocalContacts();
            if (contacts == null || contacts.isEmpty()) {
                Log.d(TAG, "No emergency contacts available for power off alert");
                return;
            }

            // Get last known location
            String locationLink = getLastKnownLocationLink(context);
            String message = "POWER OFF ALERT! My device is powering off / shutting down. " + locationLink;

            // Send direct SMS if permission is granted
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                try {
                    SmsManager smsManager;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        smsManager = context.getSystemService(SmsManager.class);
                    } else {
                        smsManager = SmsManager.getDefault();
                    }

                    for (EmergencyContact contact : contacts) {
                        if (!TextUtils.isEmpty(contact.getPhoneNumber())) {
                            smsManager.sendTextMessage(contact.getPhoneNumber(), null, message, null, null);
                            Log.d(TAG, "Power-off SMS alert sent to " + contact.getPhoneNumber());
                        }
                    }

                    // Record event in emergency history
                    EmergencyEvent event = new EmergencyEvent(
                            null,
                            "Device Shutdown Alert",
                            System.currentTimeMillis(),
                            0.0, 0.0,
                            locationLink,
                            "Power Off Alert Sent",
                            "Sent power-off SMS alert with location to " + contacts.size() + " contact(s)"
                    );
                    repository.addEmergencyEvent(event, null);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to send power-off SMS alert: " + e.getMessage());
                }
            } else {
                Log.w(TAG, "SEND_SMS permission not granted. Cannot send shutdown SMS");
            }
        }
    }

    @SuppressLint("MissingPermission")
    private String getLastKnownLocationLink(Context context) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

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
                    return "My last known location: " + LocationHelper.generateMapsUrl(finalLoc.getLatitude(), finalLoc.getLongitude());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting location during shutdown: " + e.getMessage());
        }
        return "(Location unavailable during shutdown)";
    }
}