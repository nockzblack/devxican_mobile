package com.itesm.devxican_mobile.ui.branch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavType;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.itesm.devxican_mobile.ui.post.CreatePostActivity;
import com.itesm.devxican_mobile.ui.post.PostActivity;
import com.itesm.devxican_mobile.ui.post.PostAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

public class BranchActivity extends AppCompatActivity {


    RecyclerView rv_posts;
    FirebaseAuth mAuth;
    FirebaseUser curr_user;
    FirebaseFirestore db;
    CollectionReference branches_ref, users_ref;
    DocumentReference branch_ref, user_ref;
    ArrayList<DocumentReference> posts;
    Branch branch;
    User user;
    Button bt_new_post;

    public void createPost() {
        Toast.makeText(getApplicationContext(), "CREANDO NUEVO POST", Toast.LENGTH_LONG).show();
        Intent intNew = new Intent(BranchActivity.this, CreatePostActivity.class);
        intNew.putExtra("branch_id", branch_ref.getId());
        //intNew.putExtra("branch_id", "oidnfsandfWEFEW123");

        //finish();
        BranchActivity.this.startActivity(intNew);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        Random random = new Random(System.currentTimeMillis());
        Picasso.get()
                .load(background_urls[random.nextInt(background_urls.length)])
                .into((ImageView) findViewById(R.id.branch_back));
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        curr_user = mAuth.getCurrentUser();
        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        bt_new_post = findViewById(R.id.new_post);

        bt_new_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPost();
            }
        });

        users_ref = db.collection("users");
        user_ref = users_ref.document(curr_user.getUid());
        rv_posts = findViewById(R.id.rv_posts);
        branches_ref = db.collection("branches");
        branch_ref = branches_ref.document(intent.getStringExtra("branch_id"));
        branch_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    branch = task.getResult().toObject(Branch.class);
                    posts = new ArrayList<>(branch.posts);
                    constructRecycler();
                } else {
                    Log.wtf("ERROR", "Error reading branches documents.", task.getException());
                }
            }
        });

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



    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
            this.finish();
            return true;
    }


    private void constructRecycler() {


        rv_posts.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        PostAdapter adapter = new PostAdapter(this.getApplicationContext(), this.posts, branch_ref);
        rv_posts.setAdapter(adapter);

        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intPost = new Intent(getApplicationContext(), PostActivity.class);
                intPost.putExtra("post_id", posts.get(rv_posts.getChildAdapterPosition(view)).getId());
                intPost.putExtra("branch_name", branch.name);
                startActivity(intPost);
            }
        });
    }


    private static final String[] background_urls = {
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F1.jpg?alt=media&token=63dbb25e-a1c0-4d6a-a9c0-72b028e3bea7",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F10.jpg?alt=media&token=8746001d-8e2d-4bd5-bab2-c64c0f413d9c",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F11.jpg?alt=media&token=b872a75c-e306-4f59-9d92-1434f21d779d",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F12.jpg?alt=media&token=d3a1a659-f6ba-4465-a2f7-988c67a317a3",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F13.jpg?alt=media&token=350e54b5-668e-4b81-a36a-bc186320fbfa",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F14.jpg?alt=media&token=973ea0d7-0ebf-4d25-9ee4-7e5e30d3f510",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F15.jpg?alt=media&token=04d8485e-a10b-4af7-b5d9-91c7190216e6",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F16.jpg?alt=media&token=3f1664e6-5a98-4338-aa1b-25e49f5e7f12",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F17.jpg?alt=media&token=3d750f6f-605f-48e3-bf93-da5f91291642",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F18.jpg?alt=media&token=cf2409d0-fd0c-4255-adb7-b56297572f8f",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F19.jpg?alt=media&token=50b0f7f4-7de7-41a5-9573-d089c57f4547",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F2.jpg?alt=media&token=0b5c37cb-6a4e-4352-ad8a-ff69aff8ca0e",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F20.jpg?alt=media&token=8b485545-5743-444d-9e68-9ffd9d3e5761",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F21.jpg?alt=media&token=882cc703-869e-4188-a753-0dbbad2b5797",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F22.jpg?alt=media&token=ce99ea20-1810-4ee5-a94f-7da3bd38d851",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F23.jpg?alt=media&token=0c59471d-6093-4d2d-aa36-6899f71127ea",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F24.jpg?alt=media&token=fc950a3a-f04b-44fa-bc36-e4a57ad022cb",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F25.jpg?alt=media&token=cf91ed10-4dc6-4d9b-91c6-6a7560462661",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F26.jpg?alt=media&token=5f19749a-ef1a-48fe-a8b1-95934cfb2cde",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F27.jpg?alt=media&token=d19d1c26-5fe3-4171-a1a1-2f1d66f971ce",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F28.jpg?alt=media&token=27565429-b750-462a-8e21-e28bfb8c1d92",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F29.jpg?alt=media&token=d9f50b13-5b7e-4af4-a724-8c2ec5062524",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F3.jpg?alt=media&token=053f7517-6632-4d93-9798-48979d4e47dc",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F30.jpg?alt=media&token=673a3b45-136e-48e3-9109-1b1c23ef436b",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F31.jpg?alt=media&token=ed2e82bf-2fa6-4f33-a669-7091302f5aaa",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F33.jpg?alt=media&token=f6b5b761-6164-469d-91a9-fcbc52407580",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F34.jpg?alt=media&token=b36c91aa-804a-4eb8-b7fc-89d153fb6c9b",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F35.jpg?alt=media&token=18a56d2e-34e7-494f-9106-52e4a107ae4c",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F36.jpg?alt=media&token=cc3479d0-66cd-44e7-b1ce-23b58416eafa",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F37.jpg?alt=media&token=b6fd888f-aced-4b7d-86a8-30fe42e06b6b",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F38.jpg?alt=media&token=27de52d4-0331-4dcb-ac4f-ad4a8d08e239",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F39.jpg?alt=media&token=8f8b2e31-0365-4e9d-9c09-220be1014c89",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F4.jpg?alt=media&token=fd70e183-cd9e-479d-bb27-f913723d6733",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F40.jpg?alt=media&token=d42af5ed-44dd-454c-abaf-90bd77c549b6",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F5.jpg?alt=media&token=5d97e24d-1ce2-4cb7-9e52-c296c68520a2",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F6.jpg?alt=media&token=ab9e9ede-c185-411b-9513-8bfbaadb7340",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F7.jpg?alt=media&token=7d02fb85-e132-416e-a1a5-12997f1f6cd2",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F8.jpg?alt=media&token=05dfb8f9-caaa-43e1-8413-45b08dda3946",
            "https://firebasestorage.googleapis.com/v0/b/devxicanmobile.appspot.com/o/branches%2Fbackgrounds%2F9.jpg?alt=media&token=5acc3f72-91f9-4bbb-9279-768e1f30c918"
    };
}