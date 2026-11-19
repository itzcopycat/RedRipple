package com.example.redripple.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.redripple.MainActivity;
import com.example.redripple.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignupActivity extends AppCompatActivity {

    private TextInputLayout tilFullName, tilEmail, tilPhone, tilPassword, tilConfirmPassword, tilBloodGroup;
    private TextInputEditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private AutoCompleteTextView actvBloodGroup;
    private MaterialButtonToggleGroup toggleAccountType;
    private MaterialCheckBox cbTerms;
    private MaterialButton btnCreateAccount;
    private ProgressBar progressSignup;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Replace with your backend API URL (e.g., http://10.0.2.2/redripple/signup.php for local XAMPP/WAMP)
    private static final String SIGNUP_URL = "http://10.0.2.2/redripple/signup.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilBloodGroup = findViewById(R.id.tilBloodGroup);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        actvBloodGroup = findViewById(R.id.actvBloodGroup);

        toggleAccountType = findViewById(R.id.toggleAccountType);
        cbTerms = findViewById(R.id.cbTerms);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        progressSignup = findViewById(R.id.progressSignup);
        View tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Setup Blood Group dropdown adapter
        String[] bloodGroups = new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bloodGroups);
        actvBloodGroup.setAdapter(adapter);

        btnCreateAccount.setOnClickListener(v -> attemptSignup());

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptSignup() {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";
        String bloodGroup = actvBloodGroup.getText() != null ? actvBloodGroup.getText().toString().trim() : "";

        int checkedButtonId = toggleAccountType.getCheckedButtonId();
        String accountType = (checkedButtonId == R.id.btnRecipient) ? "Recipient" : "Donor";

        boolean cancel = false;

        if (fullName.isEmpty()) {
            tilFullName.setError("Full name required");
            cancel = true;
        } else {
            tilFullName.setError(null);
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Valid email required");
            cancel = true;
        } else {
            tilEmail.setError(null);
        }

        if (phone.isEmpty() || phone.length() < 10) {
            tilPhone.setError("Valid phone number required");
            cancel = true;
        } else {
            tilPhone.setError(null);
        }

        if (password.isEmpty() || password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            cancel = true;
        } else {
            tilPassword.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            cancel = true;
        } else {
            tilConfirmPassword.setError(null);
        }

        if (bloodGroup.isEmpty()) {
            tilBloodGroup.setError("Select blood group");
            cancel = true;
        } else {
            tilBloodGroup.setError(null);
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "You must accept Terms & Conditions", Toast.LENGTH_SHORT).show();
            cancel = true;
        }

        if (cancel) {
            return;
        }

        setLoading(true);

        executorService.execute(() -> {
            String result;
            try {
                URL url = new URL(SIGNUP_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("fullname", fullName);
                jsonParam.put("email", email);
                jsonParam.put("phone", phone);
                jsonParam.put("password", password);
                jsonParam.put("blood_group", bloodGroup);
                jsonParam.put("account_type", accountType);

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    result = response.toString();
                } else {
                    result = "{\"status\":\"error\",\"message\":\"Server error code: " + responseCode + "\"}";
                }
            } catch (Exception e) {
                result = "{\"status\":\"error\",\"message\":\"Connection failed: " + e.getMessage() + "\"}";
            }

            final String finalResult = result;
            runOnUiThread(() -> {
                setLoading(false);
                handleSignupResponse(finalResult);
            });
        });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            progressSignup.setVisibility(View.VISIBLE);
            btnCreateAccount.setText("");
            btnCreateAccount.setEnabled(false);
        } else {
            progressSignup.setVisibility(View.GONE);
            btnCreateAccount.setText(R.string.btn_create_account);
            btnCreateAccount.setEnabled(true);
        }
    }

    private void handleSignupResponse(String responseStr) {
        try {
            JSONObject jsonObject = new JSONObject(responseStr);
            String status = jsonObject.optString("status", "error");
            String message = jsonObject.optString("message", "Unknown error");

            if ("success".equals(status)) {
                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            if (responseStr.contains("success") || responseStr.isEmpty()) {
                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
