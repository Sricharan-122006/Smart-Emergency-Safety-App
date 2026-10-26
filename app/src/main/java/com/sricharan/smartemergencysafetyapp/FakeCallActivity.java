package com.sricharan.smartemergencysafetyapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class FakeCallActivity extends AppCompatActivity {

    private EditText etCallerName, etCallerNumber;
    private Button btnDelayNow, btnDelay5s, btnDelay10s, btnDelay30s, btnTriggerFakeCall;
    private int selectedDelaySeconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fake_call);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etCallerName = findViewById(R.id.et_caller_name);
        etCallerNumber = findViewById(R.id.et_caller_number);

        btnDelayNow = findViewById(R.id.btn_delay_now);
        btnDelay5s = findViewById(R.id.btn_delay_5s);
        btnDelay10s = findViewById(R.id.btn_delay_10s);
        btnDelay30s = findViewById(R.id.btn_delay_30s);
        btnTriggerFakeCall = findViewById(R.id.btn_trigger_fake_call);

        btnDelayNow.setOnClickListener(v -> selectedDelaySeconds = 0);
        btnDelay5s.setOnClickListener(v -> selectedDelaySeconds = 5);
        btnDelay10s.setOnClickListener(v -> selectedDelaySeconds = 10);
        btnDelay30s.setOnClickListener(v -> selectedDelaySeconds = 30);

        btnTriggerFakeCall.setOnClickListener(v -> scheduleFakeCall());
    }

    private void scheduleFakeCall() {
        String name = etCallerName.getText().toString().trim();
        String number = etCallerNumber.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            name = "Mom";
        }
        if (TextUtils.isEmpty(number)) {
            number = "+1 800-555-0199";
        }

        final String finalName = name;
        final String finalNumber = number;

        if (selectedDelaySeconds == 0) {
            launchFakeCallScreen(finalName, finalNumber);
        } else {
            Toast.makeText(this, "Fake call scheduled in " + selectedDelaySeconds + " seconds!", Toast.LENGTH_SHORT).show();
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                    launchFakeCallScreen(finalName, finalNumber), selectedDelaySeconds * 1000L);
        }
    }

    private void launchFakeCallScreen(String name, String number) {
        Intent intent = new Intent(FakeCallActivity.this, FakeCallIncomingActivity.class);
        intent.putExtra("caller_name", name);
        intent.putExtra("caller_number", number);
        startActivity(intent);
    }
}