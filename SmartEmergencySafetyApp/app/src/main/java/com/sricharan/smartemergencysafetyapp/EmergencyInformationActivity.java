package com.sricharan.smartemergencysafetyapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sricharan.smartemergencysafetyapp.repository.FirebaseRepository;

public class EmergencyInformationActivity extends AppCompatActivity {

    private EditText etMedicalNotes;
    private Button btnSaveNotes;
    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emergency_information);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = FirebaseRepository.getInstance(this);

        etMedicalNotes = findViewById(R.id.et_medical_notes);
        btnSaveNotes = findViewById(R.id.btn_save_medical_notes);

        etMedicalNotes.setText(repository.getPersonalMedicalNotes());

        btnSaveNotes.setOnClickListener(v -> {
            String notes = etMedicalNotes.getText().toString().trim();
            repository.setPersonalMedicalNotes(notes);
            Toast.makeText(this, "Medical notes saved!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_dial_112).setOnClickListener(v -> openDialer("112"));
        findViewById(R.id.btn_dial_100).setOnClickListener(v -> openDialer("100"));
        findViewById(R.id.btn_dial_101).setOnClickListener(v -> openDialer("101"));
        findViewById(R.id.btn_dial_102).setOnClickListener(v -> openDialer("102"));
        findViewById(R.id.btn_dial_1091).setOnClickListener(v -> openDialer("1091"));
    }

    private void openDialer(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }
}