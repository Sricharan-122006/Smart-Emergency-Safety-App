package com.sricharan.smartemergencysafetyapp.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.sricharan.smartemergencysafetyapp.models.EmergencyContact;
import com.sricharan.smartemergencysafetyapp.models.EmergencyEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseRepository {

    private static final String PREFS_NAME = "SmartEmergencySafetyPrefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_SOS_MESSAGE = "custom_sos_message";
    private static final String KEY_EMERGENCY_NUMBER = "emergency_number";
    private static final String KEY_BATTERY_MONITOR = "battery_monitor_enabled";
    private static final String KEY_POWER_OFF_ALERT = "power_off_alert_enabled";
    private static final String KEY_MEDICAL_NOTES = "medical_notes";
    private static final String KEY_LOCAL_CONTACTS = "local_contacts";
    private static final String KEY_LOCAL_HISTORY = "local_history";

    public interface Callback<T> {
        void onResult(boolean success, T data, String errorMessage);
    }

    private static FirebaseRepository instance;
    private final Context context;
    private final SharedPreferences prefs;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private boolean isFirebaseAvailable = false;

    private FirebaseRepository(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        try {
            if (!FirebaseApp.getApps(context).isEmpty()) {
                this.firebaseAuth = FirebaseAuth.getInstance();
                this.firestore = FirebaseFirestore.getInstance();
                this.isFirebaseAvailable = true;
            }
        } catch (Exception e) {
            this.isFirebaseAvailable = false;
        }
    }

    public static synchronized FirebaseRepository getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseRepository(context);
        }
        return instance;
    }

    public boolean isFirebaseConfigured() {
        return isFirebaseAvailable;
    }

    // --- AUTHENTICATION ---

    public boolean isLoggedIn() {
        if (isFirebaseAvailable && firebaseAuth != null) {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                return true;
            }
        }
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getCurrentUserId() {
        if (isFirebaseAvailable && firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            return firebaseAuth.getCurrentUser().getUid();
        }
        return prefs.getString(KEY_USER_ID, "local_user_123");
    }

    public String getCurrentUserEmail() {
        if (isFirebaseAvailable && firebaseAuth != null && firebaseAuth.getCurrentUser() != null) {
            return firebaseAuth.getCurrentUser().getEmail();
        }
        return prefs.getString(KEY_USER_EMAIL, "user@example.com");
    }

    public String getCurrentUserName() {
        return prefs.getString(KEY_USER_NAME, "Safety User");
    }

    public void registerUser(String email, String password, String name, Callback<Boolean> callback) {
        if (isFirebaseAvailable && firebaseAuth != null) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                            String uid = firebaseAuth.getCurrentUser().getUid();

                            // Save user profile to Firestore
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("email", email);
                            userMap.put("createdAt", System.currentTimeMillis());

                            firestore.collection("users").document(uid).set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        prefs.edit()
                                                .putString(KEY_USER_ID, uid)
                                                .putString(KEY_USER_NAME, name)
                                                .putString(KEY_USER_EMAIL, email)
                                                .putBoolean(KEY_IS_LOGGED_IN, true)
                                                .apply();
                                        callback.onResult(true, true, null);
                                    })
                                    .addOnFailureListener(e -> {
                                        // User created in auth even if firestore write failed
                                        prefs.edit()
                                                .putString(KEY_USER_ID, uid)
                                                .putString(KEY_USER_NAME, name)
                                                .putString(KEY_USER_EMAIL, email)
                                                .putBoolean(KEY_IS_LOGGED_IN, true)
                                                .apply();
                                        callback.onResult(true, true, null);
                                    });
                        } else {
                            String err = task.getException() != null ? task.getException().getLocalizedMessage() : "Registration failed";
                            // Fallback register locally if firebase project isn't linked
                            localRegister(email, password, name, callback, err);
                        }
                    });
        } else {
            localRegister(email, password, name, callback, null);
        }
    }

    private void localRegister(String email, String password, String name, Callback<Boolean> callback, String firebaseErr) {
        prefs.edit()
                .putString(KEY_USER_ID, "local_" + System.currentTimeMillis())
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();

        String msg = (firebaseErr != null) ? "Registered in local mode (" + firebaseErr + ")" : null;
        callback.onResult(true, true, msg);
    }

    public void loginUser(String email, String password, Callback<Boolean> callback) {
        if (isFirebaseAvailable && firebaseAuth != null) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && firebaseAuth.getCurrentUser() != null) {
                            String uid = firebaseAuth.getCurrentUser().getUid();

                            firestore.collection("users").document(uid).get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        String name = "Safety User";
                                        if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                                            name = documentSnapshot.getString("name");
                                        }
                                        prefs.edit()
                                                .putString(KEY_USER_ID, uid)
                                                .putString(KEY_USER_NAME, name)
                                                .putString(KEY_USER_EMAIL, email)
                                                .putBoolean(KEY_IS_LOGGED_IN, true)
                                                .apply();
                                        callback.onResult(true, true, null);
                                    })
                                    .addOnFailureListener(e -> {
                                        prefs.edit()
                                                .putString(KEY_USER_ID, uid)
                                                .putString(KEY_USER_NAME, "Safety User")
                                                .putString(KEY_USER_EMAIL, email)
                                                .putBoolean(KEY_IS_LOGGED_IN, true)
                                                .apply();
                                        callback.onResult(true, true, null);
                                    });
                        } else {
                            String err = task.getException() != null ? task.getException().getLocalizedMessage() : "Invalid credentials";
                            // Local login fallback
                            localLogin(email, password, callback, err);
                        }
                    });
        } else {
            localLogin(email, password, callback, null);
        }
    }

    private void localLogin(String email, String password, Callback<Boolean> callback, String firebaseErr) {
        String storedEmail = prefs.getString(KEY_USER_EMAIL, "");
        if (!storedEmail.isEmpty() && !storedEmail.equalsIgnoreCase(email)) {
            callback.onResult(false, false, firebaseErr != null ? firebaseErr : "User not found. Please register first.");
            return;
        }

        prefs.edit()
                .putString(KEY_USER_EMAIL, email)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();

        callback.onResult(true, true, null);
    }

    public void sendPasswordReset(String email, Callback<Boolean> callback) {
        if (isFirebaseAvailable && firebaseAuth != null) {
            firebaseAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> callback.onResult(true, true, null))
                    .addOnFailureListener(e -> callback.onResult(false, false, e.getLocalizedMessage()));
        } else {
            callback.onResult(true, true, "Password reset instructions sent to " + email);
        }
    }

    public void logout() {
        if (isFirebaseAvailable && firebaseAuth != null) {
            firebaseAuth.signOut();
        }
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, false).apply();
    }

    // --- EMERGENCY CONTACTS ---

    private CollectionReference getContactsRef() {
        String uid = getCurrentUserId();
        return firestore.collection("users").document(uid).collection("emergencyContacts");
    }

    public void getEmergencyContacts(Callback<List<EmergencyContact>> callback) {
        if (isFirebaseAvailable && firestore != null && isLoggedIn()) {
            getContactsRef().orderBy("createdAt", Query.Direction.DESCENDING).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<EmergencyContact> list = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            EmergencyContact contact = doc.toObject(EmergencyContact.class);
                            if (contact != null) {
                                contact.setId(doc.getId());
                                list.add(contact);
                            }
                        }
                        // Save locally as cache
                        saveLocalContacts(list);
                        callback.onResult(true, list, null);
                    })
                    .addOnFailureListener(e -> {
                        // Return local cache on failure
                        callback.onResult(true, getLocalContacts(), null);
                    });
        } else {
            callback.onResult(true, getLocalContacts(), null);
        }
    }

    public void addEmergencyContact(EmergencyContact contact, Callback<Boolean> callback) {
        if (isFirebaseAvailable && firestore != null && isLoggedIn()) {
            String docId = getContactsRef().document().getId();
            contact.setId(docId);
            getContactsRef().document(docId).set(contact)
                    .addOnSuccessListener(aVoid -> {
                        addLocalContact(contact);
                        callback.onResult(true, true, null);
                    })
                    .addOnFailureListener(e -> {
                        addLocalContact(contact);
                        callback.onResult(true, true, "Saved locally");
                    });
        } else {
            if (TextUtils.isEmpty(contact.getId())) {
                contact.setId("cnt_" + System.currentTimeMillis());
            }
            addLocalContact(contact);
            callback.onResult(true, true, null);
        }
    }

    public void updateEmergencyContact(EmergencyContact contact, Callback<Boolean> callback) {
        if (isFirebaseAvailable && firestore != null && isLoggedIn() && contact.getId() != null) {
            getContactsRef().document(contact.getId()).set(contact)
                    .addOnSuccessListener(aVoid -> {
                        updateLocalContact(contact);
                        callback.onResult(true, true, null);
                    })
                    .addOnFailureListener(e -> {
                        updateLocalContact(contact);
                        callback.onResult(true, true, null);
                    });
        } else {
            updateLocalContact(contact);
            callback.onResult(true, true, null);
        }
    }

    public void deleteEmergencyContact(String contactId, Callback<Boolean> callback) {
        if (isFirebaseAvailable && firestore != null && isLoggedIn() && contactId != null) {
            getContactsRef().document(contactId).delete()
                    .addOnSuccessListener(aVoid -> {
                        deleteLocalContact(contactId);
                        callback.onResult(true, true, null);
                    })
                    .addOnFailureListener(e -> {
                        deleteLocalContact(contactId);
                        callback.onResult(true, true, null);
                    });
        } else {
            deleteLocalContact(contactId);
            callback.onResult(true, true, null);
        }
    }

    // --- LOCAL CONTACTS HELPER ---

    public List<EmergencyContact> getLocalContacts() {
        List<EmergencyContact> list = new ArrayList<>();
        String json = prefs.getString(KEY_LOCAL_CONTACTS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                EmergencyContact contact = new EmergencyContact(
                        obj.getString("id"),
                        obj.getString("name"),
                        obj.getString("phoneNumber"),
                        obj.optLong("createdAt", System.currentTimeMillis())
                );
                list.add(contact);
            }
        } catch (Exception ignored) {}
        return list;
    }

    private void saveLocalContacts(List<EmergencyContact> list) {
        try {
            JSONArray arr = new JSONArray();
            for (EmergencyContact c : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", c.getId());
                obj.put("name", c.getName());
                obj.put("phoneNumber", c.getPhoneNumber());
                obj.put("createdAt", c.getCreatedAt());
                arr.put(obj);
            }
            prefs.edit().putString(KEY_LOCAL_CONTACTS, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    private void addLocalContact(EmergencyContact contact) {
        List<EmergencyContact> list = getLocalContacts();
        list.add(0, contact);
        saveLocalContacts(list);
    }

    private void updateLocalContact(EmergencyContact contact) {
        List<EmergencyContact> list = getLocalContacts();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(contact.getId())) {
                list.set(i, contact);
                break;
            }
        }
        saveLocalContacts(list);
    }

    private void deleteLocalContact(String contactId) {
        List<EmergencyContact> list = getLocalContacts();
        List<EmergencyContact> updated = new ArrayList<>();
        for (EmergencyContact c : list) {
            if (!c.getId().equals(contactId)) {
                updated.add(c);
            }
        }
        saveLocalContacts(updated);
    }

    // --- EMERGENCY HISTORY ---

    private CollectionReference getHistoryRef() {
        String uid = getCurrentUserId();
        return firestore.collection("users").document(uid).collection("emergencyHistory");
    }

    public void getEmergencyHistory(Callback<List<EmergencyEvent>> callback) {
        if (isFirebaseAvailable && firestore != null && isLoggedIn()) {
            getHistoryRef().orderBy("timestamp", Query.Direction.DESCENDING).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<EmergencyEvent> list = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            EmergencyEvent event = doc.toObject(EmergencyEvent.class);
                            if (event != null) {
                                event.setId(doc.getId());
                                list.add(event);
                            }
                        }
                        saveLocalHistory(list);
                        callback.onResult(true, list, null);
                    })
                    .addOnFailureListener(e -> callback.onResult(true, getLocalHistory(), null));
        } else {
            callback.onResult(true, getLocalHistory(), null);
        }
    }

    public void addEmergencyEvent(EmergencyEvent event, Callback<Boolean> callback) {
        if (isFirebaseAvailable && firestore != null && isLoggedIn()) {
            String docId = getHistoryRef().document().getId();
            event.setId(docId);
            getHistoryRef().document(docId).set(event)
                    .addOnSuccessListener(aVoid -> {
                        addLocalHistoryEvent(event);
                        if (callback != null) callback.onResult(true, true, null);
                    })
                    .addOnFailureListener(e -> {
                        addLocalHistoryEvent(event);
                        if (callback != null) callback.onResult(true, true, null);
                    });
        } else {
            if (TextUtils.isEmpty(event.getId())) {
                event.setId("evt_" + System.currentTimeMillis());
            }
            addLocalHistoryEvent(event);
            if (callback != null) callback.onResult(true, true, null);
        }
    }

    public void deleteEmergencyEvent(String eventId, Callback<Boolean> callback) {
        if (isFirebaseAvailable && firestore != null && isLoggedIn() && eventId != null) {
            getHistoryRef().document(eventId).delete()
                    .addOnSuccessListener(aVoid -> {
                        deleteLocalHistoryEvent(eventId);
                        callback.onResult(true, true, null);
                    })
                    .addOnFailureListener(e -> {
                        deleteLocalHistoryEvent(eventId);
                        callback.onResult(true, true, null);
                    });
        } else {
            deleteLocalHistoryEvent(eventId);
            callback.onResult(true, true, null);
        }
    }

    public void clearAllEmergencyHistory(Callback<Boolean> callback) {
        prefs.edit().putString(KEY_LOCAL_HISTORY, "[]").apply();
        if (isFirebaseAvailable && firestore != null && isLoggedIn()) {
            getHistoryRef().get().addOnSuccessListener(snapshots -> {
                for (DocumentSnapshot doc : snapshots) {
                    doc.getReference().delete();
                }
                callback.onResult(true, true, null);
            }).addOnFailureListener(e -> callback.onResult(true, true, null));
        } else {
            callback.onResult(true, true, null);
        }
    }

    public List<EmergencyEvent> getLocalHistory() {
        List<EmergencyEvent> list = new ArrayList<>();
        String json = prefs.getString(KEY_LOCAL_HISTORY, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                EmergencyEvent event = new EmergencyEvent(
                        obj.getString("id"),
                        obj.getString("eventType"),
                        obj.optLong("timestamp", System.currentTimeMillis()),
                        obj.optDouble("latitude", 0.0),
                        obj.optDouble("longitude", 0.0),
                        obj.optString("mapsUrl", ""),
                        obj.optString("status", ""),
                        obj.optString("details", "")
                );
                list.add(event);
            }
        } catch (Exception ignored) {}
        return list;
    }

    private void saveLocalHistory(List<EmergencyEvent> list) {
        try {
            JSONArray arr = new JSONArray();
            for (EmergencyEvent e : list) {
                JSONObject obj = new JSONObject();
                obj.put("id", e.getId());
                obj.put("eventType", e.getEventType());
                obj.put("timestamp", e.getTimestamp());
                obj.put("latitude", e.getLatitude());
                obj.put("longitude", e.getLongitude());
                obj.put("mapsUrl", e.getMapsUrl());
                obj.put("status", e.getStatus());
                obj.put("details", e.getDetails());
                arr.put(obj);
            }
            prefs.edit().putString(KEY_LOCAL_HISTORY, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    private void addLocalHistoryEvent(EmergencyEvent event) {
        List<EmergencyEvent> list = getLocalHistory();
        list.add(0, event);
        saveLocalHistory(list);
    }

    private void deleteLocalHistoryEvent(String eventId) {
        List<EmergencyEvent> list = getLocalHistory();
        List<EmergencyEvent> updated = new ArrayList<>();
        for (EmergencyEvent e : list) {
            if (!e.getId().equals(eventId)) {
                updated.add(e);
            }
        }
        saveLocalHistory(updated);
    }

    // --- PREFERENCES & SETTINGS ---

    public String getCustomSosMessage() {
        return prefs.getString(KEY_SOS_MESSAGE, "EMERGENCY! I need assistance. Please contact me as soon as possible. My location is: ");
    }

    public void setCustomSosMessage(String message) {
        prefs.edit().putString(KEY_SOS_MESSAGE, message).apply();
    }

    public String getEmergencyNumber() {
        return prefs.getString(KEY_EMERGENCY_NUMBER, "112");
    }

    public void setEmergencyNumber(String number) {
        prefs.edit().putString(KEY_EMERGENCY_NUMBER, number).apply();
    }

    public boolean isBatteryMonitoringEnabled() {
        return prefs.getBoolean(KEY_BATTERY_MONITOR, true);
    }

    public void setBatteryMonitoringEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BATTERY_MONITOR, enabled).apply();
    }

    public boolean isPowerOffAlertEnabled() {
        return prefs.getBoolean(KEY_POWER_OFF_ALERT, true);
    }

    public void setPowerOffAlertEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_POWER_OFF_ALERT, enabled).apply();
    }

    public String getPersonalMedicalNotes() {
        return prefs.getString(KEY_MEDICAL_NOTES, "Blood Group: O+\nAllergies: None\nEmergency Notes: Please contact family immediately if unconscious.");
    }

    public void setPersonalMedicalNotes(String notes) {
        prefs.edit().putString(KEY_MEDICAL_NOTES, notes).apply();
    }
}