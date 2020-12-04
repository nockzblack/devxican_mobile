package com.itesm.devxican_mobile.ui.login;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itesm.devxican_mobile.HomeActivity;
import com.itesm.devxican_mobile.R;
import com.itesm.devxican_mobile.data.model.User;
import com.itesm.devxican_mobile.ui.login.LoginViewModel;
import com.itesm.devxican_mobile.ui.login.LoginViewModelFactory;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    public static final String USER_TAG = "user";

    private static final String LOGIN_FILE = "login_prefences";
    private static final String EMAIL_PREFS = "email";
    private static final String PASS_PREFS = "password";

    private SharedPreferences prefs;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference users_ref;

    public EditText et_email, et_password;
    public Button loginButton, signinButton;
    public ProgressBar loadingProgressBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        users_ref = db.collection("users");


        // Fields
        et_email = findViewById(R.id.username);
        et_password = findViewById(R.id.password);
        // Buttons
        loginButton = findViewById(R.id.login);
        signinButton = findViewById(R.id.signin);

        // Widget
        loadingProgressBar = findViewById(R.id.loading);

        prefs = getSharedPreferences(LOGIN_FILE, MODE_PRIVATE);
        et_email.setText(readEmail());
        et_password.setText(readPassword());
        loginButton.setEnabled(et_password.getText().toString().length() >= 6 && !et_email.getText().toString().trim().equals(""));




        // TextWatcher do changes when is called
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                loginButton.setEnabled(et_password.getText().toString().length() >= 6 && !et_email.getText().toString().trim().equals(""));
            }
        };

        et_password.addTextChangedListener(afterTextChangedListener);
        et_email.addTextChangedListener(afterTextChangedListener);

        // listens the enter on keyboard
        et_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {

                    login(et_email.getText().toString(),
                            et_password.getText().toString());
                }
                return false;
            }
        });

        // listens the button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login(et_email.getText().toString(),
                        et_password.getText().toString());
            }
        });

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewUser(et_email.getText().toString(), et_password.getText().toString());
            }
        });
    }

    private void login(String email, String password) {

        loadingProgressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) { // Logged in
                    loadingProgressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Successfully logged in as: ".concat(email), Toast.LENGTH_LONG).show();
                    updateUiWithUser();
                }
            }
        });
    }

    private void createNewUser(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    User user = new User(email, "Anonymous", "n/a", "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/users%2Fdeveloper.png?alt=media&token=363ffb24-d105-46e3-b2a9-481666fba6a8");
                    users_ref.document().set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            loadingProgressBar.setVisibility(View.GONE);
                            Log.d(TAG, "User created: ".concat(email));
                            Toast.makeText(LoginActivity.this, "Successfully created new user: ".concat(email), Toast.LENGTH_LONG).show();
                            updateUiWithUser();
                        }
                    });
                } else {
                    Log.wtf(TAG, "Couldn't create a new user: ", task.getException());
                }
            }
        });

    }

    private void updateUiWithUser() {
        Intent auxIntent = new Intent(this, HomeActivity.class);
        saveEmail(et_email.getText().toString());
        savePassword(et_password.getText().toString());
        auxIntent.putExtra(USER_TAG, this.mAuth.getCurrentUser());
        startActivity(auxIntent);
    }

    public void saveEmail(String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(EMAIL_PREFS, email);
        editor.apply();
    }

    public void savePassword(String password) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PASS_PREFS, password);
        editor.apply();
    }

    public String readEmail() {
        return prefs.getString(EMAIL_PREFS, "");
    }

    public String readPassword() {
        return prefs.getString(PASS_PREFS, "");
    }

}