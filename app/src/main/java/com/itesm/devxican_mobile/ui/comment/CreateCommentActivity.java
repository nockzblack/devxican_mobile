package com.itesm.devxican_mobile.ui.comment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

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

public class CreateCommentActivity extends AppCompatActivity {

    EditText et_body;
    Button createCom;
    DocumentReference post_ref, user_ref, comment_ref;
    FirebaseAuth mAuth;
    FirebaseUser curr_user;
    User user;
    FirebaseFirestore db;
    CollectionReference posts_ref, users_ref, comments_ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_comment);

        Intent intent = getIntent();

        mAuth = FirebaseAuth.getInstance();
        curr_user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        posts_ref = db.collection("posts");
        users_ref = db.collection("users");
        comments_ref = db.collection("comments");

        user_ref = users_ref.document(curr_user.getUid());
        post_ref = posts_ref.document(intent.getStringExtra("post_id"));


        et_body = findViewById(R.id.body_com);
;
        createCom = findViewById(R.id.pubilsh_com);

        createCom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Comment com = new Comment();
                com.author = user_ref;
                com.post = post_ref;
                com.body = et_body.getText().toString();
                com.likes = new ArrayList<>();
                com.dislikes = new ArrayList<>();
                com.show = true;
                com.time = Timestamp.now();

                comments_ref.add(com).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            comment_ref = task.getResult();
                            comment_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        User user = task.getResult().toObject(User.class);
                                        user.comments.add(comment_ref);
                                        user_ref.update("comments", user.comments).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.i("SUCCESS", "Post added to user");
                                                    post_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                Post post = task.getResult().toObject(Post.class);
                                                                post.comments.add(comment_ref);
                                                                post_ref.update("posts", post.comments).addOnCompleteListener(new OnCompleteListener<Void>() {
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