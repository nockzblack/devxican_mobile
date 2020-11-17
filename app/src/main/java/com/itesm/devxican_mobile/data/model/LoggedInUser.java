package com.itesm.devxican_mobile.data.model;

import com.google.firebase.auth.FirebaseUser;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userId;
    private String displayName;
    private FirebaseUser user;

    public LoggedInUser(FirebaseUser user) {
        this.user = user;
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