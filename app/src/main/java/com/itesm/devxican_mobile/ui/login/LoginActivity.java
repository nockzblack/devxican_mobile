package com.itesm.devxican_mobile.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
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

import com.itesm.devxican_mobile.HomeActivity;
import com.itesm.devxican_mobile.R;
import com.itesm.devxican_mobile.ui.login.LoginViewModel;
import com.itesm.devxican_mobile.ui.login.LoginViewModelFactory;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    public static final String USER_TAG = "LoggedInUser";

    private LoginViewModel loginViewModel;

    private EditText usernameEditText;
    private EditText passwordEditText;

    private Button loginButton;
    private Button registerButton;

    private ProgressBar loadingProgressBar;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout
        setContentView(R.layout.activity_login);


        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory(this))
                .get(LoginViewModel.class);

        // Fields
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);

        // Buttons
        loginButton = findViewById(R.id.login);
        registerButton = findViewById(R.id.register);

        // Widget
        ProgressBar loadingProgressBar = findViewById(R.id.loading);

        // TextWatcher do changes when is called
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        // Add TextWatcher as listener
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        // Add TextWatcher as listener
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        // listens the enter on keyboard
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.registerAndLogin(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        // listens the sign In button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                //Log.wtf(TAG, "before call medthod login from view model");
                loginViewModel.signIn(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });

        // listens the register button

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.registerAndLogin(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });



        // listens the form and updates it
        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                registerButton.setEnabled(loginFormState.isDataValid());

                if (loginFormState.getUsernameError() != null) {
                    String strError = getString(loginFormState.getUsernameError());
                    usernameEditText.setError(strError);
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });


        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }

                // Hide loading Bar
                loadingProgressBar.setVisibility(View.GONE);


                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                    return;
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser();

                    setResult(Activity.RESULT_OK); // activity life cycle method

                    //Complete and destroy login activity once successful
                    finish(); // activity life cycle method
                }

            }
        });

    }

    private void updateUiWithUser() {
        Intent auxIntent = new Intent(this, HomeActivity.class);
        auxIntent.putExtra(USER_TAG, this.loginViewModel.getUser());
        startActivity(auxIntent);
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
        this.passwordEditText.getText().clear();
    }
}