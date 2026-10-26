# Smart Emergency Safety App

A complete, modern, professional Android personal safety application built using **Java**, **XML**, **Firebase Authentication**, **Firebase Cloud Firestore**, and **Google Play Services Location**.

---

## 📱 Features & Highlights

1. **Splash Screen**:
   - Polished branding with safety icon, tagline ("*Your Safety, One Tap Away*"), and automatic authentication status check.

2. **Authentication System**:
   - Secure email & password registration and login via Firebase Authentication with local offline fallback.
   - Password reset functionality.

3. **Smart SOS Emergency System**:
   - Prominent red SOS button on the main dashboard.
   - Acquires high-accuracy GPS coordinates using Google Play Services Location API.
   - Generates Google Maps URL (`https://maps.google.com/?q=lat,lng`).
   - Retrieves trusted emergency contacts and prepares an emergency SMS alert prefilled with message and map link.
   - Records the SOS event in Emergency History.
   - Clear confirmation dialogs and handles missing contacts or unavailable location gracefully without crashing.

4. **Emergency Contacts Management**:
   - Add, Edit, and Delete trusted emergency contacts.
   - Validates names and phone numbers.
   - Synchronizes automatically with Cloud Firestore (`users/{userId}/emergencyContacts/{contactId}`) and caches locally.

5. **Real-time Location Sharing**:
   - Detects current latitude and longitude.
   - Provides options to open in Google Maps, copy map link, or share via Android system share sheet.

6. **Safety Timer**:
   - Select preset safety durations (1 min, 5 min, 15 min, 30 min) or custom minutes.
   - Ticking countdown display with "I am Safe" cancellation button.
   - Triggers warning sound, vibration, and an alert dialog if time expires.

7. **Fake Call Simulator**:
   - Set up caller name, caller number, and delay (Immediate, 5s, 10s, 30s).
   - In-app realistic incoming call screen with ringer audio and vibration.
   - Accept call to see active call timer or decline call to dismiss.

8. **Emergency History**:
   - Chronological log of all SOS alerts, location shares, and safety timer events.
   - Delete individual event records or clear all history.

9. **Emergency Information & Helplines**:
   - Pre-configured regional emergency numbers (112 National Emergency, 100 Police, 101 Fire, 102 Ambulance, 1091 Women Helpline).
   - One-tap dialer launch (`Intent.ACTION_DIAL`).
   - Personal medical notes editor (Blood Group, Allergies, Medical Conditions).

10. **Low-Battery Warning & Settings**:
    - Automatic low-battery detection (when battery drops below 15%).
    - Custom SOS message template editor.
    - Logout option.

---

## 🛠️ Technology Stack

- **Language**: Java
- **UI Design**: XML layouts with Material Design 3 guidelines
- **Backend Services**: Firebase Authentication & Firebase Cloud Firestore
- **Location Services**: Google Play Services Location API (`FusedLocationProviderClient`)
- **Storage**: SharedPreferences & Cloud Firestore

---

## 🔥 Firebase Setup Guide

The application includes a placeholder `google-services.json` file to allow immediate local building and testing out of the box. To connect your own live Firebase backend:

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add Project** and name it `SmartEmergencySafetyApp`.
3. In the project dashboard, click **Add App** -> select **Android**.
4. Enter the package name: `com.sricharan.smartemergencysafetyapp`.
5. Download `google-services.json`.
6. Replace the placeholder file at: `app/google-services.json`.
7. In Firebase Console:
   - Go to **Authentication** -> **Sign-in method** -> Enable **Email/Password**.
   - Go to **Firestore Database** -> Click **Create Database** -> Start in test mode or apply rules from `firestore.rules`.

---

## ⚙️ How to Build and Run

### 1. Build using Android Studio
- Open Android Studio.
- Select `Smart Emergency Safety App`.
- Click **Build** -> **Make Project** (`Ctrl + F9` or `Cmd + F9`).
- Run on an Android Emulator or physical smartphone running Android 7.0 (API 24) or higher.

### 2. Generate Debug APK using Gradle
Run the following command in terminal:
```bash
./gradlew assembleDebug
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🧪 Testing Checklist

- [x] Launch app -> Verify Splash screen transitions smoothly.
- [x] Register new account -> Verify successful creation.
- [x] Login with credentials -> Verify navigation to Dashboard.
- [x] Emergency Contacts -> Add, edit, and delete contacts.
- [x] SOS Alert -> Tap SOS -> Confirm -> Verify SMS intent opens with contacts and Google Maps link.
- [x] Share Location -> Detect location -> Verify coordinates and Google Maps link.
- [x] Safety Timer -> Set 1 min -> Start -> Verify countdown and alarm on expiration.
- [x] Fake Call Simulator -> Schedule call with 5s delay -> Verify incoming call screen and sound/vibration.
- [x] Emergency History -> Verify logged events and deletion.
- [x] Emergency Info -> Tap Dial on 112 -> Verify dialer opens with number prefilled.
