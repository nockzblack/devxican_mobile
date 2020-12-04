package com.itesm.devxican_mobile.data.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

public class User {

    public Integer role; // 0 - developer | 1 - recruiter | Integer.MAX_VALUE - admin
    public List<DocumentReference> posts,
                                   comments,
                                   follows,
                                   branches;
    public String email,
                  name,
                  phone,
                  photoURL;

    public User() {}

    public User(String email, String name, String phone, String photoURL,
                List<DocumentReference> posts, List<DocumentReference> follows, List<DocumentReference> branches,
                List<DocumentReference> comments) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.photoURL = photoURL;
        this.posts = posts;
        this.follows = follows;
        this.branches = branches;
        this.comments = comments;
        this.role = 0;

    }

    public User(String email, String name, String phone, String photoURL) {
        this(email, name, phone, photoURL,
             new ArrayList<DocumentReference>(), new ArrayList<DocumentReference>(), new ArrayList<DocumentReference>(),
             new ArrayList<DocumentReference>());
    }

}
