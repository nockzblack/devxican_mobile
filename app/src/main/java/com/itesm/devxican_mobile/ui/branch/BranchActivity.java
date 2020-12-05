package com.itesm.devxican_mobile.ui.branch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.itesm.devxican_mobile.R;
import com.itesm.devxican_mobile.data.model.Branch;

import android.os.Bundle;

import java.util.ArrayList;

public class BranchActivity extends AppCompatActivity {


    RecyclerView rv_posts;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseFirestore db;
    CollectionReference branches_ref;
    DocumentReference branch_ref, user_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        branches_ref = db.collection("branches");
        user = mAuth.getCurrentUser();



        rv_posts = findViewById(R.id.rv_posts);
    }

}