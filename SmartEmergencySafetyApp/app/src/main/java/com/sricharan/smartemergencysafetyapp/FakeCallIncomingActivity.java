package com.sricharan.smartemergencysafetyapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.sricharan.smartemergencysafetyapp.models.EmergencyEvent;
import com.sricharan.smartemergencysafetyapp.repository.FirebaseRepository;

import java.util.Locale;

public class FakeCallIncomingActivity extends AppCompatActivity {

    private TextView tvName, tvNumber, tvStatus;
    private LinearLayout layoutIncomingActions, layoutActiveActions;
    private ImageButton btnAccept, btnDecline, btnHangup;

    private Ringtone ringtone;
    private Vibrator vibrator;
    private boolean isCallConnected = false;
    private int callSeconds = 0;
    private Handler callTimerHandler = new Handler(Looper.getMainLooper());
    private Runnable callTimerRunnable;
    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_fake_call_incoming);

        repository = FirebaseRepository.getInstance(this);

        tvName = findViewById(R.id.tv_sim_caller_name);
        tvNumber = findViewById(R.id.tv_sim_caller_number);
        tvStatus = findViewById(R.id.tv_sim_call_status);

        layoutIncomingActions = findViewById(R.id.layout_incoming_actions);
        layoutActiveActions = findViewById(R.id.layout_active_call_actions);

        btnAccept = findViewById(R.id.btn_sim_accept);
        btnDecline = findViewById(R.id.btn_sim_decline);
        btnHangup = findViewById(R.id.btn_sim_hangup);

        String name = getIntent().getStringExtra("caller_name");
        String number = getIntent().getStringExtra("caller_number");

        if (name != null) tvName.setText(name);
        if (number != null) tvNumber.setText(number);

        btnAccept.setOnClickListener(v -> acceptCall());
        btnDecline.setOnClickListener(v -> endSimulation("Declined"));
        btnHangup.setOnClickListener(v -> endSimulation("Ended by user"));

        startRingtoneAndVibration();
    }

    @SuppressLint("MissingPermission")
    private void startRingtoneAndVibration() {
        try {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            if (ringtoneUri == null) {
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
            if (ringtone != null) {
                ringtone.play();
            }

            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 1000, 1000}, 0));
                } else {
                    vibrator.vibrate(new long[]{0, 1000, 1000}, 0);
                }
            }
        } catch (Exception ignored) {}
    }

    @SuppressLint("MissingPermission")
    private void stopRingtoneAndVibration() {
        try {
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }
            if (vibrator != null) {
                vibrator.cancel();
            }
        } catch (Exception ignored) {}
    }

    private void acceptCall() {
        isCallConnected = true;
        stopRingtoneAndVibration();

        layoutIncomingActions.setVisibility(View.GONE);
        layoutActiveActions.setVisibility(View.VISIBLE);

        tvStatus.setText("Call Connected 00:00");
        tvStatus.setTextColor(0xFF4CAF50);

        callTimerRunnable = new Runnable() {
            @Override
            public void run() {
                callSeconds++;
                int mins = callSeconds / 60;
                int secs = callSeconds % 60;
                tvStatus.setText(String.format(Locale.getDefault(), "Call Connected %02d:%02d", mins, secs));
                callTimerHandler.postDelayed(this, 1000);
            }
        };
        callTimerHandler.postDelayed(callTimerRunnable, 1000);
    }

    private void endSimulation(String action) {
        stopRingtoneAndVibration();
        if (callTimerRunnable != null) {
            callTimerHandler.removeCallbacks(callTimerRunnable);
        }

        // Record history record
        String details = "Simulated call with " + tvName.getText().toString() + " (" + action + ")";
        EmergencyEvent event = new EmergencyEvent(
                null,
                "Fake Call Simulation",
                System.currentTimeMillis(),
                0.0, 0.0, "N/A",
                "Simulation Completed",
                details
        );
        repository.addEmergencyEvent(event, null);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRingtoneAndVibration();
        if (callTimerRunnable != null) {
            callTimerHandler.removeCallbacks(callTimerRunnable);
        }
    }
}