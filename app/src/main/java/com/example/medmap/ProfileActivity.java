package com.example.medmap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "appointment_channel";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    private DatabaseReference databaseReference;

    FirebaseAuth auth;
    FirebaseUser user;
    TextView profileText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("appointments");
        Log.d("ProfileActivity", "Database reference initialized: " + databaseReference);

        createNotificationChannel();

        // Check and request notification permission if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    public void openClinics(View view) {
        Intent intent = new Intent(this, ClinicsActivity.class);
        startActivity(intent);
    }

    public void openHospitals(View view) {
        Intent intent = new Intent(this, HospitalsActivity.class);
        startActivity(intent);
    }

    public void openPharmacies(View view) {
        Intent intent = new Intent(this, PharmaciesActivity.class);
        startActivity(intent);
    }

    public void openManageProfile(View view) {
        Intent intent = new Intent(this, ManageProfileActivity.class);
        startActivity(intent);
    }

    public void submitAppointment(View view) {
        EditText fullNameEditText = findViewById(R.id.full_name_edit_text);
        EditText phoneNumberEditText = findViewById(R.id.phone_number_edit_text);
        EditText dateOfAppointmentEditText = findViewById(R.id.date_of_appointment_edit_text);
        EditText additionalNotesEditText = findViewById(R.id.additional_notes_edit_text);

        String fullName = fullNameEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String dateOfAppointment = dateOfAppointmentEditText.getText().toString().trim();
        String additionalNotes = additionalNotesEditText.getText().toString().trim();

        // Handle form submission, e.g., validate inputs and send data to server or start new activity
        if (fullName.isEmpty() || phoneNumber.isEmpty() || dateOfAppointment.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create an appointment object
        Appointment appointment = new Appointment(fullName, phoneNumber, dateOfAppointment, additionalNotes);

        // Store appointment in Firebase
        databaseReference.push().setValue(appointment)
                .addOnSuccessListener(aVoid -> {
                    // Show success message
                    Toast.makeText(this, "Appointment submitted successfully", Toast.LENGTH_SHORT).show();

                    // Trigger local notification if permission is granted
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                                    == PackageManager.PERMISSION_GRANTED) {
                        triggerNotification("New Appointment Booked", "Appointment for " + fullName + " on " + dateOfAppointment);
                    }

                    // Clear form fields
                    fullNameEditText.setText("");
                    phoneNumberEditText.setText("");
                    dateOfAppointmentEditText.setText("");
                    additionalNotesEditText.setText("");
                })
                .addOnFailureListener(e -> {
                    // Show error message
                    Toast.makeText(this, "Failed to submit appointment", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Appointment Channel";
            String description = "Channel for appointment notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void triggerNotification(String title, String message) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_notification) // Ensure this resource exists
                    .build();
        } else {
            notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_notification) // Ensure this resource exists
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1, notification);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
