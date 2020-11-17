package com.itesm.devxican_mobile.data.model;

import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser implements Serializable {

    private String userId;
    private String displayName;

    public LoggedInUser(FirebaseUser user) {
        this.userId = user.getUid();
        this.displayName = user.getEmail();
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}