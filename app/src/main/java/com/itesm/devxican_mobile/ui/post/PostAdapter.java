package com.itesm.devxican_mobile.ui.post;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.itesm.devxican_mobile.R;
import com.itesm.devxican_mobile.data.model.Branch;
import com.itesm.devxican_mobile.data.model.Post;
import com.itesm.devxican_mobile.data.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> implements View.OnClickListener {

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
                                                Log.d("INFO", "ERES ADMIN");
                                                isAdmin = true;
                                                if (isAdmin) {
                                                    holder.hide.setVisibility(View.VISIBLE);
                                                    holder.hide.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            post_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Post post = task.getResult().toObject(Post.class);
                                                                        post.show = !post.show;
                                                                        post_ref.update("show", post.show).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    if (!post.show) {
                                                                                        holder.itemView.setBackgroundColor(Color.LTGRAY);
                                                                                    } else {
                                                                                        //#EABFBF
                                                                                        holder.itemView.setBackgroundColor(Color.argb(255,(14*16)+10,(11*16)+15, (11*16)+15));
                                                                                    }
                                                                                    Log.d("UPDATE", !post.show ? "Post hidden successfully." : "Post shown successfully.");
                                                                                } else {
                                                                                    Log.wtf("ERROR", "Error updating post document.", task.getException());
                                                                                }
                                                                            }
                                                                        });
                                                                    } else {
                                                                        Log.wtf("ERROR", "Error reading post document.", task.getException());
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                } else {
                                                    post_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            holder.itemView.setVisibility(task.getResult().toObject(Post.class).show ? View.VISIBLE : View.INVISIBLE);
                                                        }
                                                    });
                                                }
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
                    holder.likes_rv.setText(String.valueOf(post.likes.size() - post.dislikes.size()));
                    holder.com_num_rv.setText(String.valueOf(post.comments.size()));
                    Picasso.get()
                            .load(user.photoURL)
                            .into(holder.author_img_rv);

                    if (post.likes.contains(user_ref)) { // has like on post;
                        holder.bt_like_rv.setEnabled(false);
                        holder.bt_like_rv.setVisibility(View.INVISIBLE);
                        holder.bt_dislike_rv.setEnabled(true);
                        holder.bt_dislike_rv.setVisibility(View.VISIBLE);

                        haslike = true;
                    } else if (post.dislikes.contains(user_ref)) { // has dislike
                        holder.bt_like_rv.setEnabled(true);
                        holder.bt_like_rv.setVisibility(View.VISIBLE);
                        holder.bt_dislike_rv.setEnabled(false);
                        holder.bt_dislike_rv.setVisibility(View.INVISIBLE);
                        hasdislike = true;
                    }

                    holder.bt_like_rv.setOnClickListener(new View.OnClickListener() {
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
                                                holder.bt_dislike_rv.setEnabled(true);
                                                holder.bt_dislike_rv.setVisibility(View.VISIBLE);

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
                                            holder.bt_like_rv.setEnabled(false);
                                            holder.bt_like_rv.setVisibility(View.INVISIBLE);

                                            post_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        Post post = task.getResult().toObject(Post.class);

                                                        holder.likes_rv.setText(String.valueOf(post.likes.size() - post.dislikes.size()));
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

                    holder.bt_dislike_rv.setOnClickListener(new View.OnClickListener() {
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
                                                holder.bt_like_rv.setEnabled(true);
                                                holder.bt_like_rv.setVisibility(View.VISIBLE);

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
                                            holder.bt_dislike_rv.setEnabled(false);
                                            holder.bt_dislike_rv.setVisibility(View.INVISIBLE);

                                            post_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        Post post = task.getResult().toObject(Post.class);

                                                        holder.likes_rv.setText(String.valueOf(post.likes.size() - post.dislikes.size()));
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
                                Log.w("WARNING", "Already have a dislike on this post!");
                            }
                        }
                    });

                    post_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                Post post = task.getResult().toObject(Post.class);
                                if (post.author.equals(user_ref)) {
                                    holder.delpost.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Log.wtf("ERROR", "Error", task.getException());
                            }
                        }
                    });

                    holder.delpost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            branch_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Branch branch = task.getResult().toObject(Branch.class);
                                        branch.posts.remove(post_ref);
                                        branch_ref.update("posts", branch.posts).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    user_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                 user = task.getResult().toObject(User.class);
                                                                 user.posts.remove(post_ref);
                                                                 user_ref.update("posts", user.posts).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                     @Override
                                                                     public void onComplete(@NonNull Task<Void> task) {
                                                                         if (task.isSuccessful()) {
                                                                             holder.itemView.setVisibility(View.INVISIBLE);
                                                                             Log.i("SUCCESS", "Erased from db.");
                                                                         } else {
                                                                             Log.wtf("ERROR", "Error updating a post");
                                                                         }
                                                                     }
                                                                 });
                                                            } else {
                                                                Log.wtf("ERROR", "Error reading a post");
                                                            }

                                                        }
                                                    });
                                                } else {
                                                    Log.wtf("ERROR", "Error reading a branch");
                                                }
                                            }
                                        });
                                        post_ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                } else {
                                                    Log.wtf("ERROR", "Error deleting a post");
                                                }
                                            }
                                        });
                                    } else {
                                        Log.wtf("ERROR", "Error reading a post");
                                    }
                                }
                            });
                        }
                    });


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
        ImageButton delpost;

        public PostViewHolder(@NonNull View v) {
            super(v);

            hide = v.findViewById(R.id.post_rv_hide);
            // ImageView
            branch_img_rv = v.findViewById(R.id.branch_img_rv);
            author_img_rv = v.findViewById(R.id.author_img_rv);
            bt_like_rv= v.findViewById(R.id.bt_like_rv);
            bt_dislike_rv= v.findViewById(R.id.bt_disike_rv);

            delpost = v.findViewById(R.id.delpost);
            //TextView
            branch_name_rv= v.findViewById(R.id.branch_name_rv);
            user_name_rv= v.findViewById(R.id.user_name_rv);
            post_body_rv = v.findViewById(R.id.post_body_rv);
            likes_rv= v.findViewById(R.id.likes_rv);
            com_num_rv= v.findViewById(R.id.com_num_rv);
        }
    }
}