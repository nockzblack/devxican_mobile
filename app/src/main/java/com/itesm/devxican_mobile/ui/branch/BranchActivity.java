package com.itesm.devxican_mobile.ui.branch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.itesm.devxican_mobile.R;
import com.itesm.devxican_mobile.data.model.Branch;
import com.itesm.devxican_mobile.data.model.User;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toolbar;

import java.util.ArrayList;

public class BranchActivity extends AppCompatActivity {


    RecyclerView rv_posts;
    FirebaseAuth mAuth;
    FirebaseUser curr_user;
    FirebaseFirestore db;
    CollectionReference branches_ref, users_ref;
    DocumentReference branch_ref, user_ref;
    Branch branch;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        curr_user = mAuth.getCurrentUser();
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
        branches_ref = db.collection("branches");
        branches_ref.whereEqualTo("name", intent.getStringExtra("name")).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    branch_ref = task.getResult().getDocuments().get(0).getReference();
                    branch_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            branch = task.getResult().toObject(Branch.class);
                            actionBar.setTitle("r/".concat(branch.name));
                        }
                    });
                } else {
                    Log.wtf("ERROR", "Error reading branches documents.", task.getException());
                }
            }
        });


        users_ref = db.collection("users");
        user_ref = users_ref.document(curr_user.getUid());
        user_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    user = task.getResult().toObject(User.class);
                } else {
                    Log.wtf("ERROR", "Error reading user document.", task.getException());
                }
            }
        });
        rv_posts = findViewById(R.id.rv_posts);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            this.finish();
            return true;
    }
}