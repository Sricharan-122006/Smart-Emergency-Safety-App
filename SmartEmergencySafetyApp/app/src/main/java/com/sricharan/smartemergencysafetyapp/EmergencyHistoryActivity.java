package com.sricharan.smartemergencysafetyapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sricharan.smartemergencysafetyapp.adapters.EmergencyHistoryAdapter;
import com.sricharan.smartemergencysafetyapp.models.EmergencyEvent;
import com.sricharan.smartemergencysafetyapp.repository.FirebaseRepository;

import java.util.ArrayList;
import java.util.List;

public class EmergencyHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private ProgressBar pbHistory;
    private TextView tvEmptyHistory;
    private Button btnClearHistory;
    private EmergencyHistoryAdapter adapter;
    private FirebaseRepository repository;
    private List<EmergencyEvent> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emergency_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = FirebaseRepository.getInstance(this);

        rvHistory = findViewById(R.id.rv_history);
        pbHistory = findViewById(R.id.pb_history);
        tvEmptyHistory = findViewById(R.id.tv_empty_history);
        btnClearHistory = findViewById(R.id.btn_clear_history);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmergencyHistoryAdapter(historyList, event -> confirmDeleteEvent(event));
        rvHistory.setAdapter(adapter);

        btnClearHistory.setOnClickListener(v -> confirmClearAll());

        loadHistory();
    }

    private void loadHistory() {
        pbHistory.setVisibility(View.VISIBLE);
        repository.getEmergencyHistory((success, history, errorMessage) -> {
            pbHistory.setVisibility(View.GONE);
            if (history != null) {
                historyList = history;
                adapter.setHistoryList(historyList);
            }
            updateEmptyState();
        });
    }

    private void updateEmptyState() {
        if (historyList.isEmpty()) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            rvHistory.setVisibility(View.GONE);
            btnClearHistory.setEnabled(false);
        } else {
            tvEmptyHistory.setVisibility(View.GONE);
            rvHistory.setVisibility(View.VISIBLE);
            btnClearHistory.setEnabled(true);
        }
    }

    private void confirmDeleteEvent(EmergencyEvent event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event Record")
                .setMessage("Are you sure you want to remove this emergency record?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deleteEmergencyEvent(event.getId(), (success, data, errorMessage) -> {
                        Toast.makeText(EmergencyHistoryActivity.this, "Record deleted", Toast.LENGTH_SHORT).show();
                        loadHistory();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmClearAll() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All History")
                .setMessage("Are you sure you want to delete all emergency history records?")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    repository.clearAllEmergencyHistory((success, data, errorMessage) -> {
                        Toast.makeText(EmergencyHistoryActivity.this, "History cleared", Toast.LENGTH_SHORT).show();
                        loadHistory();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}