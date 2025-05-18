package com.example.eskuvobolt;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final String PREF_KEY = MainActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 99;
    EditText userNameET;
    EditText userEmailET;
    EditText userPwdET;
    EditText userPwdConfirmET;
    EditText userAddressET;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        Bundle bundle = getIntent().getExtras();
//        int secret_key = bundle.getInt("SECRET_KEY");
        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != 99) {
            finish();
        }

        userNameET = findViewById(R.id.user);
        userEmailET = findViewById(R.id.userEmail);
        userPwdET = findViewById(R.id.userPassword);
        userPwdConfirmET = findViewById(R.id.userPassword2);
        userAddressET = findViewById(R.id.userAddress);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        String userName = preferences.getString("userName", "");
        String password = preferences.getString("password", "");

        userNameET.setText(userName);
        userPwdET.setText(password);
        userPwdConfirmET.setText(password);

        mAuth = FirebaseAuth.getInstance();


        Log.i(LOG_TAG, "onCreate");
    }

    public void register(View view) {
        // TODO.

        // szöveg lekérdezés
        String userName = userNameET.getText().toString();
        String userMail = userEmailET.getText().toString();
        String pwd = userPwdET.getText().toString();
        String pwdConfirm = userPwdConfirmET.getText().toString();
        String address = userAddressET.getText().toString();

        // Jelszó ellenőrzés
        if (!pwd.equals(pwdConfirm)) {
            Log.e(LOG_TAG, "A jelszavak nem egyeznek.");
            return;
        }

        // logba írás
        Log.i(LOG_TAG, "[+] NEW_USER_REGISTER:\nNév: " + userName +  "\nEmail: " + userMail + "\nJelszó: " + pwd + "\nCím: " + address);

        //startShopping();

        mAuth.createUserWithEmailAndPassword(userMail, pwd).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "[+] USER_CREATED_SUCCESSFULLY");
                    startShopping();
                } else {
                    Log.d(LOG_TAG, "[!] USER_CREATION_FAILURE");
                    Toast.makeText(RegisterActivity.this, "[!] USER_CREATION_FAILURE" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startShopping() {
        // Felhasználók adatai

        Intent intent = new Intent(this, ShopListActivity.class);
        //intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }

    public void cancel(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }
}