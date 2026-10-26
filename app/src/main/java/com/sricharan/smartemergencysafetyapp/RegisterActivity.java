package com.sricharan.smartemergencysafetyapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sricharan.smartemergencysafetyapp.repository.FirebaseRepository;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvGoLogin;
    private ProgressBar pbRegister;
    private FirebaseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = FirebaseRepository.getInstance(this);

        etName = findViewById(R.id.et_register_name);
        etEmail = findViewById(R.id.et_register_email);
        etPassword = findViewById(R.id.et_register_password);
        etConfirmPassword = findViewById(R.id.et_register_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvGoLogin = findViewById(R.id.tv_go_login);
        pbRegister = findViewById(R.id.pb_register);

        btnRegister.setOnClickListener(v -> handleRegister());

        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        showLoading(true);

        repository.registerUser(email, password, name, (success, data, errorMessage) -> {
            showLoading(false);
            if (success) {
                Toast.makeText(RegisterActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Registration Failed: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            pbRegister.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
        } else {
            pbRegister.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
        }
    }
}