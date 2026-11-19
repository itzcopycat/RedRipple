package com.example.redripple.ui.request;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.redripple.R;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AllRequestsActivity extends AppCompatActivity {

    private RecyclerView rvRequests;
    private ProgressBar progressAllRequests;
    private RequestAdapter adapter;
    private final List<BloodRequestModel> requestList = new ArrayList<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String GET_REQUESTS_URL = "http://10.0.2.2/redripple/get_requests.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_requests);

        rvRequests = findViewById(R.id.rvRequests);
        progressAllRequests = findViewById(R.id.progressAllRequests);

        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestAdapter(requestList);
        rvRequests.setAdapter(adapter);

        fetchRequests();
    }

    private void fetchRequests() {
        progressAllRequests.setVisibility(View.VISIBLE);

        executorService.execute(() -> {
            String result = "";
            try {
                URL url = new URL(GET_REQUESTS_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

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
                }
            } catch (Exception e) {
                result = "";
            }

            final String finalResult = result;
            runOnUiThread(() -> {
                progressAllRequests.setVisibility(View.GONE);
                parseRequests(finalResult);
            });
        });
    }

    private void parseRequests(String jsonStr) {
        requestList.clear();
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String bloodGroup = obj.optString("blood_group", "O+");
                String hospital = obj.optString("hospital", "Hospital");
                String patientName = obj.optString("patient_name", "Patient");
                String units = obj.optString("units_required", "1");
                String phone = obj.optString("phone", "0000000000");

                requestList.add(new BloodRequestModel(bloodGroup, hospital, patientName, units, phone));
            }
        } catch (JSONException e) {
            // Fallback mock items if server isn't running yet
            requestList.add(new BloodRequestModel("A+", "City General Hospital", "John Doe", "2", "9876543210"));
            requestList.add(new BloodRequestModel("B-", "St. Mary Medical Center", "Jane Smith", "1", "9123456789"));
            requestList.add(new BloodRequestModel("O+", "Metro Health Clinic", "Robert Johnson", "3", "9988776655"));
        }
        adapter.notifyDataSetChanged();
    }

    // Model class
    public static class BloodRequestModel {
        String bloodGroup, hospital, patientName, units, phone;

        public BloodRequestModel(String bloodGroup, String hospital, String patientName, String units, String phone) {
            this.bloodGroup = bloodGroup;
            this.hospital = hospital;
            this.patientName = patientName;
            this.units = units;
            this.phone = phone;
        }
    }

    // RecyclerView Adapter
    private class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
        private final List<BloodRequestModel> items;

        public RequestAdapter(List<BloodRequestModel> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_request, parent, false);
            return new RequestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
            BloodRequestModel model = items.get(position);
            holder.tvBadge.setText(model.bloodGroup);
            holder.tvHospital.setText(model.hospital);
            holder.tvMeta.setText(model.units + " units needed • Patient: " + model.patientName);
            holder.tvTime.setText("Contact: " + model.phone);
            holder.btnRespond.setOnClickListener(v -> Toast.makeText(AllRequestsActivity.this, "Responded to request for " + model.patientName, Toast.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class RequestViewHolder extends RecyclerView.ViewHolder {
            TextView tvBadge, tvHospital, tvMeta, tvTime;
            MaterialButton btnRespond;

            public RequestViewHolder(@NonNull View itemView) {
                super(itemView);
                tvBadge = itemView.findViewById(R.id.tvItemBadge);
                tvHospital = itemView.findViewById(R.id.tvItemHospital);
                tvMeta = itemView.findViewById(R.id.tvItemMeta);
                tvTime = itemView.findViewById(R.id.tvItemTime);
                btnRespond = itemView.findViewById(R.id.btnItemRespond);
            }
        }
    }
}
