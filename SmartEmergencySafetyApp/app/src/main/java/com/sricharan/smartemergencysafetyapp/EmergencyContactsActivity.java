package com.sricharan.smartemergencysafetyapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sricharan.smartemergencysafetyapp.adapters.EmergencyContactAdapter;
import com.sricharan.smartemergencysafetyapp.models.EmergencyContact;
import com.sricharan.smartemergencysafetyapp.repository.FirebaseRepository;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity {

    private RecyclerView rvContacts;
    private ProgressBar pbContacts;
    private TextView tvEmptyContacts;
    private FloatingActionButton fabAddContact;
    private EmergencyContactAdapter adapter;
    private FirebaseRepository repository;
    private List<EmergencyContact> contactList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_emergency_contacts);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = FirebaseRepository.getInstance(this);

        rvContacts = findViewById(R.id.rv_contacts);
        pbContacts = findViewById(R.id.pb_contacts);
        tvEmptyContacts = findViewById(R.id.tv_empty_contacts);
        fabAddContact = findViewById(R.id.fab_add_contact);

        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmergencyContactAdapter(contactList, new EmergencyContactAdapter.OnContactClickListener() {
            @Override
            public void onEdit(EmergencyContact contact) {
                showAddOrEditContactDialog(contact);
            }

            @Override
            public void onDelete(EmergencyContact contact) {
                confirmDeleteContact(contact);
            }
        });
        rvContacts.setAdapter(adapter);

        findViewById(R.id.btn_add_contact_top).setOnClickListener(v -> showAddOrEditContactDialog(null));
        fabAddContact.setOnClickListener(v -> showAddOrEditContactDialog(null));

        loadContacts();
    }

    private void loadContacts() {
        pbContacts.setVisibility(View.VISIBLE);
        repository.getEmergencyContacts((success, contacts, errorMessage) -> {
            pbContacts.setVisibility(View.GONE);
            if (contacts != null) {
                contactList = contacts;
                adapter.setContactList(contactList);
            }
            updateEmptyState();
        });
    }

    private void updateEmptyState() {
        if (contactList.isEmpty()) {
            tvEmptyContacts.setVisibility(View.VISIBLE);
            rvContacts.setVisibility(View.GONE);
        } else {
            tvEmptyContacts.setVisibility(View.GONE);
            rvContacts.setVisibility(View.VISIBLE);
        }
    }

    private void showAddOrEditContactDialog(EmergencyContact existingContact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(existingContact == null ? "Add Emergency Contact" : "Edit Emergency Contact");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);
        builder.setView(dialogView);

        EditText etName = dialogView.findViewById(R.id.et_dialog_contact_name);
        EditText etPhone = dialogView.findViewById(R.id.et_dialog_contact_phone);

        if (existingContact != null) {
            etName.setText(existingContact.getName());
            etPhone.setText(existingContact.getPhoneNumber());
        }

        builder.setPositiveButton(existingContact == null ? "Add" : "Save", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                etName.setError("Name is required");
                return;
            }

            if (TextUtils.isEmpty(phone) || phone.length() < 7) {
                etPhone.setError("Please enter a valid phone number");
                return;
            }

            if (existingContact == null) {
                EmergencyContact newContact = new EmergencyContact(null, name, phone, System.currentTimeMillis());
                repository.addEmergencyContact(newContact, (success, data, errorMessage) -> {
                    Toast.makeText(EmergencyContactsActivity.this, "Contact Added", Toast.LENGTH_SHORT).show();
                    loadContacts();
                });
            } else {
                existingContact.setName(name);
                existingContact.setPhoneNumber(phone);
                repository.updateEmergencyContact(existingContact, (success, data, errorMessage) -> {
                    Toast.makeText(EmergencyContactsActivity.this, "Contact Updated", Toast.LENGTH_SHORT).show();
                    loadContacts();
                });
            }
            dialog.dismiss();
        });
    }

    private void confirmDeleteContact(EmergencyContact contact) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete " + contact.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    repository.deleteEmergencyContact(contact.getId(), (success, data, errorMessage) -> {
                        Toast.makeText(EmergencyContactsActivity.this, "Contact Deleted", Toast.LENGTH_SHORT).show();
                        loadContacts();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}