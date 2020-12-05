package com.itesm.devxican_mobile.ui.branch;

import android.content.Context;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.itesm.devxican_mobile.R;
import com.itesm.devxican_mobile.data.model.Branch;
import com.itesm.devxican_mobile.data.model.User;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class BranchAdapter extends RecyclerView.Adapter<com.itesm.devxican_mobile.ui.branch.BranchAdapter.BranchViewHolder> implements View.OnClickListener {


    public StorageReference mStorageRef;
    public FirebaseFirestore db;
    public FirebaseAuth mAuth;
    public CollectionReference branches_ref, users_ref;
    public FirebaseUser curr_user;
    public DocumentReference user_ref;
    public User user;

    public static final String ERROR = "ERROR";
    public static final String INFO = "INFO";

    DocumentReference branch_ref; // will be constantly changing
    Branch branchdata; // also constantly changing

    public View.OnClickListener listener;
    public Context context;
    public ArrayList<DocumentReference> branches;

    public BranchAdapter(Context context, ArrayList<DocumentReference> branches) {

        this.context = context;
        this.branches = branches;

        mAuth = FirebaseAuth.getInstance();
        curr_user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        branches_ref = db.collection("branches");
        users_ref = db.collection("users");
        mStorageRef = FirebaseStorage.getInstance().getReference("branches");

        user_ref = users_ref.document(curr_user.getUid());
        user_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    user = task.getResult().toObject(User.class);
                    //Log.i(INFO, );
                } else {
                    Log.w(ERROR, "Error getting user document.", task.getException());
                }
            }
        });

    }

    @NonNull
    @Override
    public BranchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.branch_row, parent, false);

        view.setOnClickListener(this);

        return new BranchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchViewHolder holder, int position) {


        branch_ref = branches_ref.document(branches.get(position).getId());

        branch_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Branch branch = task.getResult().toObject(Branch.class);

                    if (branch.followers.contains(user_ref)) {
                        holder.bt_follow.setText("unfollow");
                    }

                    // downloading image from firebase storage
                    Picasso.get()
                            .load(branch.backURL)
                            .fit()
                            .centerInside()
                            .into(holder.branch_img);


                    holder.post_b.setText(String.valueOf(branch.posts.size()));
                    holder.foll_b.setText(String.valueOf(branch.followers.size()));
                    holder.name_b.setText("r/".concat(branch.name));

                } else {
                    Log.wtf("ERROR", "Error reading branch document.", task.getException());
                }
            }
        });

        holder.bt_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            user = task.getResult().toObject(User.class);

                            if (user.branches.contains(branch_ref)) {
                                user.branches.remove(branch_ref);
                                holder.bt_follow.setText("follow");
                            } else {
                                user.branches.add(branch_ref);
                                holder.bt_follow.setText("unfollow");
                            }

                            user_ref.update("branches", user.branches).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        branch_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    Log.i("SUCCESS", "Updated user successfully.");
                                                    Branch branch = task.getResult().toObject(Branch.class);

                                                    if (branch.followers.contains(user_ref)) { // already follows
                                                        branch.followers.remove(user_ref);
                                                    } else {
                                                        branch.followers.add(user_ref);
                                                    }

                                                    branch.followers.add(user_ref);
                                                    branch_ref.update("followers", branch.followers).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.i("SUCCESS", "Updated branch successfully.");
                                                            } else {
                                                                Log.wtf("Error", "Error reading branch document.", task.getException());
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Log.wtf("Error", "Error reading branch document.", task.getException());
                                                }
                                            }
                                        });

                                    } else {
                                        Log.wtf("Error", "Error reading user document.", task.getException());
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() { return this.branches.size(); }

    public void setOnClickListener(View.OnClickListener listener) { this.listener = listener; }

    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onClick(view);
        }
    }

    public class BranchViewHolder extends RecyclerView.ViewHolder {

        ImageView branch_img;
        TextView name_b, foll_b, post_b;
        Button bt_follow;
        public BranchViewHolder(@NonNull View itemView) {
            super(itemView);

            branch_img = itemView.findViewById(R.id.branch_img);
            name_b = itemView.findViewById(R.id.branch_name);
            foll_b = itemView.findViewById(R.id.branch_followers);
            post_b = itemView.findViewById(R.id.branch_posts);
            bt_follow = itemView.findViewById(R.id.follow);
        }


    }

}
