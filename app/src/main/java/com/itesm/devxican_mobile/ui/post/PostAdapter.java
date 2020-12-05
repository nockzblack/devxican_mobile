package com.itesm.devxican_mobile.ui.post;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.google.firebase.storage.StorageReference;
import com.itesm.devxican_mobile.R;
import com.itesm.devxican_mobile.data.model.Branch;
import com.itesm.devxican_mobile.data.model.Post;
import com.itesm.devxican_mobile.data.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> implements View.OnClickListener {

    public StorageReference mStorageRef;
    public FirebaseFirestore db;
    public FirebaseAuth mAuth;
    public CollectionReference posts_ref, users_ref;
    public FirebaseUser curr_user;
    public DocumentReference user_ref, branch_ref;
    public User user;
    public Branch branch;
    public String branch_name;

    public View.OnClickListener listener;
    public Context context;
    public ArrayList<DocumentReference> posts;
    public boolean isAdmin = false, haslike = false, hasdislike = false; // constantly changing variables


    public PostAdapter(Context context, ArrayList<DocumentReference> posts, DocumentReference branch_ref) {

        this.context = context;
        this.posts = posts;
        this.branch_ref = branch_ref;

        mAuth = FirebaseAuth.getInstance();
        curr_user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        users_ref = db.collection("users");
        posts_ref = db.collection("posts");
        user_ref = users_ref.document(curr_user.getUid());

        user_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    user = task.getResult().toObject(User.class);
                    branch_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                branch = task.getResult().toObject(Branch.class);
                                branch_name = branch.name;
                                if (branch.admins.contains(user_ref)) {
                                    isAdmin = true;
                                }
                            } else {
                                Log.wtf("ERROR", "Error reading branch document.", task.getException());
                            }
                        }
                    });
                } else {
                    Log.wtf("ERROR", "Error reading user document.", task.getException());
                }
            }
        });

    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_row, parent, false);

        view.setOnClickListener(this);

        return new PostViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {

        DocumentReference post_ref = posts.get(position);
        post_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Post post = task.getResult().toObject(Post.class);
                    if (post.show || isAdmin) {
                        if (!post.show) {
                            holder.itemView.setVisibility(View.INVISIBLE);
                        }
                        branch_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    branch = task.getResult().toObject(Branch.class);
                                    holder.branch_name_rv.setText(branch.name);
                                    Picasso.get()
                                            .load(branch.backURL)
                                            .into(holder.branch_img_rv);
                                } else {
                                    Log.wtf("ERROR", "Error reading branch document.", task.getException());
                                }
                            }
                        });
                        holder.user_name_rv.setText(curr_user.getEmail());
                        holder.post_body_rv.setText(post.body.substring(0,post.body.length() < 140 ? post.body.length() : 140).concat(" [...]"));
                        holder.likes_rv.setText(String.valueOf(post.likes.size()));
                        holder.com_num_rv.setText(String.valueOf(post.comments.size()));



                        Picasso.get()
                                .load(user.photoURL)
                                .into(holder.author_img_rv);

                        if (post.likes.contains(posts.get(position))) { // has like on post;
                            holder.bt_like_rv.setColorFilter(Color.RED);
                            holder.bt_like_rv.setEnabled(false);
                            holder.bt_dislike_rv.setEnabled(true);
                            haslike = true;
                        } else if (post.dislikes.contains(posts.get(position))) { // has dislike
                            holder.bt_like_rv.setColorFilter(Color.RED);
                            holder.bt_like_rv.setEnabled(true);
                            holder.bt_dislike_rv.setEnabled(false);
                            hasdislike = true;
                        }

                        holder.bt_like_rv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!post.likes.contains(post_ref)) { // user doesnt havea like
                                    if (hasdislike) { // remove dislike
                                        post.dislikes.remove(user_ref);
                                        post_ref.update("dislikes", post.dislikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    hasdislike = false;
                                                    holder.bt_dislike_rv.setColorFilter(Color.WHITE);
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
                                                haslike = true;
                                                holder.bt_like_rv.setColorFilter(Color.RED);
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

                        holder.bt_dislike_rv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (!post.dislikes.contains(post_ref)) { // user doesnt havea like
                                    if (haslike) { // remove dislike
                                        post.likes.remove(user_ref);
                                        post_ref.update("likes", post.likes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    haslike = false;
                                                    Log.i("INFO", "Successfully removed like from the post");
                                                } else {
                                                    Log.wtf("ERROR", "Error removing like from post.", task.getException());
                                                }
                                            }
                                        });
                                    }
                                    post.dislikes.add(user_ref);
                                    post_ref.update("dislikes", post.dislikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                hasdislike = true;
                                                holder.bt_dislike_rv.setColorFilter(Color.WHITE);
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

                        //
                        if (isAdmin) {
                            holder.hide.setVisibility(View.VISIBLE);
                            holder.itemView.setBackgroundColor(Color.LTGRAY);
                            holder.hide.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    posts.get(position).update("show", !post.show).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("UPDATE", "Post hidden successfully.");
                                            } else {
                                                Log.wtf("ERROR", "Error updating post document.", task.getException());
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                } else {
                    Log.wtf("ERROR", "Error reading post document", task.getException());
                }
            }
        });



    }

    @Override
    public int getItemCount() { return this.posts.size(); }

    public void setOnClickListener(View.OnClickListener listener) { this.listener = listener; }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onClick(view);
        }
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {

        TextView branch_name_rv, user_name_rv, post_body_rv, likes_rv, com_num_rv;
        ImageView branch_img_rv, author_img_rv, bt_like_rv, bt_dislike_rv;
        FloatingActionButton hide;

        public PostViewHolder(@NonNull View v) {
            super(v);

            hide = v.findViewById(R.id.post_rv_hide);
            // ImageView
            branch_img_rv = v.findViewById(R.id.branch_img_rv);
            author_img_rv = v.findViewById(R.id.author_img_rv);
            bt_like_rv= v.findViewById(R.id.bt_like_rv);
            bt_dislike_rv= v.findViewById(R.id.bt_disike_rv);
            //TextView
            branch_name_rv= v.findViewById(R.id.branch_name_rv);
            user_name_rv= v.findViewById(R.id.user_name_rv);
            post_body_rv = v.findViewById(R.id.post_body_rv);
            likes_rv= v.findViewById(R.id.likes_rv);
            com_num_rv= v.findViewById(R.id.com_num_rv);
        }
    }
}