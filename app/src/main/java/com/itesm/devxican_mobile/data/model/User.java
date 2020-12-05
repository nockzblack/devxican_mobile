package com.itesm.devxican_mobile.data.model;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class User {

    public static final String TAG = "ProfileSettingsActivity";
    public static MutableLiveData<User> auxUser = new MutableLiveData<>();;
    public static MutableLiveData<User> getAuxUser() {return auxUser;}

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

    public static void LOAD_USER(String uid) {
        User newUser = new User();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("users").document(uid);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.wtf(TAG, "DocumentSnapshot data: " + document.getData());

                        newUser.name = document.getData().get("name").toString();
                        newUser.photoURL = document.getData().get("photoURL").toString();
                        newUser.phone = document.getData().get("phone").toString();
                        newUser.email = document.getData().get("email").toString();

                        newUser.branches = (List<DocumentReference>) document.getData().get("branches");
                        newUser.comments = (List<DocumentReference>) document.getData().get("comments");
                        newUser.follows = (List<DocumentReference>) document.getData().get("follows");
                        newUser.posts = (List<DocumentReference>) document.getData().get("posts");

                        //Log.wtf(TAG, "Name: " + newUser.name);

                        User.auxUser.setValue(newUser);

                    } else {
                        Log.wtf(TAG, "No such document");
                    }
                } else {
                    Log.wtf(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

}
