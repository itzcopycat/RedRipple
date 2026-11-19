package com.example.redripple.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.redripple.MainActivity;
import com.example.redripple.R;
import com.google.android.material.button.MaterialButton;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmailOrPhone, tilPassword;
    private TextInputEditText etEmailOrPhone, etPassword;
    private MaterialCheckBox cbRememberMe;
    private MaterialButton btnLogin;
    private ProgressBar progressLogin;
    private View tvGoToSignup;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Replace with your backend API URL (e.g., http://10.0.2.2/redripple/login.php for local XAMPP/WAMP)
    private static final String LOGIN_URL = "http://10.0.2.2/redripple/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tilEmailOrPhone = findViewById(R.id.tilEmailOrPhone);
        tilPassword = findViewById(R.id.tilPassword);
        etEmailOrPhone = findViewById(R.id.etEmailOrPhone);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
        progressLogin = findViewById(R.id.progressLogin);
        tvGoToSignup = findViewById(R.id.tvGoToSignup);

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvGoToSignup.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void attemptLogin() {
        String emailOrPhone = etEmailOrPhone.getText() != null ? etEmailOrPhone.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        boolean cancel = false;

        if (emailOrPhone.isEmpty()) {
            tilEmailOrPhone.setError("Field cannot be empty");
            cancel = true;
        } else {
            tilEmailOrPhone.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password cannot be empty");
            cancel = true;
        } else {
            tilPassword.setError(null);
        }

        if (cancel) {
            return;
        }

        setLoading(true);

        executorService.execute(() -> {
            String result = "";
            try {
                URL url = new URL(LOGIN_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("email_or_phone", emailOrPhone);
                jsonParam.put("password", password);

                OutputStream os = conn.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
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
                handleLoginResponse(finalResult);
            });
        });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            progressLogin.setVisibility(View.VISIBLE);
            btnLogin.setText("");
            btnLogin.setEnabled(false);
        } else {
            progressLogin.setVisibility(View.GONE);
            btnLogin.setText(R.string.btn_login);
            btnLogin.setEnabled(true);
        }
    }

    private void handleLoginResponse(String responseStr) {
        try {
            JSONObject jsonObject = new JSONObject(responseStr);
            String status = jsonObject.optString("status", "error");
            String message = jsonObject.optString("message", "Unknown error");

            if ("success".equals(status)) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            if (responseStr.contains("success") || responseStr.isEmpty()) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
