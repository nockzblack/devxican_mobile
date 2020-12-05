
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
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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
import com.itesm.devxican_mobile.ui.comment.CommentAdapter;
import com.itesm.devxican_mobile.ui.comment.CreateCommentActivity;
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
    RecyclerView rv_comments;
    ImageButton ib;
    ImageView author_img, bt_like, bt_dislike;
    TextView post_title, post_author, post_branch, post_body, post_likes, post_com_num;
    FloatingActionButton creCom;
    public boolean isAdmin = false, haslike = false, hasdislike = false; // constantly changing variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Intent intent = getIntent();

        rv_comments = findViewById(R.id.rv_comments);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        curr_user = mAuth.getCurrentUser();
        creCom = findViewById(R.id.new_comment);





        creCom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intNew = new Intent(getApplicationContext(), CreateCommentActivity.class);
                String id = post_ref.getId();
                intNew.putExtra("post_id", id);

                startActivity(intNew);
            }
        });

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
                    if (post.likes.contains(user_ref)) { // has like on post;
                        bt_like.setEnabled(false);
                        bt_like.setVisibility(View.INVISIBLE);
                        bt_dislike.setEnabled(true);
                        bt_dislike.setVisibility(View.VISIBLE);

                        haslike = true;
                    } else if (post.dislikes.contains(user_ref)) { // has dislike
                        bt_like.setEnabled(true);
                        bt_like.setVisibility(View.VISIBLE);
                        bt_dislike.setEnabled(false);
                        bt_dislike.setVisibility(View.INVISIBLE);
                        hasdislike = true;
                    }

                } else {
                    Log.wtf("ERROR", "Error reading post document.", task.getException());
                }
            }
        });

        author_img = findViewById(R.id.author_post_img);
        bt_like = findViewById(R.id.bt_like_post);
        bt_dislike = findViewById(R.id.bt_disike_post);



        bt_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!post.likes.contains(user_ref)) { // user doesnt havea like
                    if (hasdislike) { // remove dislike
                        post.dislikes.remove(user_ref);
                        post_ref.update("dislikes", post.dislikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    // restore dislike state
                                    hasdislike = false;
                                    bt_dislike.setEnabled(true);
                                    bt_dislike.setVisibility(View.VISIBLE);

                                    Log.i("INFO", "Successfully removed the dislike at the post");
                                } else {
                                    Log.wtf("ERROR", "Error updating post.", task.getException());
                                }
                            }
                        });
                    }
                    post.likes.add(user_ref);
                    post_ref.update("likes", post.likes).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // update like state
                                haslike = true;
                                bt_like.setEnabled(false);
                                bt_like.setVisibility(View.INVISIBLE);

                                post_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Post post = task.getResult().toObject(Post.class);
                                            post_likes.setText(String.valueOf(post.likes.size() - post.dislikes.size()));
                                        } else {
                                            Log.wtf("ERROR", "Error reading post document.", task.getException());
                                        }
                                    }
                                });

                                Log.i("INFO", "Successfully liked the post");
                            } else {
                                Log.wtf("ERROR", "Error updating post.", task.getException());
                            }
                        }
                    });

                } else {
                    Log.w("WARNING", "Already have a like on this post!");
                }
            }
        });

        bt_dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!post.dislikes.contains(user_ref)) { // user doesnt have a dislike
                    if (haslike) { // remove like
                        post.likes.remove(user_ref);
                        post_ref.update("likes", post.likes).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // restore like state to default
                                    haslike = false;
                                    bt_like.setEnabled(true);
                                    bt_like.setVisibility(View.VISIBLE);

                                    Log.i("INFO", "Successfully removed like from the post");
                                } else {
                                    Log.wtf("ERROR", "Error removing like from post.", task.getException());
                                }
                            }
                        });
                    }
                    post.dislikes.add(user_ref); // adding dislike
                    post_ref.update("dislikes", post.dislikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                hasdislike = true;
                                bt_dislike.setEnabled(false);
                                bt_dislike.setVisibility(View.INVISIBLE);

                                post_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Post post = task.getResult().toObject(Post.class);

                                            post_likes.setText(String.valueOf(post.likes.size() - post.dislikes.size()));
                                        } else {
                                            Log.wtf("ERROR", "Error reading post document.", task.getException());
                                        }
                                    }
                                });

                                Log.i("INFO", "Successfully disliked the post");
                            } else {
                                Log.wtf("ERROR", "Error updating post.", task.getException());
                            }
                        }
                    });

                } else {
                    Log.w("WARNING", "Already have a like on this post!");
                }
            }
        });

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

                    constructRecycler();

                    bt_like.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });


                    post_body.setText(post.body);
                    post_title.setText(post.title);
                    post_likes.setText(String.valueOf(post.likes.size() - post.dislikes.size()));
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

    private void constructRecycler() {
        rv_comments.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        CommentAdapter adapter = new CommentAdapter(this.getApplicationContext(), this.comments, post_ref);
        rv_comments.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        this.finish();
        return true;
    }
}