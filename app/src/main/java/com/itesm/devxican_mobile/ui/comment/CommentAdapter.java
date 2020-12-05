package com.itesm.devxican_mobile.ui.comment;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.itesm.devxican_mobile.data.model.Comment;
import com.itesm.devxican_mobile.data.model.Post;
import com.itesm.devxican_mobile.data.model.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    public FirebaseFirestore db;
    public FirebaseAuth mAuth;
    public FirebaseUser curr_user;
    public CollectionReference comments_ref, users_ref;
    public DocumentReference post_ref, user_ref;
    public Post post;
    public User user;

    public Context context;
    public ArrayList<DocumentReference> comments;
    public boolean isAdmin = false, haslike = false, hasdislike = false; // constantly changing variables

    public CommentAdapter(Context context, ArrayList<DocumentReference> comments, DocumentReference post_ref) {

        this.context = context;
        this.comments = comments;
        this.post_ref = post_ref;

        mAuth = FirebaseAuth.getInstance();
        curr_user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
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

    }

    @NonNull
    @Override
    public CommentAdapter.CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_row, parent, false);
        return new CommentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.CommentViewHolder holder, int position) {

        DocumentReference com_ref = comments.get(position);
        com_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Comment com = task.getResult().toObject(Comment.class);
                    com.post.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentReference branch_ref = task.getResult().toObject(Post.class).branch;
                                branch_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Branch branch = task.getResult().toObject(Branch.class);
                                            if (branch.admins.contains(user_ref)) {
                                                Log.d("INFO", "ERES ADMIN");
                                                isAdmin = true;
                                                if (isAdmin) {
                                                    holder.hide.setVisibility(View.VISIBLE);
                                                    holder.hide.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View view) {
                                                            com_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Comment com = task.getResult().toObject(Comment.class);
                                                                        com.show = !com.show;
                                                                        com_ref.update("show", com.show).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    if (!com.show) {
                                                                                        holder.itemView.setBackgroundColor(Color.LTGRAY);
                                                                                    } else {
                                                                                        //#EABFBF
                                                                                        holder.itemView.setBackgroundColor(Color.argb(255,(14*16)+10,(11*16)+15, (11*16)+15));
                                                                                    }
                                                                                    Log.d("UPDATE", !com.show ? "Comment hidden successfully." : "Comment shown successfully.");
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
                                                }
                                            } else {
                                                com_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        holder.itemView.setVisibility(task.getResult().toObject(Comment.class).show ? View.VISIBLE : View.INVISIBLE);
                                                    }
                                                });
                                            }
                                        } else {
                                            Log.wtf("ERROR", "Error reading branch document.", task.getException());
                                        }
                                    }
                                });
                            } else {
                                Log.wtf("ERROR", "Error reading post document.", task.getException());
                            }
                        }
                    });


                    if (com.likes.contains(user_ref)) { // has like on post;
                        holder.bt_like.setEnabled(false);
                        holder.bt_like.setVisibility(View.INVISIBLE);
                        holder.bt_dislike.setEnabled(true);
                        holder.bt_dislike.setVisibility(View.VISIBLE);
                        haslike = true;
                    } else if (com.dislikes.contains(user_ref)) { // has dislike
                        holder.bt_like.setEnabled(true);
                        holder.bt_like.setVisibility(View.VISIBLE);
                        holder.bt_dislike.setEnabled(false);
                        holder.bt_dislike.setVisibility(View.INVISIBLE);
                        hasdislike = true;
                    }

                    holder.bt_like.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!com.likes.contains(user_ref)) { // user has no like in comment, so proceed
                                // remove dislike
                                if (hasdislike) {
                                    com.dislikes.remove(user_ref);
                                    com_ref.update("dislikes", com.dislikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                hasdislike = false;
                                                holder.bt_dislike.setEnabled(true);
                                                holder.bt_dislike.setVisibility(View.VISIBLE);
                                                Log.i("SUCCESS", "Comment successfully un-disliked.");
                                            } else {
                                                Log.wtf("ERROR", "Error un-disliking comment", task.getException());
                                            }
                                        }
                                    });
                                }
                                // add like
                                com.likes.add(user_ref);
                                com_ref.update("likes", com.likes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            haslike = true;
                                            holder.bt_like.setEnabled(false);
                                            holder.bt_like.setVisibility(View.INVISIBLE);

                                            com_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        Comment com = task.getResult().toObject(Comment.class);

                                                        holder.likes.setText(String.valueOf(com.likes.size() - com.dislikes.size()));
                                                    } else {
                                                        Log.wtf("ERROR", "Error reading post document.", task.getException());
                                                    }
                                                }
                                            });

                                            Log.i("INFO", "Successfully liked the comment");
                                        } else {
                                            Log.wtf("ERROR", "Error liking comment.");
                                        }
                                    }
                                });
                            } else {
                                Log.w("WARNING", "Already have a like on this comment!");
                            }
                        }
                    });

                    holder.bt_dislike.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!com.dislikes.contains(user_ref)) { // user has no dislike in comment, so proceed
                                // remove like
                                if (haslike) {
                                    com.likes.remove(user_ref);
                                    com_ref.update("likes", com.likes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                haslike = false;
                                                holder.bt_like.setEnabled(true);
                                                holder.bt_like.setVisibility(View.VISIBLE);
                                                Log.i("SUCCESS", "Comment successfully un-liked.");
                                            } else {
                                                Log.wtf("ERROR", "Error un-liking comment", task.getException());
                                            }
                                        }
                                    });
                                }
                                // add dislike
                                com.dislikes.add(user_ref);
                                com_ref.update("dislikes", com.dislikes).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            hasdislike = true;
                                            holder.bt_dislike.setEnabled(false);
                                            holder.bt_dislike.setVisibility(View.INVISIBLE);

                                            com_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        Comment com = task.getResult().toObject(Comment.class);

                                                        holder.likes.setText(String.valueOf(com.likes.size() - com.dislikes.size()));
                                                    } else {
                                                        Log.wtf("ERROR", "Error reading post document.", task.getException());
                                                    }
                                                }
                                            });

                                            Log.i("INFO", "Successfully disliked the comment");
                                        } else {
                                            Log.wtf("ERROR", "Error disliking comment.");
                                        }
                                    }
                                });
                            } else {
                                Log.w("WARNING", "Already have a dislike on this comment!");
                            }
                        }
                    });

                    holder.body.setText(com.body); // body

                    com.author.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                User author = task.getResult().toObject(User.class);

                                holder.author_name.setText(author.name);
                                Picasso.get()
                                        .load(author.photoURL)
                                        .into(holder.author_img);

                                com.post.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            Post post = task.getResult().toObject(Post.class);
                                            // branch info
                                            post.branch.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        Branch branch = task.getResult().toObject(Branch.class);

                                                        holder.branch_name.setText(branch.name);
                                                        Picasso.get()
                                                                .load(branch.backURL)
                                                                .into(holder.author_img);

                                                    } else {
                                                        Log.wtf("ERROR", "Error reading post document.", task.getException());
                                                    }
                                                }
                                            });
                                        } else {
                                            Log.wtf("ERROR", "Error reading post document.", task.getException());
                                        }
                                    }
                                });

                            } else {
                                Log.wtf("ERROR", "Error getting author document.", task.getException());
                            }
                        }
                    });


                } else {
                    Log.wtf("ERROR", "Error reading comment document.", task.getException());
                }
            }
        });

    }

    @Override
    public int getItemCount() { return this.comments.size(); }



    public class CommentViewHolder extends RecyclerView.ViewHolder {

        ImageView author_img, branch_img, bt_like, bt_dislike;
        TextView branch_name, author_name, body, likes;
        FloatingActionButton hide;

        public CommentViewHolder(@NonNull View v) {
            super(v);

            // ImageView
            author_img = v.findViewById(R.id.com_author_img_rv);
            branch_img = v.findViewById(R.id.com_branch_img_rv);

            // TextView
            branch_name = v.findViewById(R.id.com_branch_name_rv);
            author_name = v.findViewById(R.id.com_author_name_rv);
            body = v.findViewById(R.id.com_body_rv);
            likes = v.findViewById(R.id.com_likes_rv);

            // Button
            hide = v.findViewById(R.id.com_hide_rv);
            bt_like = v.findViewById(R.id.bt_com_like_rv);
            bt_dislike = v.findViewById(R.id.bt_com_disike_rv);

        }

    }
}
