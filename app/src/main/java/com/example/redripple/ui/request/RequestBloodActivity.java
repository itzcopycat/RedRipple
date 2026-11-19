package com.example.redripple.ui.request;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.redripple.R;
import com.google.android.material.button.MaterialButton;
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

public class RequestBloodActivity extends AppCompatActivity {

    private TextInputLayout tilPatientName, tilRequestBloodGroup, tilUnitsRequired, tilHospital, tilRequestPhone;
    private TextInputEditText etPatientName, etUnitsRequired, etHospital, etRequestPhone;
    private AutoCompleteTextView actvRequestBloodGroup;
    private MaterialButton btnSubmitRequest;
    private ProgressBar progressRequestBlood;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Replace with your backend API URL (e.g., http://10.0.2.2/redripple/request_blood.php for local XAMPP/WAMP)
    private static final String REQUEST_URL = "http://10.0.2.2/redripple/request_blood.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_blood);

        tilPatientName = findViewById(R.id.tilPatientName);
        tilRequestBloodGroup = findViewById(R.id.tilRequestBloodGroup);
        tilUnitsRequired = findViewById(R.id.tilUnitsRequired);
        tilHospital = findViewById(R.id.tilHospital);
        tilRequestPhone = findViewById(R.id.tilRequestPhone);

        etPatientName = findViewById(R.id.etPatientName);
        actvRequestBloodGroup = findViewById(R.id.actvRequestBloodGroup);
        etUnitsRequired = findViewById(R.id.etUnitsRequired);
        etHospital = findViewById(R.id.etHospital);
        etRequestPhone = findViewById(R.id.etRequestPhone);

        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
        progressRequestBlood = findViewById(R.id.progressRequestBlood);

        // Setup Blood Group dropdown adapter
        String[] bloodGroups = new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, bloodGroups);
        actvRequestBloodGroup.setAdapter(adapter);

        btnSubmitRequest.setOnClickListener(v -> attemptSubmitRequest());
    }

    private void attemptSubmitRequest() {
        String patientName = etPatientName.getText() != null ? etPatientName.getText().toString().trim() : "";
        String bloodGroup = actvRequestBloodGroup.getText() != null ? actvRequestBloodGroup.getText().toString().trim() : "";
        String unitsRequired = etUnitsRequired.getText() != null ? etUnitsRequired.getText().toString().trim() : "";
        String hospital = etHospital.getText() != null ? etHospital.getText().toString().trim() : "";
        String phone = etRequestPhone.getText() != null ? etRequestPhone.getText().toString().trim() : "";

        boolean cancel = false;

        if (patientName.isEmpty()) {
            tilPatientName.setError("Patient name required");
            cancel = true;
        } else {
            tilPatientName.setError(null);
        }

        if (bloodGroup.isEmpty()) {
            tilRequestBloodGroup.setError("Select blood group");
            cancel = true;
        } else {
            tilRequestBloodGroup.setError(null);
        }

        if (unitsRequired.isEmpty()) {
            tilUnitsRequired.setError("Units required");
            cancel = true;
        } else {
            tilUnitsRequired.setError(null);
        }

        if (hospital.isEmpty()) {
            tilHospital.setError("Hospital name required");
            cancel = true;
        } else {
            tilHospital.setError(null);
        }

        if (phone.isEmpty() || phone.length() < 10) {
            tilPhoneError(phone);
            cancel = true;
        } else {
            tilRequestPhone.setError(null);
        }

        if (cancel) {
            return;
        }

        setLoading(true);

        executorService.execute(() -> {
            String result;
            try {
                URL url = new URL(REQUEST_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("patient_name", patientName);
                jsonParam.put("blood_group", bloodGroup);
                jsonParam.put("units_required", unitsRequired);
                jsonParam.put("hospital", hospital);
                jsonParam.put("phone", phone);

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
                handleRequestResponse(finalResult);
            });
        });
    }

    private void tilPhoneError(String phone) {
        if (phone.isEmpty()) {
            tilRequestPhone.setError("Phone number required");
        } else {
            tilRequestPhone.setError("Valid phone number required");
        }
    }

    private void setLoading(boolean loading) {
        if (loading) {
            progressRequestBlood.setVisibility(View.VISIBLE);
            btnSubmitRequest.setText("");
            btnSubmitRequest.setEnabled(false);
        } else {
            progressRequestBlood.setVisibility(View.GONE);
            btnSubmitRequest.setText(R.string.btn_submit_request);
            btnSubmitRequest.setEnabled(true);
        }
    }

    private void handleRequestResponse(String responseStr) {
        try {
            JSONObject jsonObject = new JSONObject(responseStr);
            String status = jsonObject.optString("status", "error");
            String message = jsonObject.optString("message", "Unknown error");

            if ("success".equals(status)) {
                Toast.makeText(this, "Blood Request Submitted Successfully!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            if (responseStr.contains("success") || responseStr.isEmpty()) {
                Toast.makeText(this, "Blood Request Submitted Successfully!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Invalid server response", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
