package com.example.devxican_mobile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogInActivity extends AppCompatActivity {

    private static  final String LOGIN_FILE = "login_prefences";
    private static final String TAG = "LogInActivity";
    private static final String EMAIL_PREFS = "email";
    private static final String PASS_PREFS = "password";

    private FirebaseAuth mAuth; // firebase object

    private Button signUp;
    private Button logIn;

    private EditText emailInput;
    private EditText passwordInput;


    private SharedPreferences prefs;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chargePrefs();

        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextPassword);

        signUp = findViewById(R.id.buttonSignUp);
        logIn = findViewById(R.id.buttonLogIn);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        emailInput.setText(readEmail());
        passwordInput.setText(readPassword());

    /*
        // [START create_user_with_email]

        // [END create_user_with_email]
    */

    }


    public void makeSignUp(View v) {
        String email = this.emailInput.getText().toString();
        String password =  this.passwordInput.getText().toString();

        if (validateEmailPassword(email,password)) {
            authServiceNewUserEmailPassword(email, password);
        }


    }

    public void makeLogIn(View v) {
        String email = this.emailInput.getText().toString();
        String password =  this.passwordInput.getText().toString();

        if (validateEmailPassword(email,password)) {
            authServiceLogInEmailPassword(email, password);
        }
    }

    private void authServiceNewUserEmailPassword(final String email, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveEmail(user.getEmail());
                            savePassword(password);

                            String msg = "Usuario con correo: " + user.getEmail() + " creado exitosamente";
                            Toast.makeText(LogInActivity.this, msg, Toast.LENGTH_SHORT).show();


                            // TODO: LAUNCH INTENT TO NEXT ACTIVITY WHEN NEW USER LOG IN



                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LogInActivity.this, "Error al crear usuario con correo: ." + email,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void authServiceLogInEmailPassword(final String email, final String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveEmail(user.getEmail());
                            savePassword(password);

                            Log.d(TAG, "signInWithEmail:Success");
                            String msg = "Usuario " + user.getEmail() + " ha iniciado sesion correctamente";
                            Toast.makeText(LogInActivity.this, msg, Toast.LENGTH_SHORT).show();


                            // TODO: LAUNCH INTENT TO NEXT ACTIVITY WHEN LOG IN

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LogInActivity.this, "Error al iniciar sesión.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    private boolean validateEmailPassword(String mail, String password) {
        return true;
    }

    private void chargePrefs() {
         prefs = getSharedPreferences(LOGIN_FILE, MODE_PRIVATE);
    }

    public void saveEmail(String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(EMAIL_PREFS, email);
        editor.commit();
    }

    public void savePassword(String password) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PASS_PREFS, password);
        editor.commit();
    }

    public String readEmail() {
        return prefs.getString(EMAIL_PREFS, "");
    }

    public String readPassword() {
        return prefs.getString(PASS_PREFS, "");
    }




}