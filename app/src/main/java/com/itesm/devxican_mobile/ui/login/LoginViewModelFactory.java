package com.itesm.devxican_mobile.ui.login;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class LoginViewModelFactory implements ViewModelProvider.Factory {

    LoginActivity activity;

    public LoginViewModelFactory(LoginActivity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(FirebaseAuth.getInstance(), activity);
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}