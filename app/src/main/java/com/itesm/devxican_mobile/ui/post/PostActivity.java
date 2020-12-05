
package com.itesm.devxican_mobile.ui.post;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.itesm.devxican_mobile.R;
import com.itesm.devxican_mobile.data.model.Branch;
import com.itesm.devxican_mobile.data.model.Post;
import com.itesm.devxican_mobile.data.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostActivity extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    FirebaseUser curr_user;
    DocumentReference post_ref, branch_ref, user_ref;
    CollectionReference users_ref, comments_ref;
    Post post;
    Branch branch;
    User user;
    ArrayList<DocumentReference> comments;

    ImageView author_img;
    TextView post_title, post_author, post_branch, post_body, post_likes, post_com_num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Intent intent = getIntent();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        curr_user = mAuth.getCurrentUser();

        users_ref = db.collection("users");
        comments_ref = db.collection("comments");

        user_ref = users_ref.document(curr_user.getUid());
        post_ref = db.collection("posts").document(intent.getStringExtra("post_id"));
        post_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    post = task.getResult().toObject(Post.class);
                    branch_ref = post.branch;

                } else {
                    Log.wtf("ERROR", "Error reading post document.", task.getException());
                }
            }
        });

        author_img = findViewById(R.id.author_post_img);

        post_author = findViewById(R.id.post_author_name);
        post_title = findViewById(R.id.post_title);
        post_body = findViewById(R.id.post_body);
        post_branch = findViewById(R.id.post_branch);
        post_likes = findViewById(R.id.likes_post);
        post_com_num = findViewById(R.id.com_num_post);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(intent.getStringExtra("branch_name"));

        FloatingActionButton new_comment = (FloatingActionButton) findViewById(R.id.new_comment);
        new_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // public a new comment
            }
        });

        post_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    post = task.getResult().toObject(Post.class);

                    comments = new ArrayList<>(post.comments);
                    //constructRecycler();

                    post_body.setText(post.body);
                    post_title.setText(post.title);
                    post_likes.setText(String.valueOf(post.likes.size()));
                    post_com_num.setText(String.valueOf(post.comments.size()));
                    branch_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                branch = task.getResult().toObject(Branch.class);
                                post_branch.setText(branch.name);
                            }
                        }
                    });

                    post.author.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            user = task.getResult().toObject(User.class);
                            post_author.setText(user.name);
                            Picasso.get()
                                    .load(user.photoURL)
                                    .into(author_img);
                        }
                    });

                } else {
                    Log.wtf("ERROR", "Error reading post document.", task.getException());
                }
            }
        });




    }

/*    private void constructRecycler() {
        rv_comments.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        CommentAdapter adapter = new CommentAdapter(this.getApplicationContext(), this.comments, post_ref);
        rv_comments.setAdapter(adapter);
    }*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        this.finish();
        return true;
    }
}