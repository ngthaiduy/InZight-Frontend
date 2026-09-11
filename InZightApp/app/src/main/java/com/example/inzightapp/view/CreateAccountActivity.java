package com.example.inzightapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inzightapp.R;

public class CreateAccountActivity extends AppCompatActivity {

    private ImageButton backButton;
    private EditText fullNameEditText;
    private Spinner daySpinner, monthSpinner, yearSpinner, sexSpinner;
    private EditText contactEditText;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        backButton = findViewById(R.id.backButtonAccount);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        daySpinner = findViewById(R.id.daySpinner);
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        sexSpinner = findViewById(R.id.sexSpinner);
        contactEditText = findViewById(R.id.contactEditText);
        nextButton = findViewById(R.id.nextButton);

        backButton.setOnClickListener(v -> finish());

        nextButton.setOnClickListener(v -> {
            String fullName = fullNameEditText.getText().toString().trim();
            String contact = contactEditText.getText().toString().trim();

            if (TextUtils.isEmpty(fullName)) {
                fullNameEditText.setError("Please enter your full name.");
                fullNameEditText.requestFocus();
                return;
            }

            if (daySpinner.getSelectedItemPosition() == 0 ||
                    monthSpinner.getSelectedItemPosition() == 0 ||
                    yearSpinner.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select full date of birth.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (sexSpinner.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select your sex.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(contact)) {
                contactEditText.setError("Vui lòng nhập email.");
                contactEditText.requestFocus();
                return;
            }

            // Validate email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(contact).matches()) {
                contactEditText.setError("Email không hợp lệ. Vui lòng nhập email đúng định dạng.");
                contactEditText.requestFocus();
                return;
            }

            String day = daySpinner.getSelectedItem().toString();
            String month = monthSpinner.getSelectedItem().toString();
            String year = yearSpinner.getSelectedItem().toString();

            // Normalize dob to yyyy-MM-dd
            String dob = String.format("%s-%s-%s", year, padLeft(month), padLeft(day));

            Intent intent = new Intent(CreateAccountActivity.this, CreatePasswordActivity.class);
            intent.putExtra("fullName", fullName);
            intent.putExtra("contact", contact);
            intent.putExtra("dateOfBirth", dob);
            intent.putExtra("gender", sexSpinner.getSelectedItem().toString());
            intent.putExtra("username", contact);

            startActivity(intent);
        });
    }

    private String padLeft(String s) {
        if (s.length() == 1) return "0" + s;
        return s;
    }
}
