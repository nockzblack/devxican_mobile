package com.itesm.devxican_mobile.ui.post;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itesm.devxican_mobile.R;
import com.itesm.devxican_mobile.data.model.Branch;
import com.itesm.devxican_mobile.data.model.Comment;
import com.itesm.devxican_mobile.data.model.Post;
import com.itesm.devxican_mobile.data.model.User;

import java.util.ArrayList;

public class CreatePostActivity extends AppCompatActivity {

    EditText et_title, et_body;
    ImageButton imagePost;
    Button createPost;
    DocumentReference branch_ref, user_ref, post_ref;
    FirebaseAuth mAuth;
    FirebaseUser curr_user;
    User user;
    FirebaseFirestore db;
    CollectionReference branches_ref, users_ref, posts_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        Intent intent = getIntent();

        mAuth = FirebaseAuth.getInstance();
        curr_user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        branches_ref = db.collection("branches");
        users_ref = db.collection("users");
        posts_ref = db.collection("posts");

        user_ref = users_ref.document(curr_user.getUid());
        branch_ref = branches_ref.document(intent.getStringExtra("branch_id"));


        et_title = findViewById(R.id.title_post);
        et_body = findViewById(R.id.body_post);

        imagePost = findViewById(R.id.postImage);

        createPost = findViewById(R.id.pubilsh_post);

        createPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Post post = new Post();
                post.author = user_ref;
                post.branch = branch_ref;
                post.title = et_title.getText().toString();
                post.body = et_body.getText().toString();
                post.likes = new ArrayList<>();
                post.dislikes = new ArrayList<>();
                post.comments = new ArrayList<>();
                post.show = true;
                post.photoURL = new ArrayList<>();
                post.time = Timestamp.now();

                posts_ref.add(post).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            post_ref = task.getResult();
                            user_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        User user = task.getResult().toObject(User.class);
                                        user.posts.add(post_ref);
                                        user_ref.update("posts", user.posts).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.i("SUCCESS", "Post added to user");
                                                    branch_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                Branch branch = task.getResult().toObject(Branch.class);
                                                                branch.posts.add(post_ref);
                                                                branch_ref.update("posts", branch.posts).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            Log.i("SUCCESS", "Branch posts updated");
                                                                        } else {
                                                                            Log.wtf("ERROR", "Error updating branch posts.", task.getException());
                                                                        }
                                                                    }
                                                                });
                                                            } else {
                                                                Log.wtf("ERROR", "Error reading branch document.", task.getException());
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Log.wtf("ERROR", "Error updating user posts.", task.getException());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.wtf("ERROR", "Error reading user document.", task.getException());
                                    }
                                }
                            });
                        } else {
                            Log.i("ERROR", "Error adding post to the database.", task.getException());
                        }
                    }
                });

            }
        });

        finish();

    }
}