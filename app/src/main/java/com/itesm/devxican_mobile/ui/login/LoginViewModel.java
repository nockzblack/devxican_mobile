package com.itesm.devxican_mobile.ui.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itesm.devxican_mobile.data.model.Result;
import com.itesm.devxican_mobile.data.model.User;
import com.itesm.devxican_mobile.data.model.LoggedInUser;
import com.itesm.devxican_mobile.R;

import java.util.concurrent.Executor;

public class LoginViewModel extends ViewModel {

    private static final String TAG = "LoginViewModel";

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference users_ref;
    private LoggedInUser user;
    private LoginActivity activity;


    LoginViewModel(FirebaseAuth mAuth, LoginActivity activity) {
        this.mAuth = mAuth;
        this.activity = activity;

        this.db = FirebaseFirestore.getInstance();
        this.users_ref = db.collection("users");

    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }



    public void registerAndLogin(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.wtf(TAG, "createUserWithEmail:successful");
                            user =  new LoggedInUser(mAuth.getCurrentUser());

                            User userdata = new User(email, "Anonymous", "n/a", "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/users%2Fdeveloper.png?alt=media&token=363ffb24-d105-46e3-b2a9-481666fba6a8");
                            Result<LoggedInUser> result = new Result.Success<>(user);

                            if (users_ref.document(task.getResult().getUser().getUid()).set(userdata).isSuccessful()) {
                                Log.i("INFO", "USUARIO REGISTRADO");
                            }


                            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                            loginResult.setValue(new LoginResult(data));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.wtf(TAG, e.getClass().toString());

                if (e instanceof FirebaseAuthUserCollisionException) {
                    loginResult.setValue(new LoginResult(R.string.collision_email));
                } else {
                    loginResult.setValue(new LoginResult(R.string.login_failed));
                }
            }
        });
    }


    public void signIn(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.wtf(TAG, "createUserWithEmail:successful");

                            user =  new LoggedInUser(mAuth.getCurrentUser());

                            Result<LoggedInUser> result = new Result.Success<>(user);

                            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                            loginResult.setValue(new LoginResult(data));
                        } else {
                            loginResult.setValue(new LoginResult(R.string.login_failed));
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    loginResult.setValue(new LoginResult(R.string.wrong_password));
                } else if (e instanceof FirebaseAuthInvalidUserException) {
                    loginResult.setValue(new LoginResult(R.string.wrong_email));
                } else {
                    loginResult.setValue(new LoginResult(R.string.login_failed));
                }
            }
        });
    }



    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_email, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return false;
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public LoggedInUser getUser() {
        return user;
    }
}
