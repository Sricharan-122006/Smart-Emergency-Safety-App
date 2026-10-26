package com.sricharan.smartemergencysafetyapp;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sricharan.smartemergencysafetyapp.models.EmergencyEvent;
import com.sricharan.smartemergencysafetyapp.repository.FirebaseRepository;

import java.util.Locale;

public class SafetyTimerActivity extends AppCompatActivity {

    private TextView tvCountdown, tvStatus;
    private LinearLayout layoutPresetSelection;
    private EditText etCustomMinutes;
    private Button btnStartTimer, btnCancelTimer;
    private Button btn1Min, btn5Min, btn15Min, btn30Min;

    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private long timerTimeInMillis = 0;
    private Ringtone alarmRingtone;
    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_safety_timer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = FirebaseRepository.getInstance(this);

        tvCountdown = findViewById(R.id.tv_timer_countdown);
        tvStatus = findViewById(R.id.tv_timer_status);
        layoutPresetSelection = findViewById(R.id.layout_preset_selection);
        etCustomMinutes = findViewById(R.id.et_custom_minutes);

        btnStartTimer = findViewById(R.id.btn_start_timer);
        btnCancelTimer = findViewById(R.id.btn_cancel_timer);

        btn1Min = findViewById(R.id.btn_preset_1min);
        btn5Min = findViewById(R.id.btn_preset_5min);
        btn15Min = findViewById(R.id.btn_preset_15min);
        btn30Min = findViewById(R.id.btn_preset_30min);

        btn1Min.setOnClickListener(v -> setMinutes(1));
        btn5Min.setOnClickListener(v -> setMinutes(5));
        btn15Min.setOnClickListener(v -> setMinutes(15));
        btn30Min.setOnClickListener(v -> setMinutes(30));

        btnStartTimer.setOnClickListener(v -> startSafetyTimer());
        btnCancelTimer.setOnClickListener(v -> cancelSafetyTimer(true));
    }

    private void setMinutes(int minutes) {
        etCustomMinutes.setText(String.valueOf(minutes));
    }

    private void startSafetyTimer() {
        String input = etCustomMinutes.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            Toast.makeText(this, "Please select or enter minutes", Toast.LENGTH_SHORT).show();
            return;
        }

        int minutes = Integer.parseInt(input);
        if (minutes <= 0) {
            Toast.makeText(this, "Minutes must be greater than 0", Toast.LENGTH_SHORT).show();
            return;
        }

        timerTimeInMillis = minutes * 60 * 1000L;

        layoutPresetSelection.setVisibility(View.GONE);
        btnStartTimer.setVisibility(View.GONE);
        btnCancelTimer.setVisibility(View.VISIBLE);

        tvStatus.setText("Safety Timer Active - Stay Safe!");

        countDownTimer = new CountDownTimer(timerTimeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int mins = (int) (millisUntilFinished / 1000) / 60;
                int secs = (int) (millisUntilFinished / 1000) % 60;
                tvCountdown.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                tvCountdown.setText("00:00");
                tvStatus.setText("TIMER EXPIRED!");
                onTimerExpired();
            }
        }.start();

        isTimerRunning = true;
    }

    private void cancelSafetyTimer(boolean userInitiated) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;

        stopAlarm();

        layoutPresetSelection.setVisibility(View.VISIBLE);
        btnStartTimer.setVisibility(View.VISIBLE);
        btnCancelTimer.setVisibility(View.GONE);

        tvCountdown.setText("00:00");
        tvStatus.setText("Timer Idle");

        if (userInitiated) {
            Toast.makeText(this, "Safety Timer cancelled. Glad you are safe!", Toast.LENGTH_SHORT).show();
        }
    }

    private void onTimerExpired() {
        playAlarmAndVibrate();

        EmergencyEvent event = new EmergencyEvent(
                null,
                "Safety Timer Expired",
                System.currentTimeMillis(),
                0.0, 0.0, "N/A",
                "Timer Expired - Prompted Alert",
                "User safety timer ended without cancellation"
        );
        repository.addEmergencyEvent(event, null);

        new AlertDialog.Builder(this)
                .setTitle("SAFETY TIMER EXPIRED!")
                .setMessage("Your safety timer has finished! Are you safe?")
                .setCancelable(false)
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton("I AM SAFE", (dialog, which) -> cancelSafetyTimer(false))
                .setNegativeButton("TRIGGER EMERGENCY SOS NOW", (dialog, which) -> {
                    stopAlarm();
                    Intent intent = new Intent(SafetyTimerActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                })
                .show();
    }

    private void playAlarmAndVibrate() {
        try {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            alarmRingtone = RingtoneManager.getRingtone(getApplicationContext(), alarmUri);
            if (alarmRingtone != null) {
                alarmRingtone.play();
            }

            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 500, 500, 500}, 0));
                } else {
                    vibrator.vibrate(new long[]{0, 500, 500, 500}, 0);
                }
            }
        } catch (Exception ignored) {}
    }

    private void stopAlarm() {
        try {
            if (alarmRingtone != null && alarmRingtone.isPlaying()) {
                alarmRingtone.stop();
            }
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.cancel();
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTimerRunning && countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopAlarm();
    }
}