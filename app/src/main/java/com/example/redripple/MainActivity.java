package com.example.redripple;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.redripple.ui.request.AllRequestsActivity;
import com.example.redripple.ui.request.RequestBloodActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialButton btnHeroRequest = findViewById(R.id.btnHeroRequest);
        MaterialCardView cardRequest = findViewById(R.id.cardRequest);
        View tvSeeAll = findViewById(R.id.tvSeeAll);

        if (btnHeroRequest != null) {
            btnHeroRequest.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RequestBloodActivity.class)));
        }

        if (cardRequest != null) {
            cardRequest.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RequestBloodActivity.class)));
        }

        if (tvSeeAll != null) {
            tvSeeAll.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AllRequestsActivity.class)));
        }
    }
}
