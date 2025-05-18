package com.example.eskuvobolt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class CreatorActivity extends AppCompatActivity {
     @Override
    protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_creator);
     }

    public void goBackToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        // Optional: ha nem akarod, hogy visszamenjen ide a "Back" gombbal
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Bezárja ezt az Activity-t
    }

}
